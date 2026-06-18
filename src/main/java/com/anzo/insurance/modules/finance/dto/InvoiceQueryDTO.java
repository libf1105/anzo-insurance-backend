package com.anzo.insurance.modules.finance.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 发票查询DTO
 */
@Data
public class InvoiceQueryDTO {

    /**
     * 企业ID
     */
    private String enterpriseId;

    /**
     * 企业名称（模糊查询）
     */
    private String enterpriseName;

    /**
     * 发票号码（模糊查询）
     */
    private String invoiceNo;

    /**
     * 开票状态
     */
    private Integer status;

    /**
     * 发票类型
     */
    private Integer invoiceType;

    /**
     * 邮寄状态
     */
    private Integer shippingStatus;

    /**
     * 最小开票金额
     */
    private BigDecimal minInvoiceAmount;

    /**
     * 最大开票金额
     */
    private BigDecimal maxInvoiceAmount;

    /**
     * 最小申请金额
     */
    private BigDecimal minApplyAmount;

    /**
     * 最大申请金额
     */
    private BigDecimal maxApplyAmount;

    /**
     * 申请时间开始
     */
    private LocalDate applyTimeStart;

    /**
     * 申请时间结束
     */
    private LocalDate applyTimeEnd;

    /**
     * 开票时间开始
     */
    private LocalDate invoiceTimeStart;

    /**
     * 开票时间结束
     */
    private LocalDate invoiceTimeEnd;

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
    private String sortField = "applyTime";

    /**
     * 排序方向（asc/desc）
     */
    private String sortOrder = "desc";
}