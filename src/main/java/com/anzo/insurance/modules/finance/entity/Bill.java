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
 * 账单实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_bill")
public class Bill extends BaseEntity {

    /**
     * 账单编号
     */
    @TableField("bill_no")
    private String billNo;

    /**
     * 企业ID
     */
    @TableField("enterprise_id")
    private String enterpriseId;

    /**
     * 账单类型：1-月度账单，2-自定义账单，3-对账单
     */
    @TableField("bill_type")
    private Integer billType;

    /**
     * 账单周期开始日期
     */
    @TableField("period_start_date")
    private LocalDate periodStartDate;

    /**
     * 账单周期结束日期
     */
    @TableField("period_end_date")
    private LocalDate periodEndDate;

    /**
     * 账单生成日期
     */
    @TableField("generation_date")
    private LocalDate generationDate;

    /**
     * 应付金额
     */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /**
     * 已付金额
     */
    @TableField("paid_amount")
    private BigDecimal paidAmount;

    /**
     * 减免金额
     */
    @TableField("deduction_amount")
    private BigDecimal deductionAmount;

    /**
     * 应收金额
     */
    @TableField("receivable_amount")
    private BigDecimal receivableAmount;

    /**
     * 货币代码
     */
    @TableField("currency")
    private String currency;

    /**
     * 账单状态：0-草稿，1-已生成，2-已发送，3-已确认，4-已支付，5-已过期，6-已作废
     */
    @TableField("status")
    private Integer status;

    /**
     * 发送时间
     */
    @TableField("sent_time")
    private LocalDateTime sentTime;

    /**
     * 确认时间
     */
    @TableField("confirmed_time")
    private LocalDateTime confirmedTime;

    /**
     * 支付时间
     */
    @TableField("paid_time")
    private LocalDateTime paidTime;

    /**
     * 付款方式：bank_transfer-银行转账，wallet_balance-钱包余额，wechat_pay-微信支付，alipay-支付宝
     */
    @TableField("payment_method")
    private String paymentMethod;

    /**
     * 付款流水号
     */
    @TableField("payment_no")
    private String paymentNo;

    /**
     * 逾期天数
     */
    @TableField("overdue_days")
    private Integer overdueDays;

    /**
     * 逾期费用
     */
    @TableField("overdue_fee")
    private BigDecimal overdueFee;

    /**
     * 付款截止日期
     */
    @TableField("due_date")
    private LocalDate dueDate;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 账单详情（JSON格式存储账单明细）
     */
    @TableField("bill_details")
    private String billDetails;

    /**
     * 对账状态：0-未对账，1-已对账，2-有差异
     */
    @TableField("reconciliation_status")
    private Integer reconciliationStatus;

    /**
     * 对账时间
     */
    @TableField("reconciliation_time")
    private LocalDateTime reconciliationTime;

    /**
     * 对账人用户ID
     */
    @TableField("reconciliation_user_id")
    private String reconciliationUserId;

    /**
     * 对账人用户名
     */
    @TableField("reconciliation_user_name")
    private String reconciliationUserName;

    /**
     * 对账差异说明
     */
    @TableField("reconciliation_diff_desc")
    private String reconciliationDiffDesc;

    /**
     * 附件URL
     */
    @TableField("attachment_url")
    private String attachmentUrl;

    /**
     * 附件名称
     */
    @TableField("attachment_name")
    private String attachmentName;

    /**
     * 附件大小
     */
    @TableField("attachment_size")
    private Long attachmentSize;

    /**
     * 获取账单类型名称
     */
    public String getBillTypeName() {
        if (billType == null) return "未知";
        switch (billType) {
            case 1: return "月度账单";
            case 2: return "自定义账单";
            case 3: return "对账单";
            default: return "未知";
        }
    }

    /**
     * 获取账单状态名称
     */
    public String getStatusName() {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "草稿";
            case 1: return "已生成";
            case 2: return "已发送";
            case 3: return "已确认";
            case 4: return "已支付";
            case 5: return "已过期";
            case 6: return "已作废";
            default: return "未知";
        }
    }

    /**
     * 获取对账状态名称
     */
    public String getReconciliationStatusName() {
        if (reconciliationStatus == null) return "未知";
        switch (reconciliationStatus) {
            case 0: return "未对账";
            case 1: return "已对账";
            case 2: return "有差异";
            default: return "未知";
        }
    }

    /**
     * 判断账单是否已支付
     */
    public boolean isPaid() {
        return status != null && status == 4;
    }

    /**
     * 判断账单是否逾期
     */
    public boolean isOverdue() {
        if (dueDate == null || status == null) return false;
        // 只有未支付的账单才可能逾期
        if (status < 4) {
            LocalDate today = LocalDate.now();
            return today.isAfter(dueDate);
        }
        return false;
    }

    /**
     * 获取未付金额
     */
    public BigDecimal getUnpaidAmount() {
        return receivableAmount.subtract(paidAmount);
    }

    /**
     * 判断账单是否完成对账
     */
    public boolean isReconciled() {
        return reconciliationStatus != null && reconciliationStatus == 1;
    }
}