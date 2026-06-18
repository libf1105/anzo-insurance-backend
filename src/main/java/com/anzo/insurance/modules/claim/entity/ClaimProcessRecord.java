package com.anzo.insurance.modules.claim.entity;

import com.anzo.insurance.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 理赔处理记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_claim_process_record")
public class ClaimProcessRecord extends BaseEntity {

    /**
     * 理赔ID
     */
    @TableField("claim_id")
    private String claimId;

    /**
     * 原状态
     */
    @TableField("from_status")
    private String fromStatus;

    /**
     * 新状态
     */
    @TableField("to_status")
    private String toStatus;

    /**
     * 处理类型
     */
    @TableField("process_type")
    private String processType;

    /**
     * 处理内容
     */
    @TableField("process_content")
    private String processContent;

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
     * 操作人用户ID
     */
    @TableField("operator_user_id")
    private String operatorUserId;

    /**
     * 操作人用户名
     */
    @TableField("operator_user_name")
    private String operatorUserName;

    /**
     * 操作时间
     */
    @TableField("operation_time")
    private LocalDateTime operationTime;

    /**
     * 是否内部处理记录
     */
    @TableField("is_internal")
    private Boolean internal;
}