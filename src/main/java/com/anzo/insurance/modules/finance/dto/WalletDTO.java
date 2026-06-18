package com.anzo.insurance.modules.finance.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 企业钱包DTO
 */
@Data
public class WalletDTO {

    /**
     * 钱包ID
     */
    private String id;

    /**
     * 企业ID
     */
    private String enterpriseId;

    /**
     * 企业名称
     */
    private String enterpriseName;

    /**
     * 钱包余额
     */
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * 冻结金额
     */
    private BigDecimal frozenAmount = BigDecimal.ZERO;

    /**
     * 可用余额（钱包余额 - 冻结金额）
     */
    private BigDecimal availableBalance = BigDecimal.ZERO;

    /**
     * 总充值金额
     */
    private BigDecimal totalRechargeAmount = BigDecimal.ZERO;

    /**
     * 总消费金额
     */
    private BigDecimal totalConsumeAmount = BigDecimal.ZERO;

    /**
     * 总退款金额
     */
    private BigDecimal totalRefundAmount = BigDecimal.ZERO;

    /**
     * 钱包状态（0-正常，1-冻结，2-异常）
     */
    private Integer status;

    /**
     * 钱包状态名称
     */
    private String statusName;

    /**
     * 最近交易时间
     */
    private LocalDateTime lastTransactionTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}