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
    Page<User> getEnterpriseUsers(String enterpriseId, Pageable pageable);
    
    /**
     * 搜索企业用户
     */
    Page<User> searchEnterpriseUsers(String enterpriseId, String keyword, Pageable pageable);
    
    /**
     * 创建企业用户
     */
    User createEnterpriseUser(String enterpriseId, UserCreateDTO createDTO);
    
    /**
     * 获取用户信息
     */
    User getUser(String enterpriseId, String userId);
    
    /**
     * 更新用户信息
     */
    void updateUser(String enterpriseId, String userId, UserUpdateDTO updateDTO);
    
    /**
     * 删除用户
     */
    void deleteUser(String enterpriseId, String userId);
    
    /**
     * 启用用户
     */
    void enableUser(String enterpriseId, String userId);
    
    /**
     * 禁用用户
     */
    void disableUser(String enterpriseId, String userId);
    
    /**
     * 重置用户密码
     */
    void resetPassword(String enterpriseId, String userId, String newPassword);
    
    /**
     * 验证用户权限
     */
    boolean verifyUserPermission(String enterpriseId, String userId);
    
    /**
     * 获取企业用户统计
     */
    UserStatistics getUserStatistics(String enterpriseId);
    
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