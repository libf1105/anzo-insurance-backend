package com.anzo.insurance.modules.claim.entity;

import com.anzo.insurance.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 理赔实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_claim")
public class Claim extends BaseEntity {

    /**
     * 理赔编号: CLM{yyyyMMdd}{4位}
     */
    @TableField("claim_no")
    private String claimNo;

    /**
     * 企业ID
     */
    @TableField("enterprise_id")
    private Long enterpriseId;

    /**
     * 保单ID
     */
    @TableField("policy_id")
    private Long policyId;

    /**
     * 保单号
     */
    @TableField("policy_no")
    private String policyNo;

    /**
     * 被保险人名称
     */
    @TableField("insured_name")
    private String insuredName;

    /**
     * 被保险人联系人
     */
    @TableField("insured_contact_name")
    private String insuredContactName;

    /**
     * 被保险人联系电话
     */
    @TableField("insured_contact_phone")
    private String insuredContactPhone;

    /**
     * 报案日期
     */
    @TableField("report_date")
    private LocalDate reportDate;

    /**
     * 出险日期
     */
    @TableField("accident_date")
    private LocalDate accidentDate;

    /**
     * 出险地点
     */
    @TableField("accident_location")
    private String accidentLocation;

    /**
     * 出险经过描述
     */
    @TableField("accident_description")
    private String accidentDescription;

    /**
     * 出险类型: WATER_DAMAGE-水渍损, IMPACT_BREAKAGE-碰损破碎, THEFT-偷盗提货不着, DAMP_HEAT-受潮受热, OTHER-其他
     */
    @TableField("accident_type")
    private String accidentType;

    /**
     * 其他出险类型说明
     */
    @TableField("accident_type_other")
    private String accidentTypeOther;

    /**
     * 理赔金额
     */
    @TableField("claim_amount")
    private BigDecimal claimAmount;

    /**
     * 货币代码
     */
    private String currency;

    /**
     * 损失情况描述
     */
    @TableField("loss_description")
    private String lossDescription;

    /**
     * 损失原因分析
     */
    @TableField("loss_reason")
    private String lossReason;

    /**
     * 材料状态: PENDING-待提交, INCOMPLETE-不完整, COMPLETE-已完整
     */
    @TableField("material_status")
    private String materialStatus;

    /**
     * 缺失材料说明
     */
    @TableField("missing_materials")
    private String missingMaterials;

    /**
     * 理赔状态
     */
    private String status;

    /**
     * 处理人用户ID
     */
    @TableField("handler_user_id")
    private Long handlerUserId;

    /**
     * 处理人用户名
     */
    @TableField("handler_user_name")
    private String handlerUserName;

    /**
     * 处理人指派时间
     */
    @TableField("handler_assigned_at")
    private LocalDateTime handlerAssignedAt;

    /**
     * 审核人用户ID
     */
    @TableField("review_user_id")
    private Long reviewUserId;

    /**
     * 审核人用户名
     */
    @TableField("review_user_name")
    private String reviewUserName;

    /**
     * 审核时间
     */
    @TableField("review_at")
    private LocalDateTime reviewAt;

    /**
     * 审核意见
     */
    @TableField("review_remark")
    private String reviewRemark;

    /**
     * 查勘人用户ID
     */
    @TableField("survey_user_id")
    private Long surveyUserId;

    /**
     * 查勘人用户名
     */
    @TableField("survey_user_name")
    private String surveyUserName;

    /**
     * 查勘时间
     */
    @TableField("survey_at")
    private LocalDateTime surveyAt;

    /**
     * 查勘报告
     */
    @TableField("survey_report")
    private String surveyReport;

    /**
     * 支付操作人ID
     */
    @TableField("payment_user_id")
    private Long paymentUserId;

    /**
     * 支付操作人用户名
     */
    @TableField("payment_user_name")
    private String paymentUserName;

    /**
     * 赔付时间
     */
    @TableField("payment_at")
    private LocalDateTime paymentAt;

    /**
     * 实际赔付金额
     */
    @TableField("payment_amount")
    private BigDecimal paymentAmount;

    /**
     * 赔付货币
     */
    @TableField("payment_currency")
    private String paymentCurrency;

    /**
     * 拒赔原因
     */
    @TableField("reject_reason")
    private String rejectReason;

    /**
     * 撤回原因
     */
    @TableField("withdraw_reason")
    private String withdrawReason;

    /**
     * 状态变更时间
     */
    @TableField("status_changed_at")
    private LocalDateTime statusChangedAt;
}