package com.anzo.insurance.modules.finance.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 交易记录查询DTO
 */
@Data
public class TransactionQueryDTO {

    /**
     * 企业ID
     */
    private String enterpriseId;

    /**
     * 钱包ID
     */
    private String walletId;

    /**
     * 交易类型
     */
    private Integer transactionType;

    /**
     * 业务类型
     */
    private Integer businessType;

    /**
     * 业务ID
     */
    private String businessId;

    /**
     * 交易状态
     */
    private Integer status;

    /**
     * 支付方式
     */
    private Integer paymentMethod;

    /**
     * 交易流水号（模糊查询）
     */
    private String transactionNo;

    /**
     * 支付流水号（模糊查询）
     */
    private String paymentNo;

    /**
     * 最小交易金额
     */
    private BigDecimal minAmount;

    /**
     * 最大交易金额
     */
    private BigDecimal maxAmount;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 操作人用户ID
     */
    private String operatorUserId;

    /**
     * 页码
     */
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @NotNull(message = "每页大小不能为空")
    @Min(value = 1, message = "每页大小必须大于0")
    @Min(value = 100, message = "每页大小不能超过100")
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField = "createdAt";

    /**
     * 排序方向（asc/desc）
     */
    private String sortOrder = "desc";
}