package com.anzo.insurance.modules.enterprise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 企业审核DTO
 */
@Data
@Schema(description = "企业审核请求")
public class EnterpriseReviewDTO {
    
    @NotBlank(message = "企业ID不能为空")
    @Schema(description = "企业ID", required = true)
    private Long enterpriseId;
    
    @NotNull(message = "审核结果不能为空")
    @Schema(description = "审核结果: APPROVED-通过, REJECTED-拒绝", required = true, allowableValues = {"APPROVED", "REJECTED"})
    private String reviewResult;
    
    @Schema(description = "审核备注")
    private String remark;
    
    @Schema(description = "拒绝原因（审核不通过时必填）")
    private String rejectReason;
}