package com.anzo.insurance.modules.finance.entity;

import com.anzo.insurance.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_transaction_record")
public class TransactionRecord extends BaseEntity {

    /**
     * 交易流水号
     */
    @TableField("transaction_no")
    private String transactionNo;

    /**
     * 企业ID
     */
    @TableField("enterprise_id")
    private String enterpriseId;

    /**
     * 交易类型：1-充值，2-投保扣费，3-退费，4-退款，5-调整，6-冻结，7-解冻
     */
    @TableField("transaction_type")
    private Integer transactionType;

    /**
     * 交易金额（正数表示收入，负数表示支出）
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 货币代码
     */
    @TableField("currency")
    private String currency;

    /**
     * 交易前余额
     */
    @TableField("balance_before")
    private BigDecimal balanceBefore;

    /**
     * 交易后余额
     */
    @TableField("balance_after")
    private BigDecimal balanceAfter;

    /**
     * 交易前冻结金额
     */
    @TableField("frozen_before")
    private BigDecimal frozenBefore;

    /**
     * 交易后冻结金额
     */
    @TableField("frozen_after")
    private BigDecimal frozenAfter;

    /**
     * 关联业务ID（如保单ID、充值记录ID等）
     */
    @TableField("related_business_id")
    private String relatedBusinessId;

    /**
     * 关联业务类型：policy-保单，recharge-充值，refund-退款，adjustment-调整
     */
    @TableField("related_business_type")
    private String relatedBusinessType;

    /**
     * 关联业务描述（如保单号、充值方式等）
     */
    @TableField("related_business_desc")
    private String relatedBusinessDesc;

    /**
     * 交易状态：0-待处理，1-成功，2-失败，3-取消
     */
    @TableField("status")
    private Integer status;

    /**
     * 交易时间
     */
    @TableField("transaction_time")
    private LocalDateTime transactionTime;

    /**
     * 完成时间
     */
    @TableField("completed_time")
    private LocalDateTime completedTime;

    /**
     * 支付方式：bank_transfer-银行转账，wallet_balance-钱包余额，wechat_pay-微信支付，alipay-支付宝
     */
    @TableField("payment_method")
    private String paymentMethod;

    /**
     * 支付流水号（第三方支付时使用）
     */
    @TableField("payment_no")
    private String paymentNo;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 操作用户ID
     */
    @TableField("operator_user_id")
    private String operatorUserId;

    /**
     * 操作用户名
     */
    @TableField("operator_user_name")
    private String operatorUserName;

    /**
     * 是否人工操作
     */
    @TableField("is_manual")
    private Boolean isManual;

    /**
     * 审核状态：0-无需审核，1-待审核，2-审核通过，3-审核拒绝
     */
    @TableField("audit_status")
    private Integer auditStatus;

    /**
     * 审核意见
     */
    @TableField("audit_opinion")
    private String auditOpinion;

    /**
     * 审核时间
     */
    @TableField("audit_time")
    private LocalDateTime auditTime;

    /**
     * 审核人用户ID
     */
    @TableField("auditor_user_id")
    private String auditorUserId;

    /**
     * 审核人用户名
     */
    @TableField("auditor_user_name")
    private String auditorUserName;

    /**
     * 获取交易类型名称
     */
    public String getTransactionTypeName() {
        if (transactionType == null) return "未知";
        switch (transactionType) {
            case 1: return "充值";
            case 2: return "投保扣费";
            case 3: return "退费";
            case 4: return "退款";
            case 5: return "调整";
            case 6: return "冻结";
            case 7: return "解冻";
            default: return "未知";
        }
    }

    /**
     * 获取交易状态名称
     */
    public String getStatusName() {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待处理";
            case 1: return "成功";
            case 2: return "失败";
            case 3: return "取消";
            default: return "未知";
        }
    }

    /**
     * 获取审核状态名称
     */
    public String getAuditStatusName() {
        if (auditStatus == null) return "未知";
        switch (auditStatus) {
            case 0: return "无需审核";
            case 1: return "待审核";
            case 2: return "审核通过";
            case 3: return "审核拒绝";
            default: return "未知";
        }
    }

    /**
     * 判断是否为收入（金额为正）
     */
    public boolean isIncome() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 判断是否为支出（金额为负）
     */
    public boolean isExpense() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) < 0;
    }
}