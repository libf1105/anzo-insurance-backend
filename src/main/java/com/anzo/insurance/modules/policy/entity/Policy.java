package com.anzo.insurance.modules.policy.entity;

import com.anzo.insurance.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 保单实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_policy")
public class Policy extends BaseEntity {

    /**
     * 保单号
     */
    @TableField("policy_no")
    private String policyNo;

    /**
     * 投保申请ID
     */
    @TableField("application_id")
    private Long applicationId;

    /**
     * 企业ID
     */
    @TableField("enterprise_id")
    private Long enterpriseId;

    /**
     * 投保人用户ID
     */
    @TableField("applicant_user_id")
    private Long applicantUserId;

    /**
     * 保单状态
     * 0: 已提交  1: 待审核  2: 保司审核中  3: 已承保  4: 已生效  5: 已撤销  6: 已退保  7: 已过期
     */
    @TableField("status")
    private Integer status;

    /**
     * 保险公司ID
     */
    @TableField("insurer_id")
    private Long insurerId;

    /**
     * 保险公司名称
     */
    @TableField("insurer_name")
    private String insurerName;

    /**
     * 贸易方向：0-进口，1-出口，2-国内
     */
    @TableField("trade_direction")
    private Integer tradeDirection;

    /**
     * 运输方式：0-海运，1-空运，2-铁路，3-陆运，4-多式联运
     */
    @TableField("transport_mode")
    private Integer transportMode;

    /**
     * 投保人名称
     */
    @TableField("applicant_name")
    private String applicantName;

    /**
     * 被保人名称
     */
    @TableField("insured_name")
    private String insuredName;

    /**
     * 货物名称
     */
    @TableField("cargo_name")
    private String cargoName;

    /**
     * 货物类别
     */
    @TableField("cargo_category")
    private String cargoCategory;

    /**
     * 包装方式
     */
    @TableField("packing_method")
    private String packingMethod;

    /**
     * 包装数量
     */
    @TableField("packing_quantity")
    private Integer packingQuantity;

    /**
     * 唛头
     */
    @TableField("shipping_mark")
    private String shippingMark;

    /**
     * 保险金额
     */
    @TableField("insured_amount")
    private BigDecimal insuredAmount;

    /**
     * 保险金额币种
     */
    @TableField("currency")
    private String currency;

    /**
     * 发票金额
     */
    @TableField("invoice_amount")
    private BigDecimal invoiceAmount;

    /**
     * 加成比例
     */
    @TableField("addition_rate")
    private BigDecimal additionRate;

    /**
     * 免赔额
     */
    @TableField("deductible_amount")
    private BigDecimal deductibleAmount;

    /**
     * 保费金额
     */
    @TableField("premium_amount")
    private BigDecimal premiumAmount;

    /**
     * 费率
     */
    @TableField("rate")
    private BigDecimal rate;

    /**
     * 费率显示格式
     */
    @TableField("rate_display")
    private String rateDisplay;

    /**
     * 费率计算说明
     */
    @TableField("rate_calculation_desc")
    private String rateCalculationDesc;

    /**
     * 启运地国家
     */
    @TableField("origin_country")
    private String originCountry;

    /**
     * 启运地城市
     */
    @TableField("origin_city")
    private String originCity;

    /**
     * 目的地国家
     */
    @TableField("destination_country")
    private String destinationCountry;

    /**
     * 目的地城市
     */
    @TableField("destination_city")
    private String destinationCity;

    /**
     * 启运日期
     */
    @TableField("departure_date")
    private LocalDate departureDate;

    /**
     * 预计到达日期
     */
    @TableField("expected_arrival_date")
    private LocalDate expectedArrivalDate;

    /**
     * 运输工具信息（船名航次/航班号/车次等）
     */
    @TableField("transport_vehicle_info")
    private String transportVehicleInfo;

    /**
     * 提单号/运单号
     */
    @TableField("document_no")
    private String documentNo;

    /**
     * 特别约定
     */
    @TableField("special_agreements")
    private String specialAgreements;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 保单生效日期
     */
    @TableField("effective_date")
    private LocalDate effectiveDate;

    /**
     * 保单到期日期
     */
    @TableField("expiry_date")
    private LocalDate expiryDate;

    /**
     * 保单文件URL
     */
    @TableField("policy_file_url")
    private String policyFileUrl;

    /**
     * 保单文件名称
     */
    @TableField("policy_file_name")
    private String policyFileName;

    /**
     * 保单文件大小
     */
    @TableField("policy_file_size")
    private Long policyFileSize;

    /**
     * 撤销原因
     */
    @TableField("cancellation_reason")
    private String cancellationReason;

    /**
     * 撤销时间
     */
    @TableField("cancellation_time")
    private LocalDateTime cancellationTime;

    /**
     * 退保原因
     */
    @TableField("surrender_reason")
    private String surrenderReason;

    /**
     * 退保时间
     */
    @TableField("surrender_time")
    private LocalDateTime surrenderTime;

    /**
     * 退费金额
     */
    @TableField("refund_amount")
    private BigDecimal refundAmount;

    /**
     * 保司保单号
     */
    @TableField("insurer_policy_no")
    private String insurerPolicyNo;

    /**
     * 保司确认时间
     */
    @TableField("insurer_confirmed_time")
    private LocalDateTime insurerConfirmedTime;

    /**
     * 投保日期
     */
    @TableField("application_date")
    private LocalDate applicationDate;

    /**
     * 提交时间
     */
    @TableField("submission_time")
    private LocalDateTime submissionTime;

    /**
     * 创建者用户名
     */
    @TableField(value = "create_user_name", fill = FieldFill.INSERT)
    private String createUserName;

    /**
     * 更新者用户名
     */
    @TableField(value = "update_user_name", fill = FieldFill.UPDATE)
    private String updateUserName;

    /**
     * 获取状态中文名称
     */
    public String getStatusName() {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "已提交";
            case 1: return "待审核";
            case 2: return "保司审核中";
            case 3: return "已承保";
            case 4: return "已生效";
            case 5: return "已撤销";
            case 6: return "已退保";
            case 7: return "已过期";
            default: return "未知";
        }
    }

    /**
     * 获取贸易方向中文名称
     */
    public String getTradeDirectionName() {
        if (tradeDirection == null) return "未知";
        switch (tradeDirection) {
            case 0: return "进口";
            case 1: return "出口";
            case 2: return "国内";
            default: return "未知";
        }
    }

    /**
     * 获取运输方式中文名称
     */
    public String getTransportModeName() {
        if (transportMode == null) return "未知";
        switch (transportMode) {
            case 0: return "海运";
            case 1: return "空运";
            case 2: return "铁路";
            case 3: return "陆运";
            case 4: return "多式联运";
            default: return "未知";
        }
    }
}