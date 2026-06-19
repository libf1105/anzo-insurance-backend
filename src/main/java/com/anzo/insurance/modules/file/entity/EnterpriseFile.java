package com.anzo.insurance.modules.file.entity;

import com.anzo.insurance.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 企业文件实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("enterprise_files")
public class EnterpriseFile extends BaseEntity {

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件原始名称
     */
    private String originalName;

    /**
     * 文件类型
     * BUSINESS_LICENSE - 营业执照
     * TAX_REGISTRATION - 税务登记证
     * LEGAL_PERSON_ID - 法人身份证
     * OTHER - 其他文件
     */
    private String fileType;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件MIME类型
     */
    private String mimeType;

    /**
     * 文件存储路径
     */
    private String filePath;

    /**
     * 文件访问URL
     */
    private String fileUrl;

    /**
     * 文件MD5值
     */
    private String md5;

    /**
     * 企业ID
     */
    private Long enterpriseId;

    /**
     * 上传用户ID
     */
    private Long uploadUserId;

    /**
     * 文件状态
     * ACTIVE - 有效
     * INACTIVE - 无效
     * DELETED - 已删除
     */
    private String status;

    /**
     * 文件描述
     */
    private String description;

    /**
     * 审核状态
     * PENDING - 待审核
     * APPROVED - 已通过
     * REJECTED - 已拒绝
     */
    private String reviewStatus;

    /**
     * 审核备注
     */
    private String reviewRemark;

    /**
     * 审核人
     */
    private String reviewBy;

    /**
     * 审核时间
     */
    private String reviewAt;

    /**
     * 下载次数
     */
    private Integer downloadCount;

    /**
     * 文件元数据（JSON格式）
     */
    private String metadata;

    /**
     * 文件类型枚举
     */
    public enum FileType {
        BUSINESS_LICENSE("营业执照"),
        TAX_REGISTRATION("税务登记证"),
        LEGAL_PERSON_ID("法人身份证"),
        OTHER("其他文件");

        private final String description;

        FileType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public static FileType fromValue(String value) {
            for (FileType type : FileType.values()) {
                if (type.name().equals(value)) {
                    return type;
                }
            }
            return OTHER;
        }
    }

    /**
     * 文件状态枚举
     */
    public enum Status {
        ACTIVE("有效"),
        INACTIVE("无效"),
        DELETED("已删除");

        private final String description;

        Status(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 审核状态枚举
     */
    public enum ReviewStatus {
        PENDING("待审核"),
        APPROVED("已通过"),
        REJECTED("已拒绝");

        private final String description;

        ReviewStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 辅助方法

    /**
     * 获取文件大小格式化的字符串
     */
    public String getFormattedFileSize() {
        if (fileSize == null) {
            return "0 B";
        }
        
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.2f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", fileSize / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * 获取文件类型描述
     */
    public String getFileTypeDescription() {
        try {
            return FileType.valueOf(fileType).getDescription();
        } catch (IllegalArgumentException e) {
            return "未知类型";
        }
    }

    /**
     * 获取文件状态描述
     */
    public String getStatusDescription() {
        try {
            return Status.valueOf(status).getDescription();
        } catch (IllegalArgumentException e) {
            return "未知状态";
        }
    }

    /**
     * 获取审核状态描述
     */
    public String getReviewStatusDescription() {
        try {
            return ReviewStatus.valueOf(reviewStatus).getDescription();
        } catch (IllegalArgumentException e) {
            return "未知状态";
        }
    }

    /**
     * 是否是营业执照
     */
    public boolean isBusinessLicense() {
        return FileType.BUSINESS_LICENSE.name().equals(fileType);
    }

    /**
     * 是否是税务登记证
     */
    public boolean isTaxRegistration() {
        return FileType.TAX_REGISTRATION.name().equals(fileType);
    }

    /**
     * 是否是法人身份证
     */
    public boolean isLegalPersonId() {
        return FileType.LEGAL_PERSON_ID.name().equals(fileType);
    }

    /**
     * 文件是否有效
     */
    public boolean isActive() {
        return Status.ACTIVE.name().equals(status);
    }

    /**
     * 文件是否已审核通过
     */
    public boolean isApproved() {
        return ReviewStatus.APPROVED.name().equals(reviewStatus);
    }

    /**
     * 文件是否待审核
     */
    public boolean isPendingReview() {
        return ReviewStatus.PENDING.name().equals(reviewStatus);
    }
}