package com.anzo.insurance.modules.auth.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 注册营业执照上传响应
 */
@Data
@Builder
public class RegisterLicenseUploadResponseDTO {

    private String fileName;

    private String originalName;

    private String fileUrl;

    private String objectKey;

    private Long fileSize;

    private String mimeType;
}
