package com.anzo.insurance.modules.finance.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 发票DTO
 */
@Data
public class InvoiceDTO {

    /**
     * 发票ID
     */
    private String id;

    /**
     * 发票号码
     */
    private String invoiceNo;

    /**
     * 企业ID
     */
    private String enterpriseId;

    /**
     * 企业名称
     */
    private String enterpriseName;

    /**
     * 申请单ID
     */
    private String applicationId;

    /**
     * 申请金额
     */
    private BigDecimal applyAmount;

    /**
     * 申请时间
     */
    private LocalDateTime applyTime;

    /**
     * 开票金额
     */
    private BigDecimal invoiceAmount = BigDecimal.ZERO;

    /**
     * 开票时间
     */
    private LocalDateTime invoiceTime;

    /**
     * 发票类型（1-增值税普通发票，2-增值税专用发票）
     */
    private Integer invoiceType;

    /**
     * 发票类型名称
     */
    private String invoiceTypeName;

    /**
     * 发票抬头
     */
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
    private String invoiceContent;

    /**
     * 开票状态（0-待开票，1-开票中，2-已开票，3-开票失败，4-已作废）
     */
    private Integer status;

    /**
     * 开票状态名称
     */
    private String statusName;

    /**
     * 作废原因
     */
    private String voidReason;

    /**
     * 作废时间
     */
    private LocalDateTime voidTime;

    /**
     * 发票文件URL
     */
    private String invoiceFileUrl;

    /**
     * 发票文件名称
     */
    private String invoiceFileName;

    /**
     * 发票文件大小
     */
    private Long invoiceFileSize;

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
     * 邮寄状态（0-待邮寄，1-邮寄中，2-已签收）
     */
    private Integer shippingStatus;

    /**
     * 邮寄状态名称
     */
    private String shippingStatusName;

    /**
     * 快递公司
     */
    private String expressCompany;

    /**
     * 快递单号
     */
    private String trackingNo;

    /**
     * 签收时间
     */
    private LocalDateTime receiveTime;

    /**
     * 申请备注
     */
    private String applyRemark;

    /**
     * 开票备注
     */
    private String invoiceRemark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}