package com.anzo.insurance.modules.claim.service;

import com.anzo.insurance.modules.claim.dto.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;
import java.util.List;

/**
 * 理赔服务接口
 */
public interface ClaimService {
    
    /**
     * 创建理赔
     */
    ClaimResponseDTO createClaim(ClaimCreateDTO dto);
    
    /**
     * 更新理赔
     */
    ClaimResponseDTO updateClaim(String claimId, ClaimUpdateDTO dto);
    
    /**
     * 获取理赔详情
     */
    ClaimResponseDTO getClaim(String claimId);
    
    /**
     * 分页查询理赔
     */
    Page<ClaimResponseDTO> queryClaims(ClaimQueryDTO queryDTO);
    
    /**
     * 根据保单号查询理赔列表
     */
    List<ClaimResponseDTO> getClaimsByPolicy(String policyNo);
    
    /**
     * 获取理赔统计信息
     */
    ClaimStatisticsDTO getClaimStatistics();
    
    /**
     * 提交理赔材料
     */
    ClaimResponseDTO submitMaterials(String claimId, List<ClaimMaterialCreateDTO> materials);
    
    /**
     * 审核理赔材料
     */
    ClaimResponseDTO reviewMaterials(String claimId, ClaimMaterialReviewDTO reviewDTO);
    
    /**
     * 指派处理人
     */
    ClaimResponseDTO assignHandler(String claimId, String handlerUserId, String handlerUserName);
    
    /**
     * 提交查勘报告
     */
    ClaimResponseDTO submitSurveyReport(String claimId, String surveyReport);
    
    /**
     * 审核理赔
     */
    ClaimResponseDTO reviewClaim(String claimId, String reviewRemark, boolean approved);
    
    /**
     * 处理赔付
     */
    ClaimResponseDTO processPayment(String claimId, BigDecimal paymentAmount);
    
    /**
     * 拒赔
     */
    ClaimResponseDTO rejectClaim(String claimId, String rejectReason);
    
    /**
     * 撤回理赔
     */
    ClaimResponseDTO withdrawClaim(String claimId, String withdrawReason);
    
    /**
     * 获取理赔处理记录
     */
    List<ClaimProcessRecordDTO> getProcessRecords(String claimId);
    
    /**
     * 添加处理记录
     */
    ClaimResponseDTO addProcessRecord(String claimId, ClaimProcessRecordCreateDTO recordDTO);
}
