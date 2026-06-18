package com.anzo.insurance.modules.enterprise.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.anzo.insurance.common.exception.BusinessException;
import com.anzo.insurance.common.exception.ErrorCode;
import com.anzo.insurance.modules.auth.entity.Enterprise;
import com.anzo.insurance.modules.auth.entity.User;
import com.anzo.insurance.modules.auth.repository.EnterpriseRepository;
import com.anzo.insurance.modules.auth.repository.UserRepository;
import com.anzo.insurance.modules.enterprise.dto.UserCreateDTO;
import com.anzo.insurance.modules.enterprise.dto.UserUpdateDTO;
import com.anzo.insurance.modules.enterprise.service.UserManagementService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户管理服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public org.springframework.data.domain.Page<User> getEnterpriseUsers(String enterpriseId, Pageable pageable) {
        // 验证企业存在
        Enterprise enterprise = enterpriseRepository.selectById(enterpriseId);
        if (enterprise == null) {
            throw new BusinessException(ErrorCode.ENTERPRISE_NOT_FOUND.getCode(), "企业不存在");
        }

        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getEnterpriseId, enterpriseId)
                .eq(User::getDeleted, false)
                .orderByDesc(User::getCreatedAt);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> page = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                pageable.getPageNumber() + 1, pageable.getPageSize());

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> result = 
            userRepository.selectPage(page, queryWrapper);

        return new org.springframework.data.domain.PageImpl<>(
            result.getRecords(),
            pageable,
            result.getTotal()
        );
    }

    @Override
    public org.springframework.data.domain.Page<User> searchEnterpriseUsers(String enterpriseId, String keyword, Pageable pageable) {
        // 验证企业存在
        Enterprise enterprise = enterpriseRepository.selectById(enterpriseId);
        if (enterprise == null) {
            throw new BusinessException(ErrorCode.ENTERPRISE_NOT_FOUND.getCode(), "企业不存在");
        }

        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getEnterpriseId, enterpriseId)
                .eq(User::getDeleted, false)
                .and(wrapper -> wrapper
                    .like(StrUtil.isNotBlank(keyword), User::getUsername, keyword)
                    .or()
                    .like(StrUtil.isNotBlank(keyword), User::getRealName, keyword)
                    .or()
                    .like(StrUtil.isNotBlank(keyword), User::getPhone, keyword)
                    .or()
                    .like(StrUtil.isNotBlank(keyword), User::getEmail, keyword)
                )
                .orderByDesc(User::getCreatedAt);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> page = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                pageable.getPageNumber() + 1, pageable.getPageSize());

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> result = 
            userRepository.selectPage(page, queryWrapper);

        return new org.springframework.data.domain.PageImpl<>(
            result.getRecords(),
            pageable,
            result.getTotal()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User createEnterpriseUser(String enterpriseId, UserCreateDTO createDTO) {
        // 验证企业存在
        Enterprise enterprise = enterpriseRepository.selectById(enterpriseId);
        if (enterprise == null) {
            throw new BusinessException(ErrorCode.ENTERPRISE_NOT_FOUND.getCode(), "企业不存在");
        }

        // 验证用户名是否已存在
        if (userRepository.existsByUsername(createDTO.getUsername())) {
            throw new BusinessException(ErrorCode.USER_EXISTS.getCode(), "用户名已存在");
        }

        // 验证当前用户权限（必须是管理员）
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "当前用户不存在"));
        
        if (!User.Role.ADMIN.getValue().equals(currentUser.getRole()) && 
            !User.Role.SUPER_ADMIN.getValue().equals(currentUser.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "没有权限创建用户");
        }

        // 创建用户
        User user = new User();
        user.setId(IdUtil.fastSimpleUUID());
        user.setEnterpriseId(enterpriseId);
        user.setUsername(createDTO.getUsername());
        user.setPasswordHash(passwordEncoder.encode(createDTO.getPassword()));
        user.setRealName(createDTO.getRealName());
        user.setPhone(createDTO.getPhone());
        user.setEmail(createDTO.getEmail());
        user.setRole(createDTO.getRole());
        user.setStatus(User.Status.ACTIVE.getValue());
        user.setCreatedAt(LocalDateTime.now());
        user.setCreatedBy(currentUsername);

        userRepository.insert(user);
        
        log.info("企业用户已创建: enterpriseId={}, username={}, role={}", 
            enterpriseId, createDTO.getUsername(), createDTO.getRole());
        
        return user;
    }

    @Override
    public User getUser(String enterpriseId, String userId) {
        // 验证企业存在
        Enterprise enterprise = enterpriseRepository.selectById(enterpriseId);
        if (enterprise == null) {
            throw new BusinessException(ErrorCode.ENTERPRISE_NOT_FOUND.getCode(), "企业不存在");
        }

        User user = userRepository.selectById(userId);
        if (user == null || !user.getEnterpriseId().equals(enterpriseId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在或不属于该企业");
        }

        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(String enterpriseId, String userId, UserUpdateDTO updateDTO) {
        // 验证企业存在
        Enterprise enterprise = enterpriseRepository.selectById(enterpriseId);
        if (enterprise == null) {
            throw new BusinessException(ErrorCode.ENTERPRISE_NOT_FOUND.getCode(), "企业不存在");
        }

        User user = userRepository.selectById(userId);
        if (user == null || !user.getEnterpriseId().equals(enterpriseId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在或不属于该企业");
        }

        // 验证当前用户权限
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "当前用户不存在"));
        
        if (!User.Role.ADMIN.getValue().equals(currentUser.getRole()) && 
            !User.Role.SUPER_ADMIN.getValue().equals(currentUser.getRole()) &&
            !currentUser.getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "没有权限修改该用户信息");
        }

        // 更新用户信息
        if (StrUtil.isNotBlank(updateDTO.getRealName())) {
            user.setRealName(updateDTO.getRealName());
        }
        if (StrUtil.isNotBlank(updateDTO.getPhone())) {
            user.setPhone(updateDTO.getPhone());
        }
        if (StrUtil.isNotBlank(updateDTO.getEmail())) {
            user.setEmail(updateDTO.getEmail());
        }
        if (StrUtil.isNotBlank(updateDTO.getRole())) {
            user.setRole(updateDTO.getRole());
        }
        if (StrUtil.isNotBlank(updateDTO.getStatus())) {
            user.setStatus(updateDTO.getStatus());
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedBy(currentUsername);
        
        userRepository.updateById(user);
        
        log.info("企业用户已更新: enterpriseId={}, userId={}, username={}", 
            enterpriseId, userId, user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(String enterpriseId, String userId) {
        // 验证企业存在
        Enterprise enterprise = enterpriseRepository.selectById(enterpriseId);
        if (enterprise == null) {
            throw new BusinessException(ErrorCode.ENTERPRISE_NOT_FOUND.getCode(), "企业不存在");
        }

        User user = userRepository.selectById(userId);
        if (user == null || !user.getEnterpriseId().equals(enterpriseId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在或不属于该企业");
        }

        // 验证当前用户权限
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "当前用户不存在"));
        
        if (!User.Role.ADMIN.getValue().equals(currentUser.getRole()) && 
            !User.Role.SUPER_ADMIN.getValue().equals(currentUser.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "没有权限删除用户");
        }

        // 逻辑删除
        user.setDeleted(true);
        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedBy(currentUsername);
        
        userRepository.updateById(user);
        
        log.info("企业用户已删除: enterpriseId={}, userId={}, username={}", 
            enterpriseId, userId, user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableUser(String enterpriseId, String userId) {
        updateUserStatus(enterpriseId, userId, User.Status.ACTIVE.getValue());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableUser(String enterpriseId, String userId) {
        updateUserStatus(enterpriseId, userId, User.Status.DISABLED.getValue());
    }

    private void updateUserStatus(String enterpriseId, String userId, String status) {
        User user = getUser(enterpriseId, userId);
        
        // 验证当前用户权限
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "当前用户不存在"));
        
        if (!User.Role.ADMIN.getValue().equals(currentUser.getRole()) && 
            !User.Role.SUPER_ADMIN.getValue().equals(currentUser.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "没有权限修改用户状态");
        }

        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedBy(currentUsername);
        
        userRepository.updateById(user);
        
        log.info("企业用户状态已更新: enterpriseId={}, userId={}, status={}", 
            enterpriseId, userId, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String enterpriseId, String userId, String newPassword) {
        User user = getUser(enterpriseId, userId);
        
        // 验证当前用户权限
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "当前用户不存在"));
        
        if (!User.Role.ADMIN.getValue().equals(currentUser.getRole()) && 
            !User.Role.SUPER_ADMIN.getValue().equals(currentUser.getRole()) &&
            !currentUser.getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "没有权限重置密码");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedBy(currentUsername);
        
        userRepository.updateById(user);
        
        log.info("企业用户密码已重置: enterpriseId={}, userId={}", enterpriseId, userId);
    }

    @Override
    public boolean verifyUserPermission(String enterpriseId, String userId) {
        User user = getUser(enterpriseId, userId);
        return User.Status.ACTIVE.getValue().equals(user.getStatus());
    }

    @Override
    public UserStatistics getUserStatistics(String enterpriseId) {
        // 验证企业存在
        Enterprise enterprise = enterpriseRepository.selectById(enterpriseId);
        if (enterprise == null) {
            throw new BusinessException(ErrorCode.ENTERPRISE_NOT_FOUND.getCode(), "企业不存在");
        }

        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getEnterpriseId, enterpriseId)
                .eq(User::getDeleted, false);

        List<User> users = userRepository.selectList(queryWrapper);

        int totalUsers = users.size();
        int activeUsers = (int) users.stream()
                .filter(u -> User.Status.ACTIVE.getValue().equals(u.getStatus()))
                .count();
        int disabledUsers = totalUsers - activeUsers;
        int operatorCount = (int) users.stream()
                .filter(u -> User.Role.OPERATOR.getValue().equals(u.getRole()))
                .count();
        int financeCount = (int) users.stream()
                .filter(u -> User.Role.FINANCE.getValue().equals(u.getRole()))
                .count();

        int adminCount = (int) users.stream()
                .filter(u -> User.Role.ADMIN.getValue().equals(u.getRole()))
                .count();

        return new UserStatistics() {
            @Override
            public Integer getTotalUsers() {
                return totalUsers;
            }

            @Override
            public Integer getActiveUsers() {
                return activeUsers;
            }

            @Override
            public Integer getDisabledUsers() {
                return disabledUsers;
            }

            @Override
            public Integer getOperatorCount() {
                return operatorCount;
            }

            @Override
            public Integer getFinanceCount() {
                return financeCount;
            }
        };
    }
}