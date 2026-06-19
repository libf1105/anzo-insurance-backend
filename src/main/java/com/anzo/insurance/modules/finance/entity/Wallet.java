package com.anzo.insurance.modules.finance.entity;

import com.anzo.insurance.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 企业钱包实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_wallet")
public class Wallet extends BaseEntity {

    /**
     * 企业ID
     */
    @TableField("enterprise_id")
    private Long enterpriseId;

    /**
     * 可用余额
     */
    @TableField("available_balance")
    private BigDecimal availableBalance;

    /**
     * 冻结金额
     */
    @TableField("frozen_balance")
    private BigDecimal frozenBalance;

    /**
     * 累计充值金额
     */
    @TableField("total_recharge_amount")
    private BigDecimal totalRechargeAmount;

    /**
     * 累计消费金额
     */
    @TableField("total_consumption_amount")
    private BigDecimal totalConsumptionAmount;

    /**
     * 累计退费金额
     */
    @TableField("total_refund_amount")
    private BigDecimal totalRefundAmount;

    /**
     * 货币代码（默认CNY）
     */
    @TableField("currency")
    private String currency;

    /**
     * 最低预警余额
     */
    @TableField("min_balance_alert")
    private BigDecimal minBalanceAlert;

    /**
     * 是否启用余额预警
     */
    @TableField("balance_alert_enabled")
    private Boolean balanceAlertEnabled;

    /**
     * 钱包状态：0-正常，1-冻结，2-注销
     */
    @TableField("status")
    private Integer status;

    /**
     * 获取总余额（可用 + 冻结）
     */
    public BigDecimal getTotalBalance() {
        return availableBalance.add(frozenBalance);
    }

    /**
     * 检查余额是否充足
     */
    public boolean isBalanceSufficient(BigDecimal amount) {
        return availableBalance.compareTo(amount) >= 0;
    }

    /**
     * 扣减可用余额
     */
    public void deductAvailableBalance(BigDecimal amount) {
        if (availableBalance.compareTo(amount) >= 0) {
            availableBalance = availableBalance.subtract(amount);
            totalConsumptionAmount = totalConsumptionAmount.add(amount);
        }
    }

    /**
     * 增加可用余额
     */
    public void addAvailableBalance(BigDecimal amount) {
        availableBalance = availableBalance.add(amount);
        totalRechargeAmount = totalRechargeAmount.add(amount);
    }

    /**
     * 增加退费金额
     */
    public void addRefundAmount(BigDecimal amount) {
        availableBalance = availableBalance.add(amount);
        totalRefundAmount = totalRefundAmount.add(amount);
    }

    /**
     * 冻结金额
     */
    public void freezeBalance(BigDecimal amount) {
        if (availableBalance.compareTo(amount) >= 0) {
            availableBalance = availableBalance.subtract(amount);
            frozenBalance = frozenBalance.add(amount);
        }
    }

    /**
     * 解冻金额
     */
    public void unfreezeBalance(BigDecimal amount) {
        if (frozenBalance.compareTo(amount) >= 0) {
            frozenBalance = frozenBalance.subtract(amount);
            availableBalance = availableBalance.add(amount);
        }
    }
}