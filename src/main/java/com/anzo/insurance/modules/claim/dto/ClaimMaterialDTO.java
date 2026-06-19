package com.anzo.insurance.modules.claim.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 理赔材料 DTO
 */
@Data
public class ClaimMaterialDTO {

    private Long id;

    private Long claimId;

    private String materialType;

    private String materialName;

    private String materialDescription;

    private String fileUrl;

    private String fileName;

    private Long fileSize;

    private String fileType;

    private Long uploadUserId;

    private String uploadUserName;

    private LocalDateTime uploadTime;

    private Boolean required;

    private Boolean approved;

    private Long approveUserId;

    private String approveUserName;

    private LocalDateTime approveTime;

    private String approveRemark;
}
