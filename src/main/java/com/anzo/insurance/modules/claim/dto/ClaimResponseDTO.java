package com.anzo.insurance.modules.claim.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 理赔响应DTO
 */
@Data
public class ClaimResponseDTO {
    
    /**
     * 理赔ID
     */
    private Long id;
    
    /**
     * 理赔编号
     */
    private String claimNo;
    
    /**
     * 企业ID
     */
    private Long enterpriseId;
    
    /**
     * 保单ID
     */
    private Long policyId;
    
    /**
     * 保单号
     */
    private String policyNo;
    
    /**
     * 被保险人名称
     */
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
    private LocalDate reportDate;
    
    /**
     * 出险日期
     */
    private LocalDate accidentDate;
    
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
     * 出险类型名称
     */
    private String accidentTypeName;
    
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
     * 材料状态
     */
    private String materialStatus;
    
    /**
     * 材料状态名称
     */
    private String materialStatusName;
    
    /**
     * 缺失材料说明
     */
    private String missingMaterials;
    
    /**
     * 理赔状态
     */
    private String status;
    
    /**
     * 理赔状态名称
     */
    private String statusName;
    
    /**
     * 处理人用户ID
     */
    private Long handlerUserId;
    
    /**
     * 处理人用户名
     */
    private String handlerUserName;
    
    /**
     * 处理人指派时间
     */
    private LocalDateTime handlerAssignedAt;
    
    /**
     * 审核人用户ID
     */
    private Long reviewUserId;
    
    /**
     * 审核人用户名
     */
    private String reviewUserName;
    
    /**
     * 审核时间
     */
    private LocalDateTime reviewAt;
    
    /**
     * 审核意见
     */
    private String reviewRemark;
    
    /**
     * 查勘人用户ID
     */
    private Long surveyUserId;
    
    /**
     * 查勘人用户名
     */
    private String surveyUserName;
    
    /**
     * 查勘时间
     */
    private LocalDateTime surveyAt;
    
    /**
     * 查勘报告
     */
    private String surveyReport;
    
    /**
     * 支付操作人ID
     */
    private Long paymentUserId;
    
    /**
     * 支付操作人用户名
     */
    private String paymentUserName;
    
    /**
     * 赔付时间
     */
    private LocalDateTime paymentAt;
    
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
    
    /**
     * 状态变更时间
     */
    private LocalDateTime statusChangedAt;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 材料列表
     */
    private List<ClaimMaterialDTO> materials;
    
    /**
     * 处理记录列表
     */
    private List<ClaimProcessRecordDTO> processRecords;
    
    /**
     * 统计信息
     */
    private ClaimStatisticsDTO statistics;
}