package com.anzo.insurance.modules.claim.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 理赔处理记录 DTO
 */
@Data
public class ClaimProcessRecordDTO {

    private Long id;

    private Long claimId;

    private String fromStatus;

    private String toStatus;

    private String processType;

    private String processContent;

    private String attachmentUrl;

    private String attachmentName;

    private Long operatorUserId;

    private String operatorUserName;

    private LocalDateTime operationTime;

    private Boolean internal;

    private LocalDateTime createdAt;
}
