package com.anzo.insurance.modules.finance.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 发票申请DTO
 */
@Data
public class InvoiceApplyDTO {

    /**
     * 申请单ID（关联的业务单据ID）
     */
    @NotBlank(message = "申请单ID不能为空")
    private String applicationId;

    /**
     * 企业ID
     */
    @NotBlank(message = "企业ID不能为空")
    private String enterpriseId;

    /**
     * 申请金额
     */
    @NotNull(message = "申请金额不能为空")
    private BigDecimal applyAmount;

    /**
     * 发票类型（1-增值税普通发票，2-增值税专用发票）
     */
    @NotNull(message = "发票类型不能为空")
    private Integer invoiceType;

    /**
     * 发票抬头
     */
    @NotBlank(message = "发票抬头不能为空")
    private String invoiceTitle;

    /**
     * 纳税人识别号
     */
    private String taxpayerId;

    /**
     * 开户银行
     */
    private String bankName;

    /**
     * 银行账号
     */
    private String bankAccount;

    /**
     * 注册地址
     */
    private String registerAddress;

    /**
     * 注册电话
     */
    private String registerPhone;

    /**
     * 购买方名称
     */
    private String buyerName;

    /**
     * 购买方纳税人识别号
     */
    private String buyerTaxpayerId;

    /**
     * 购买方地址电话
     */
    private String buyerAddressPhone;

    /**
     * 购买方开户行及账号
     */
    private String buyerBankAccount;

    /**
     * 发票内容
     */
    @NotBlank(message = "发票内容不能为空")
    private String invoiceContent;

    /**
     * 是否邮寄
     */
    @NotNull(message = "是否邮寄不能为空")
    private Boolean needShipping = false;

    /**
     * 邮寄地址
     */
    private String shippingAddress;

    /**
     * 收件人
     */
    private String recipientName;

    /**
     * 收件人电话
     */
    private String recipientPhone;

    /**
     * 申请备注
     */
    private String applyRemark;

    /**
     * 账单ID列表（可选的，关联的账单ID）
     */
    private List<String> billIds;
}