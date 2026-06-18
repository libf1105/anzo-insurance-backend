package com.anzo.insurance.modules.insurance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("application_draft")
public class ApplicationDraft {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("enterprise_id")
    private String enterpriseId;

    @TableField("user_id")
    private String userId;

    @TableField("current_step")
    private Integer currentStep;

    @TableField("step1_data")
    private String step1Data;

    @TableField("step2_data")
    private String step2Data;

    @TableField("step3_data")
    private String step3Data;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("expired_at")
    private LocalDateTime expiredAt;
}
