package com.anzo.insurance.modules.claim.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 理赔创建DTO
 */
@Data
public class ClaimCreateDTO {
    
    /**
     * 保单ID
     */
    @NotBlank(message = "保单ID不能为空")
    private String policyId;
    
    /**
     * 保单号
     */
    @NotBlank(message = "保单号不能为空")
    private String policyNo;
    
    /**
     * 被保险人名称
     */
    @NotBlank(message = "被保险人名称不能为空")
    private String insuredName;
    
    /**
     * 被保险人联系人
     */
    private String insuredContactName;
    
    /**
     * 被保险人联系电话
     */
    private String insuredContactPhone;
    
    /**
     * 报案日期
     */
    @NotNull(message = "报案日期不能为空")
    private LocalDate reportDate;
    
    /**
     * 出险日期
     */
    @NotNull(message = "出险日期不能为空")
    private LocalDate accidentDate;
    
    /**
     * 出险地点
     */
    @NotBlank(message = "出险地点不能为空")
    private String accidentLocation;
    
    /**
     * 出险经过描述
     */
    @NotBlank(message = "出险经过描述不能为空")
    private String accidentDescription;
    
    /**
     * 出险类型
     */
    @NotBlank(message = "出险类型不能为空")
    private String accidentType;
    
    /**
     * 其他出险类型说明
     */
    private String accidentTypeOther;
    
    /**
     * 理赔金额
     */
    @NotNull(message = "理赔金额不能为空")
    private BigDecimal claimAmount;
    
    /**
     * 货币代码
     */
    private String currency = "CNY";
    
    /**
     * 损失情况描述
     */
    private String lossDescription;
    
    /**
     * 损失原因分析
     */
    private String lossReason;
}