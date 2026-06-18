package com.anzo.insurance.modules.insurance.service;

import com.anzo.insurance.modules.insurance.dto.*;
import com.anzo.insurance.modules.insurance.entity.InsuranceApplication;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 投保业务服务接口
 */
public interface InsuranceService {
    
    /**
     * 保存投保步骤1数据
     */
    InsuranceApplication saveStep1(ApplicationStep1DTO dto);
    
    /**
     * 保存投保步骤2数据
     */
    InsuranceApplication saveStep2(ApplicationStep2DTO dto);
    
    /**
     * 保存投保步骤3数据
     */
    InsuranceApplication saveStep3(ApplicationStep3DTO dto);
    
    /**
     * 保费试算
     */
    PremiumCalculateResponseDTO calculatePremium(PremiumCalculateDTO dto);
    
    /**
     * 提交投保申请
     */
    InsuranceApplication submitApplication(String applicationId, SubmitApplicationDTO dto);
    
    /**
     * 获取投保申请详情
     */
    InsuranceApplication getApplication(String id);

    /**
     * 删除投保申请
     */
    void deleteApplication(String applicationId);
    
    /**
     * 获取企业投保列表
     */
    Page<InsuranceApplication> getApplications(ApplicationQueryDTO query);
    
    /**
     * 撤销投保申请
     */
    InsuranceApplication cancelApplication(String applicationId, String reason);
    
    /**
     * 保存投保草稿
     */
    InsuranceDraftDTO saveDraft(InsuranceDraftDTO dto);
    
    /**
     * 获取投保草稿列表
     */
    List<InsuranceDraftDTO> getDrafts();

    /**
     * 获取投保草稿详情
     */
    InsuranceDraftDTO getDraft(String draftId);
    
    /**
     * 删除投保草稿
     */
    boolean deleteDraft(String draftId);
    
    /**
     * 创建投保模板
     */
    InsuranceTemplateDTO createTemplate(InsuranceTemplateDTO dto);
    
    /**
     * 获取投保模板列表
     */
    List<InsuranceTemplateDTO> getTemplates();

    /**
     * 获取投保模板详情
     */
    InsuranceTemplateDTO getTemplate(String templateId);
    
    /**
     * 删除投保模板
     */
    boolean deleteTemplate(String templateId);
}
