package com.anzo.insurance.modules.auth.service;

import com.anzo.insurance.common.exception.BusinessException;
import com.anzo.insurance.common.exception.ErrorCode;
import com.anzo.insurance.modules.auth.entity.User;
import com.anzo.insurance.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Spring Security UserDetailsService实现
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));

        // 验证用户状态
        if (!User.Status.ACTIVE.getValue().equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_DISABLED.getCode(), "用户已被禁用");
        }

        // 构建权限列表
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole())
        );

        // 构建Spring Security UserDetails对象
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    /**
     * 根据用户ID加载用户信息
     */
    public UserDetails loadUserById(Long userId) {
        User user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在");
        }

        // 验证用户状态
        if (!User.Status.ACTIVE.getValue().equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_DISABLED.getCode(), "用户已被禁用");
        }

        // 构建权限列表
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole())
        );

        // 构建Spring Security UserDetails对象
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}