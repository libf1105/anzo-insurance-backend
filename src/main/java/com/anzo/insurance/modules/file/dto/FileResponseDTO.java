package com.anzo.insurance.modules.file.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 文件响应DTO
 */
@Data
@Accessors(chain = true)
public class FileResponseDTO {

    /**
     * 文件ID
     */
    private Long id;

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
     */
    private String fileType;

    /**
     * 文件类型描述
     */
    private String fileTypeDescription;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 格式化文件大小
     */
    private String formattedFileSize;

    /**
     * 文件MIME类型
     */
    private String mimeType;

    /**
     * 文件访问URL
     */
    private String fileUrl;

    /**
     * 文件状态
     */
    private String status;

    /**
     * 文件状态描述
     */
    private String statusDescription;

    /**
     * 审核状态
     */
    private String reviewStatus;

    /**
     * 审核状态描述
     */
    private String reviewStatusDescription;

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
     * 文件描述
     */
    private String description;

    /**
     * 下载次数
     */
    private Integer downloadCount;

    /**
     * 上传用户ID
     */
    private Long uploadUserId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}