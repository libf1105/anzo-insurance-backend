package com.anzo.insurance.modules.enterprise.service;

import com.anzo.insurance.modules.auth.entity.User;
import com.anzo.insurance.modules.enterprise.dto.UserCreateDTO;
import com.anzo.insurance.modules.enterprise.dto.UserUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 用户管理服务接口（企业内用户管理）
 */
public interface UserManagementService {
    
    /**
     * 获取企业用户列表
     */
    Page<User> getEnterpriseUsers(Long enterpriseId, Pageable pageable);
    
    /**
     * 搜索企业用户
     */
    Page<User> searchEnterpriseUsers(Long enterpriseId, String keyword, Pageable pageable);
    
    /**
     * 创建企业用户
     */
    User createEnterpriseUser(Long enterpriseId, UserCreateDTO createDTO);
    
    /**
     * 获取用户信息
     */
    User getUser(Long enterpriseId, Long userId);
    
    /**
     * 更新用户信息
     */
    void updateUser(Long enterpriseId, Long userId, UserUpdateDTO updateDTO);
    
    /**
     * 删除用户
     */
    void deleteUser(Long enterpriseId, Long userId);
    
    /**
     * 启用用户
     */
    void enableUser(Long enterpriseId, Long userId);
    
    /**
     * 禁用用户
     */
    void disableUser(Long enterpriseId, Long userId);
    
    /**
     * 重置用户密码
     */
    void resetPassword(Long enterpriseId, Long userId, String newPassword);
    
    /**
     * 验证用户权限
     */
    boolean verifyUserPermission(Long enterpriseId, Long userId);
    
    /**
     * 获取企业用户统计
     */
    UserStatistics getUserStatistics(Long enterpriseId);
    
    /**
     * 用户统计信息
     */
    interface UserStatistics {
        Integer getTotalUsers();
        Integer getActiveUsers();
        Integer getDisabledUsers();
        Integer getOperatorCount();
        Integer getFinanceCount();
    }
}