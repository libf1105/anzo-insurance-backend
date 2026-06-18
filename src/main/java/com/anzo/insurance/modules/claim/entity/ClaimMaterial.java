package com.anzo.insurance.modules.claim.entity;

import com.anzo.insurance.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 理赔材料实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_claim_material")
public class ClaimMaterial extends BaseEntity {

    /**
     * 理赔ID
     */
    @TableField("claim_id")
    private String claimId;

    /**
     * 材料类型
     */
    @TableField("material_type")
    private String materialType;

    /**
     * 材料名称
     */
    @TableField("material_name")
    private String materialName;

    /**
     * 材料描述
     */
    @TableField("material_description")
    private String materialDescription;

    /**
     * 文件URL
     */
    @TableField("file_url")
    private String fileUrl;

    /**
     * 文件名称
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件大小(字节)
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 文件类型
     */
    @TableField("file_type")
    private String fileType;

    /**
     * 上传人用户ID
     */
    @TableField("upload_user_id")
    private String uploadUserId;

    /**
     * 上传人用户名
     */
    @TableField("upload_user_name")
    private String uploadUserName;

    /**
     * 上传时间
     */
    @TableField("upload_time")
    private LocalDateTime uploadTime;

    /**
     * 是否必需材料
     */
    @TableField("is_required")
    private Boolean required;

    /**
     * 是否已审核通过
     */
    @TableField("is_approved")
    private Boolean approved;

    /**
     * 审核人用户ID
     */
    @TableField("approve_user_id")
    private String approveUserId;

    /**
     * 审核人用户名
     */
    @TableField("approve_user_name")
    private String approveUserName;

    /**
     * 审核时间
     */
    @TableField("approve_time")
    private LocalDateTime approveTime;

    /**
     * 审核意见
     */
    @TableField("approve_remark")
    private String approveRemark;
}