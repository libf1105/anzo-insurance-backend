package com.anzo.insurance.modules.claim.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 理赔更新DTO
 */
@Data
public class ClaimUpdateDTO {
    
    /**
     * 被保险人联系人
     */
    private String insuredContactName;
    
    /**
     * 被保险人联系电话
     */
    private String insuredContactPhone;
    
    /**
     * 出险地点
     */
    private String accidentLocation;
    
    /**
     * 出险经过描述
     */
    private String accidentDescription;
    
    /**
     * 出险类型
     */
    private String accidentType;
    
    /**
     * 其他出险类型说明
     */
    private String accidentTypeOther;
    
    /**
     * 理赔金额
     */
    private BigDecimal claimAmount;
    
    /**
     * 货币代码
     */
    private String currency;
    
    /**
     * 损失情况描述
     */
    private String lossDescription;
    
    /**
     * 损失原因分析
     */
    private String lossReason;
    
    /**
     * 缺失材料说明
     */
    private String missingMaterials;
    
    /**
     * 审核意见
     */
    private String reviewRemark;
    
    /**
     * 查勘报告
     */
    private String surveyReport;
    
    /**
     * 实际赔付金额
     */
    private BigDecimal paymentAmount;
    
    /**
     * 赔付货币
     */
    private String paymentCurrency;
    
    /**
     * 拒赔原因
     */
    private String rejectReason;
    
    /**
     * 撤回原因
     */
    private String withdrawReason;
}