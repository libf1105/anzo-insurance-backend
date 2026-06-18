package com.anzo.insurance.modules.finance.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 发票更新DTO
 */
@Data
public class InvoiceUpdateDTO {

    /**
     * 发票ID
     */
    @NotBlank(message = "发票ID不能为空")
    private String id;

    /**
     * 操作类型（1-开票，2-作废，3-更新邮寄信息）
     */
    @NotNull(message = "操作类型不能为空")
    private Integer operationType;

    /**
     * 开票金额（开票操作时需要）
     */
    private BigDecimal invoiceAmount;

    /**
     * 发票号码（开票操作时需要）
     */
    private String invoiceNo;

    /**
     * 作废原因（作废操作时需要）
     */
    private String voidReason;

    /**
     * 快递公司（更新邮寄信息时需要）
     */
    private String expressCompany;

    /**
     * 快递单号（更新邮寄信息时需要）
     */
    private String trackingNo;

    /**
     * 发票文件URL（开票操作时需要）
     */
    private String invoiceFileUrl;

    /**
     * 发票文件名称（开票操作时需要）
     */
    private String invoiceFileName;

    /**
     * 发票文件大小（开票操作时需要）
     */
    private Long invoiceFileSize;

    /**
     * 开票备注
     */
    private String invoiceRemark;

    /**
     * 邮寄备注
     */
    private String shippingRemark;
}