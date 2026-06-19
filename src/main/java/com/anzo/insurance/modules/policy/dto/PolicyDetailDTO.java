package com.anzo.insurance.modules.policy.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 保单详情DTO
 */
@Data
public class PolicyDetailDTO {

    /**
     * 保单ID
     */
    private Long id;

    /**
     * 保单号
     */
    private String policyNo;

    /**
     * 投保申请ID
     */
    private Long applicationId;

    /**
     * 企业ID
     */
    private Long enterpriseId;

    /**
     * 企业名称
     */
    private String enterpriseName;

    /**
     * 投保人用户ID
     */
    private Long applicantUserId;

    /**
     * 投保人用户名
     */
    private String applicantUserName;

    /**
     * 保单状态
     */
    private Integer status;

    /**
     * 保单状态名称
     */
    private String statusName;

    /**
     * 保险公司ID
     */
    private Long insurerId;

    /**
     * 保险公司名称
     */
    private String insurerName;

    /**
     * 贸易方向
     */
    private Integer tradeDirection;

    /**
     * 贸易方向名称
     */
    private String tradeDirectionName;

    /**
     * 运输方式
     */
    private Integer transportMode;

    /**
     * 运输方式名称
     */
    private String transportModeName;

    /**
     * 投保人名称
     */
    private String applicantName;

    /**
     * 被保人名称
     */
    private String insuredName;

    /**
     * 货物名称
     */
    private String cargoName;

    /**
     * 货物类别
     */
    private String cargoCategory;

    /**
     * 包装方式
     */
    private String packingMethod;

    /**
     * 包装数量
     */
    private Integer packingQuantity;

    /**
     * 唛头
     */
    private String shippingMark;

    /**
     * 保险金额
     */
    private BigDecimal insuredAmount;

    /**
     * 保险金额币种
     */
    private String currency;

    /**
     * 发票金额
     */
    private BigDecimal invoiceAmount;

    /**
     * 加成比例
     */
    private BigDecimal additionRate;

    /**
     * 免赔额
     */
    private BigDecimal deductibleAmount;

    /**
     * 保费金额
     */
    private BigDecimal premiumAmount;

    /**
     * 费率
     */
    private BigDecimal rate;

    /**
     * 费率显示格式
     */
    private String rateDisplay;

    /**
     * 费率计算说明
     */
    private String rateCalculationDesc;

    /**
     * 启运地国家
     */
    private String originCountry;

    /**
     * 启运地城市
     */
    private String originCity;

    /**
     * 目的地国家
     */
    private String destinationCountry;

    /**
     * 目的地城市
     */
    private String destinationCity;

    /**
     * 启运日期
     */
    private LocalDate departureDate;

    /**
     * 预计到达日期
     */
    private LocalDate expectedArrivalDate;

    /**
     * 运输工具信息（船名航次/航班号/车次等）
     */
    private String transportVehicleInfo;

    /**
     * 提单号/运单号
     */
    private String documentNo;

    /**
     * 特别约定
     */
    private String specialAgreements;

    /**
     * 备注
     */
    private String remark;

    /**
     * 保单生效日期
     */
    private LocalDate effectiveDate;

    /**
     * 保单到期日期
     */
    private LocalDate expiryDate;

    /**
     * 保单文件URL
     */
    private String policyFileUrl;

    /**
     * 保单文件名称
     */
    private String policyFileName;

    /**
     * 保单文件大小
     */
    private Long policyFileSize;

    /**
     * 撤销原因
     */
    private String cancellationReason;

    /**
     * 撤销时间
     */
    private LocalDateTime cancellationTime;

    /**
     * 退保原因
     */
    private String surrenderReason;

    /**
     * 退保时间
     */
    private LocalDateTime surrenderTime;

    /**
     * 退费金额
     */
    private BigDecimal refundAmount;

    /**
     * 保司保单号
     */
    private String insurerPolicyNo;

    /**
     * 保司确认时间
     */
    private LocalDateTime insurerConfirmedTime;

    /**
     * 投保日期
     */
    private LocalDate applicationDate;

    /**
     * 提交时间
     */
    private LocalDateTime submissionTime;

    /**
     * 创建者用户名
     */
    private String createUserName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新者用户名
     */
    private String updateUserName;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否允许修改
     */
    private Boolean allowModify = false;

    /**
     * 是否允许撤销
     */
    private Boolean allowCancel = false;

    /**
     * 是否允许下载保单
     */
    private Boolean allowDownload = false;

    /**
     * 是否允许退保
     */
    private Boolean allowSurrender = false;

    /**
     * 是否允许续保
     */
    private Boolean allowRenew = false;
}