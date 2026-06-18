package com.anzo.insurance.modules.finance.entity;

import com.anzo.insurance.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 发票实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_invoice")
public class Invoice extends BaseEntity {

    /**
     * 发票申请编号
     */
    @TableField("invoice_no")
    private String invoiceNo;

    /**
     * 企业ID
     */
    @TableField("enterprise_id")
    private String enterpriseId;

    /**
     * 发票类型：1-增值税普通发票，2-增值税专用发票，3-电子普通发票，4-电子专用发票
     */
    @TableField("invoice_type")
    private Integer invoiceType;

    /**
     * 发票抬头
     */
    @TableField("invoice_title")
    private String invoiceTitle;

    /**
     * 纳税人识别号
     */
    @TableField("taxpayer_id")
    private String taxpayerId;

    /**
     * 地址
     */
    @TableField("address")
    private String address;

    /**
     * 电话
     */
    @TableField("phone")
    private String phone;

    /**
     * 开户银行
     */
    @TableField("bank_name")
    private String bankName;

    /**
     * 银行账号
     */
    @TableField("bank_account")
    private String bankAccount;

    /**
     * 开票金额
     */
    @TableField("invoice_amount")
    private BigDecimal invoiceAmount;

    /**
     * 税额
     */
    @TableField("tax_amount")
    private BigDecimal taxAmount;

    /**
     * 含税金额
     */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /**
     * 货币代码
     */
    @TableField("currency")
    private String currency;

    /**
     * 开票内容
     */
    @TableField("invoice_content")
    private String invoiceContent;

    /**
     * 开票状态：0-草稿，1-待审核，2-审核通过，3-审核拒绝，4-已开票，5-已作废，6-已重开
     */
    @TableField("status")
    private Integer status;

    /**
     * 申请时间
     */
    @TableField("application_time")
    private LocalDateTime applicationTime;

    /**
     * 审核时间
     */
    @TableField("audit_time")
    private LocalDateTime auditTime;

    /**
     * 开票时间
     */
    @TableField("invoice_time")
    private LocalDateTime invoiceTime;

    /**
     * 发票代码
     */
    @TableField("invoice_code")
    private String invoiceCode;

    /**
     * 发票号码
     */
    @TableField("invoice_number")
    private String invoiceNumber;

    /**
     * 开票日期
     */
    @TableField("invoice_date")
    private LocalDate invoiceDate;

    /**
     * 开票人
     */
    @TableField("issuer")
    private String issuer;

    /**
     * 收款人
     */
    @TableField("payee")
    private String payee;

    /**
     * 复核人
     */
    @TableField("reviewer")
    private String reviewer;

    /**
     * 审核意见
     */
    @TableField("audit_opinion")
    private String auditOpinion;

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
     * 作废原因
     */
    @TableField("cancel_reason")
    private String cancelReason;

    /**
     * 作废时间
     */
    @TableField("cancel_time")
    private LocalDateTime cancelTime;

    /**
     * 作废人用户ID
     */
    @TableField("canceller_user_id")
    private String cancellerUserId;

    /**
     * 作废人用户名
     */
    @TableField("canceller_user_name")
    private String cancellerUserName;

    /**
     * 重开发票ID（被重开的发票ID）
     */
    @TableField("reissue_invoice_id")
    private String reissueInvoiceId;

    /**
     * 原发票ID（重开的发票对应的原发票）
     */
    @TableField("original_invoice_id")
    private String originalInvoiceId;

    /**
     * 发票文件URL
     */
    @TableField("invoice_file_url")
    private String invoiceFileUrl;

    /**
     * 发票文件名称
     */
    @TableField("invoice_file_name")
    private String invoiceFileName;

    /**
     * 发票文件大小
     */
    @TableField("invoice_file_size")
    private Long invoiceFileSize;

    /**
     * 关联业务类型：policy-保单，bill-账单
     */
    @TableField("related_business_type")
    private String relatedBusinessType;

    /**
     * 关联业务ID（如保单ID、账单ID）
     */
    @TableField("related_business_id")
    private String relatedBusinessId;

    /**
     * 关联业务描述
     */
    @TableField("related_business_desc")
    private String relatedBusinessDesc;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 是否红字发票（冲红）
     */
    @TableField("is_red_invoice")
    private Boolean isRedInvoice;

    /**
     * 红字发票原因
     */
    @TableField("red_invoice_reason")
    private String redInvoiceReason;

    /**
     * 红字发票编号
     */
    @TableField("red_invoice_no")
    private String redInvoiceNo;

    /**
     * 寄送方式：electronic-电子，express-快递，self_pickup-自取
     */
    @TableField("delivery_method")
    private String deliveryMethod;

    /**
     * 收件人
     */
    @TableField("recipient_name")
    private String recipientName;

    /**
     * 收件电话
     */
    @TableField("recipient_phone")
    private String recipientPhone;

    /**
     * 收件地址
     */
    @TableField("recipient_address")
    private String recipientAddress;

    /**
     * 快递单号
     */
    @TableField("tracking_no")
    private String trackingNo;

    /**
     * 寄送时间
     */
    @TableField("delivery_time")
    private LocalDateTime deliveryTime;

    /**
     * 签收时间
     */
    @TableField("receive_time")
    private LocalDateTime receiveTime;

    /**
     * 获取发票类型名称
     */
    public String getInvoiceTypeName() {
        if (invoiceType == null) return "未知";
        switch (invoiceType) {
            case 1: return "增值税普通发票";
            case 2: return "增值税专用发票";
            case 3: return "电子普通发票";
            case 4: return "电子专用发票";
            default: return "未知";
        }
    }

    /**
     * 获取开票状态名称
     */
    public String getStatusName() {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "草稿";
            case 1: return "待审核";
            case 2: return "审核通过";
            case 3: return "审核拒绝";
            case 4: return "已开票";
            case 5: return "已作废";
            case 6: return "已重开";
            default: return "未知";
        }
    }

    /**
     * 判断是否为电子发票
     */
    public boolean isElectronicInvoice() {
        return invoiceType != null && (invoiceType == 3 || invoiceType == 4);
    }

    /**
     * 判断是否已开票
     */
    public boolean isInvoiced() {
        return status != null && status == 4;
    }

    /**
     * 判断是否已作废
     */
    public boolean isCancelled() {
        return status != null && status == 5;
    }

    /**
     * 判断是否为红字发票
     */
    public boolean isRedInvoice() {
        return Boolean.TRUE.equals(isRedInvoice);
    }

    /**
     * 获取不含税金额
     */
    public BigDecimal getExcludingTaxAmount() {
        return invoiceAmount != null ? invoiceAmount : BigDecimal.ZERO;
    }
}