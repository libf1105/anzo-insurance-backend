package com.anzo.insurance.modules.finance.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易记录DTO
 */
@Data
public class TransactionRecordDTO {

    /**
     * 交易ID
     */
    private String id;

    /**
     * 交易流水号
     */
    private String transactionNo;

    /**
     * 钱包ID
     */
    private String walletId;

    /**
     * 企业ID
     */
    private String enterpriseId;

    /**
     * 企业名称
     */
    private String enterpriseName;

    /**
     * 交易类型（1-充值，2-投保扣费，3-保单退费，4-退款，5-调整，6-冻结，7-解冻）
     */
    private Integer transactionType;

    /**
     * 交易类型名称
     */
    private String transactionTypeName;

    /**
     * 交易金额
     */
    private BigDecimal amount;

    /**
     * 交易前余额
     */
    private BigDecimal beforeBalance;

    /**
     * 交易后余额
     */
    private BigDecimal afterBalance;

    /**
     * 交易前冻结金额
     */
    private BigDecimal beforeFrozenAmount;

    /**
     * 交易后冻结金额
     */
    private BigDecimal afterFrozenAmount;

    /**
     * 业务类型（1-投保，2-退保，3-手工调整，4-其他）
     */
    private Integer businessType;

    /**
     * 业务类型名称
     */
    private String businessTypeName;

    /**
     * 业务ID（保单ID、申请单ID等）
     */
    private String businessId;

    /**
     * 业务描述
     */
    private String businessDesc;

    /**
     * 支付方式（1-在线支付，2-银行转账，3-余额支付）
     */
    private Integer paymentMethod;

    /**
     * 支付方式名称
     */
    private String paymentMethodName;

    /**
     * 支付流水号（第三方支付流水号）
     */
    private String paymentNo;

    /**
     * 交易状态（0-待处理，1-处理中，2-成功，3-失败，4-已取消）
     */
    private Integer status;

    /**
     * 交易状态名称
     */
    private String statusName;

    /**
     * 交易备注
     */
    private String remark;

    /**
     * 操作人用户ID
     */
    private String operatorUserId;

    /**
     * 操作人用户名
     */
    private String operatorUserName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}