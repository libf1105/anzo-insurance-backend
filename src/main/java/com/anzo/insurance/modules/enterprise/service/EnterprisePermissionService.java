package com.anzo.insurance.modules.enterprise.service;

import com.anzo.insurance.common.exception.BusinessException;
import com.anzo.insurance.common.exception.ErrorCode;
import com.anzo.insurance.modules.auth.entity.User;
import com.anzo.insurance.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * 企业权限验证服务
 */
@Service
@RequiredArgsConstructor
public class EnterprisePermissionService {

    private final UserRepository userRepository;

    /**
     * 检查当前用户是否有企业访问权限
     */
    public boolean hasEnterpriseAccess(Long enterpriseId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));
        
        // 如果是超级管理员，可以访问所有企业
        if (User.Role.SUPER_ADMIN.getValue().equals(user.getRole())) {
            return true;
        }
        
        // 其他用户只能访问自己所在的企业
        return user.getEnterpriseId().equals(enterpriseId);
    }

    /**
     * 检查当前用户是否有企业管理权限（管理员以上）
     */
    public boolean hasAdminAccess(Long enterpriseId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));
        
        // 如果是超级管理员，可以管理所有企业
        if (User.Role.SUPER_ADMIN.getValue().equals(user.getRole())) {
            return true;
        }
        
        // 管理员只能管理自己所在的企业
        if (User.Role.ADMIN.getValue().equals(user.getRole())) {
            return user.getEnterpriseId().equals(enterpriseId);
        }
        
        return false;
    }

    /**
     * 检查当前用户是否是目标用户自己
     */
    public boolean isSelf(Long enterpriseId, Long userId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));
        
        // 首先检查是否属于同一企业
        if (!user.getEnterpriseId().equals(enterpriseId)) {
            return false;
        }
        
        // 然后检查是否是同一用户
        return user.getId().equals(userId);
    }

    /**
     * 获取当前用户所在的企业ID
     */
    public Long getCurrentEnterpriseId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));
        
        return user.getEnterpriseId();
    }

    /**
     * 检查当前用户是否是超级管理员
     */
    public boolean isSuperAdmin() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));
        
        return User.Role.SUPER_ADMIN.getValue().equals(user.getRole());
    }

    /**
     * 检查当前用户是否是管理员（企业内）
     */
    public boolean isAdmin() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));
        
        return User.Role.ADMIN.getValue().equals(user.getRole()) || 
               User.Role.SUPER_ADMIN.getValue().equals(user.getRole());
    }
}