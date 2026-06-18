package com.anzo.insurance.modules.finance.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 账单DTO
 */
@Data
public class BillDTO {

    /**
     * 账单ID
     */
    private String id;

    /**
     * 账单编号
     */
    private String billNo;

    /**
     * 企业ID
     */
    private String enterpriseId;

    /**
     * 企业名称
     */
    private String enterpriseName;

    /**
     * 账单周期（YYYY-MM）
     */
    private String billingPeriod;

    /**
     * 账单月份
     */
    private LocalDate billingMonth;

    /**
     * 保单数量
     */
    private Integer policyCount;

    /**
     * 保险金额总计
     */
    private BigDecimal totalInsuredAmount = BigDecimal.ZERO;

    /**
     * 保费金额总计
     */
    private BigDecimal totalPremiumAmount = BigDecimal.ZERO;

    /**
     * 账单金额
     */
    private BigDecimal billAmount = BigDecimal.ZERO;

    /**
     * 已付金额
     */
    private BigDecimal paidAmount = BigDecimal.ZERO;

    /**
     * 未付金额（账单金额 - 已付金额）
     */
    private BigDecimal unpaidAmount = BigDecimal.ZERO;

    /**
     * 账单状态（0-待生成，1-待付款，2-付款中，3-已付款，4-已逾期，5-已取消）
     */
    private Integer status;

    /**
     * 账单状态名称
     */
    private String statusName;

    /**
     * 付款截止日期
     */
    private LocalDate dueDate;

    /**
     * 实际付款日期
     */
    private LocalDate paymentDate;

    /**
     * 付款方式（1-余额扣款，2-在线支付，3-银行转账）
     */
    private Integer paymentMethod;

    /**
     * 付款方式名称
     */
    private String paymentMethodName;

    /**
     * 付款流水号
     */
    private String paymentNo;

    /**
     * 对账状态（0-未对账，1-已对账）
     */
    private Integer reconciliationStatus;

    /**
     * 对账状态名称
     */
    private String reconciliationStatusName;

    /**
     * 对账时间
     */
    private LocalDateTime reconciliationTime;

    /**
     * 对账人用户ID
     */
    private String reconciliationUserId;

    /**
     * 对账人用户名
     */
    private String reconciliationUserName;

    /**
     * 账单备注
     */
    private String remark;

    /**
     * 账单文件URL
     */
    private String billFileUrl;

    /**
     * 账单文件名称
     */
    private String billFileName;

    /**
     * 账单文件大小
     */
    private Long billFileSize;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}