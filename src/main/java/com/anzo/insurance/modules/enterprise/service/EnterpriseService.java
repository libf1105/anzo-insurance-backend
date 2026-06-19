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
    Enterprise getEnterpriseById(Long enterpriseId);
    
    /**
     * 根据当前登录用户获取企业信息
     */
    Enterprise getCurrentEnterprise();
    
    /**
     * 更新企业信息
     */
    void updateEnterprise(Long enterpriseId, EnterpriseUpdateDTO updateDTO);
    
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
    void enableEnterprise(Long enterpriseId);
    
    /**
     * 禁用企业
     */
    void disableEnterprise(Long enterpriseId, String reason);
    
    /**
     * 更新企业余额
     */
    void updateEnterpriseBalance(Long enterpriseId, String amount, String type);
    
    /**
     * 冻结企业余额
     */
    void freezeEnterpriseBalance(Long enterpriseId, String amount);
    
    /**
     * 解冻企业余额
     */
    void unfreezeEnterpriseBalance(Long enterpriseId, String amount);
    
    /**
     * 获取企业统计信息
     */
    EnterpriseStatistics getEnterpriseStatistics(Long enterpriseId);
    
    /**
     * 检查企业状态
     */
    boolean checkEnterpriseStatus(Long enterpriseId);
    
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