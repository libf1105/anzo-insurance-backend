package com.anzo.insurance.modules.auth.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.anzo.insurance.common.exception.BusinessException;
import com.anzo.insurance.common.exception.ErrorCode;
import com.anzo.insurance.modules.auth.dto.AuthResponseDTO;
import com.anzo.insurance.modules.auth.dto.ChangePasswordDTO;
import com.anzo.insurance.modules.auth.dto.LoginDTO;
import com.anzo.insurance.modules.auth.dto.RegisterDTO;
import com.anzo.insurance.modules.auth.dto.ResetPasswordDTO;
import com.anzo.insurance.modules.auth.dto.UpdateProfileDTO;
import com.anzo.insurance.modules.auth.entity.Enterprise;
import com.anzo.insurance.modules.auth.entity.User;
import com.anzo.insurance.modules.auth.repository.EnterpriseRepository;
import com.anzo.insurance.modules.auth.repository.UserRepository;
import com.anzo.insurance.modules.auth.service.AuthService;
import com.anzo.insurance.modules.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 认证服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public AuthResponseDTO login(LoginDTO loginDTO) {
        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));

        // 验证用户状态
        if (!User.Status.ACTIVE.getValue().equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_DISABLED.getCode(), "用户已被禁用");
        }

        // 验证企业状态
        Enterprise enterprise = enterpriseRepository.selectById(user.getEnterpriseId());
        if (enterprise == null) {
            throw new BusinessException(ErrorCode.ENTERPRISE_NOT_FOUND.getCode(), "企业不存在");
        }

        if (!Enterprise.Status.ACTIVE.getValue().equals(enterprise.getStatus())) {
            throw new BusinessException(ErrorCode.ENTERPRISE_DISABLED.getCode(), 
                String.format("企业状态异常: %s", enterprise.getStatus()));
        }

        // 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR.getCode(), "密码错误");
        }

        // Spring Security认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getUsername(),
                        loginDTO.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.updateById(user);

        // 生成JWT令牌
        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return buildAuthResponse(user, enterprise, token, refreshToken);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthResponseDTO register(RegisterDTO registerDTO) {
        // 验证企业信用代码是否已存在
        if (enterpriseRepository.existsByCreditCode(registerDTO.getCreditCode())) {
            throw new BusinessException(ErrorCode.ENTERPRISE_EXISTS.getCode(), "企业信用代码已存在");
        }

        // 验证用户名是否已存在
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new BusinessException(ErrorCode.USER_EXISTS.getCode(), "用户名已存在");
        }

        // 创建企业
        Enterprise enterprise = new Enterprise();
        enterprise.setId(IdUtil.fastSimpleUUID());
        enterprise.setName(registerDTO.getEnterpriseName());
        enterprise.setCreditCode(registerDTO.getCreditCode());
        enterprise.setContactName(registerDTO.getContactName());
        enterprise.setContactPhone(registerDTO.getContactPhone());
        enterprise.setContactEmail(registerDTO.getContactEmail());
        enterprise.setStatus(Enterprise.Status.PENDING_REVIEW.getValue());
        enterprise.setBalance(BigDecimal.ZERO);
        enterprise.setCreatedAt(LocalDateTime.now());

        enterpriseRepository.insert(enterprise);

        // 创建管理员用户
        User user = new User();
        user.setId(IdUtil.fastSimpleUUID());
        user.setEnterpriseId(enterprise.getId());
        user.setUsername(registerDTO.getUsername());
        user.setPasswordHash(passwordEncoder.encode(registerDTO.getPassword()));
        user.setRealName(registerDTO.getRealName());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setRole(User.Role.ADMIN.getValue());
        user.setStatus(User.Status.ACTIVE.getValue());
        user.setCreatedAt(LocalDateTime.now());

        userRepository.insert(user);

        // 生成JWT令牌
        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return buildAuthResponse(user, enterprise, token, refreshToken);
    }

    @Override
    public void logout() {
        // 清除安全上下文
        SecurityContextHolder.clearContext();
        
        // 可以从Redis中删除token（如果需要实现单点登出）
        log.info("用户已登出");
    }

    @Override
    public AuthResponseDTO refreshToken(String refreshToken) {
        // 验证refreshToken
        if (StrUtil.isBlank(refreshToken)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID.getCode(), "刷新令牌不能为空");
        }

        // 从refreshToken中提取用户名
        String username = jwtService.extractUsername(refreshToken);
        if (StrUtil.isBlank(username)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID.getCode(), "无效的刷新令牌");
        }

        // 获取用户信息
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));

        // 验证用户状态
        if (!User.Status.ACTIVE.getValue().equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_DISABLED.getCode(), "用户已被禁用");
        }

        // 验证refreshToken是否有效
        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID.getCode(), "刷新令牌已失效");
        }

        // 获取企业信息
        Enterprise enterprise = enterpriseRepository.selectById(user.getEnterpriseId());
        if (enterprise == null) {
            throw new BusinessException(ErrorCode.ENTERPRISE_NOT_FOUND.getCode(), "企业不存在");
        }

        // 生成新的access token
        String newToken = jwtService.generateToken(user);

        return buildAuthResponse(user, enterprise, newToken, refreshToken);
    }

    @Override
    public AuthResponseDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED.getCode(), "用户未登录");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));

        // 获取企业信息
        Enterprise enterprise = enterpriseRepository.selectById(user.getEnterpriseId());
        if (enterprise == null) {
            throw new BusinessException(ErrorCode.ENTERPRISE_NOT_FOUND.getCode(), "企业不存在");
        }

        return buildAuthResponse(user, enterprise, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthResponseDTO updateProfile(UpdateProfileDTO updateProfileDTO) {
        User user = getCurrentUserEntity();
        if (StrUtil.isNotBlank(updateProfileDTO.getPhone()) && !updateProfileDTO.getPhone().equals(user.getPhone())) {
            userRepository.findByPhone(updateProfileDTO.getPhone()).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(user.getId())) {
                    throw new BusinessException(ErrorCode.USER_EXISTS.getCode(), "手机号已被其他账号使用");
                }
            });
            user.setPhone(updateProfileDTO.getPhone());
        }

        user.setRealName(updateProfileDTO.getRealName());
        user.setEmail(updateProfileDTO.getEmail());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.updateById(user);

        Enterprise enterprise = enterpriseRepository.selectById(user.getEnterpriseId());
        return buildAuthResponse(user, enterprise, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(ChangePasswordDTO changePasswordDTO) {
        User user = getCurrentUserEntity();
        if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR.getCode(), "旧密码不正确");
        }
        if (passwordEncoder.matches(changePasswordDTO.getNewPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "新密码不能与旧密码相同");
        }

        user.setPasswordHash(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        User user = userRepository.findByUsername(resetPasswordDTO.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));
        if (!resetPasswordDTO.getPhone().equals(user.getPhone())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "手机号校验失败");
        }

        user.setPasswordHash(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.updateById(user);
    }

    private User getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED.getCode(), "用户未登录");
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));
    }

    private AuthResponseDTO buildAuthResponse(User user, Enterprise enterprise, String token, String refreshToken) {
        return AuthResponseDTO.builder()
                .token(token)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(AuthResponseDTO.UserDTO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .realName(user.getRealName())
                        .phone(user.getPhone())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .status(user.getStatus())
                        .lastLoginAt(user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : null)
                        .build())
                .enterprise(enterprise == null ? null : AuthResponseDTO.EnterpriseDTO.builder()
                        .id(enterprise.getId())
                        .name(enterprise.getName())
                        .creditCode(enterprise.getCreditCode())
                        .status(enterprise.getStatus())
                        .balance(enterprise.getBalance() == null ? "0" : enterprise.getBalance().toString())
                        .contactName(enterprise.getContactName())
                        .contactPhone(enterprise.getContactPhone())
                        .build())
                .build();
    }
}
