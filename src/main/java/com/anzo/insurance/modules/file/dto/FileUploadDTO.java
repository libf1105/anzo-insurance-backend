package com.anzo.insurance.modules.file.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 文件上传DTO
 */
@Data
public class FileUploadDTO {

    /**
     * 文件
     */
    @NotNull(message = "文件不能为空")
    private MultipartFile file;

    /**
     * 文件类型
     * BUSINESS_LICENSE - 营业执照
     * TAX_REGISTRATION - 税务登记证
     * LEGAL_PERSON_ID - 法人身份证
     * OTHER - 其他文件
     */
    @NotBlank(message = "文件类型不能为空")
    private String fileType;

    /**
     * 文件描述
     */
    private String description;

    /**
     * 企业ID（从token中获取，非必填）
     */
    private String enterpriseId;

    /**
     * 文件元数据（JSON格式）
     */
    private String metadata;
}