package com.anzo.insurance.modules.finance.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 账单查询DTO
 */
@Data
public class BillQueryDTO {

    /**
     * 企业ID
     */
    private Long enterpriseId;

    /**
     * 企业名称（模糊查询）
     */
    private String enterpriseName;

    /**
     * 账单编号（模糊查询）
     */
    private String billNo;

    /**
     * 账单状态
     */
    private Integer status;

    /**
     * 对账状态
     */
    private Integer reconciliationStatus;

    /**
     * 账单周期（YYYY-MM）
     */
    private String billingPeriod;

    /**
     * 最小账单金额
     */
    private BigDecimal minBillAmount;

    /**
     * 最大账单金额
     */
    private BigDecimal maxBillAmount;

    /**
     * 最小已付金额
     */
    private BigDecimal minPaidAmount;

    /**
     * 最大已付金额
     */
    private BigDecimal maxPaidAmount;

    /**
     * 付款截止日期开始
     */
    private LocalDate dueDateStart;

    /**
     * 付款截止日期结束
     */
    private LocalDate dueDateEnd;

    /**
     * 实际付款日期开始
     */
    private LocalDate paymentDateStart;

    /**
     * 实际付款日期结束
     */
    private LocalDate paymentDateEnd;

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
    private String sortField = "billingMonth";

    /**
     * 排序方向（asc/desc）
     */
    private String sortOrder = "desc";
}