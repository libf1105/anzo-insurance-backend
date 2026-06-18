package com.anzo.insurance.modules.enterprise.service;

import com.anzo.insurance.modules.auth.entity.Enterprise;
import com.anzo.insurance.modules.enterprise.dto.EnterpriseQueryDTO;
import com.anzo.insurance.modules.enterprise.dto.EnterpriseReviewDTO;
import com.anzo.insurance.modules.enterprise.dto.EnterpriseUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 企业服务接口
 */
public interface EnterpriseService {
    
    /**
     * 根据ID获取企业信息
     */
    Enterprise getEnterpriseById(String enterpriseId);
    
    /**
     * 根据当前登录用户获取企业信息
     */
    Enterprise getCurrentEnterprise();
    
    /**
     * 更新企业信息
     */
    void updateEnterprise(String enterpriseId, EnterpriseUpdateDTO updateDTO);
    
    /**
     * 更新当前登录用户的企业信息
     */
    void updateCurrentEnterprise(EnterpriseUpdateDTO updateDTO);
    
    /**
     * 分页查询企业列表（管理员用）
     */
    Page<Enterprise> queryEnterprises(EnterpriseQueryDTO queryDTO, Pageable pageable);
    
    /**
     * 查询待审核企业列表
     */
    List<Enterprise> getPendingReviewEnterprises();
    
    /**
     * 审核企业
     */
    void reviewEnterprise(EnterpriseReviewDTO reviewDTO);
    
    /**
     * 启用企业
     */
    void enableEnterprise(String enterpriseId);
    
    /**
     * 禁用企业
     */
    void disableEnterprise(String enterpriseId, String reason);
    
    /**
     * 更新企业余额
     */
    void updateEnterpriseBalance(String enterpriseId, String amount, String type);
    
    /**
     * 冻结企业余额
     */
    void freezeEnterpriseBalance(String enterpriseId, String amount);
    
    /**
     * 解冻企业余额
     */
    void unfreezeEnterpriseBalance(String enterpriseId, String amount);
    
    /**
     * 获取企业统计信息
     */
    EnterpriseStatistics getEnterpriseStatistics(String enterpriseId);
    
    /**
     * 检查企业状态
     */
    boolean checkEnterpriseStatus(String enterpriseId);
    
    /**
     * 企业统计信息
     */
    interface EnterpriseStatistics {
        Integer getTotalUsers();
        Integer getActivePolicies();
        String getTotalPremium();
        String getBalance();
        String getFrozenBalance();
    }
}