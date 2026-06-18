package com.anzo.insurance.modules.insurance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

/**
 * 提交投保申请DTO
 */
@Data
@Schema(description = "提交投保申请")
public class SubmitApplicationDTO {
    
    @AssertTrue(message = "必须确认已阅读并同意保险条款")
    @Schema(description = "是否确认保险条款", required = true, example = "true")
    private Boolean termsConfirmed;
    
    @Schema(description = "投保备注", example = "请加急处理")
    private String remark;
}