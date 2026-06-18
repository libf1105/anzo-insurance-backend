package com.anzo.insurance.modules.insurance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 投保步骤1: 基础信息DTO
 */
@Data
@Schema(description = "投保步骤1 - 基础信息")
public class ApplicationStep1DTO {
    
    @NotBlank(message = "贸易方向不能为空")
    @Schema(description = "贸易方向: IMPORT, EXPORT, DOMESTIC", required = true)
    private String tradeDirection;
    
    @NotBlank(message = "运输方式不能为空")
    @Schema(description = "运输方式: SEA, AIR, RAIL, ROAD, MULTIMODAL", required = true)
    private String transportType;
    
    @NotBlank(message = "保险产品不能为空")
    @Schema(description = "保险产品: CARGO, LIABILITY", required = true)
    private String insuranceProduct;
    
    @Schema(description = "保司ID，不传则自动分配")
    private String insurerId;
    
    @NotBlank(message = "投保人不能为空")
    @Schema(description = "投保人客户ID", required = true)
    private String applicantId;
    
    @NotBlank(message = "被保险人不能为空")
    @Schema(description = "被保险人客户ID", required = true)
    private String insuredId;
    
    @NotNull(message = "同投保人标志不能为空")
    @Schema(description = "被保险人是否同投保人", defaultValue = "false")
    private Boolean insuredSameAsApplicant = false;
    
    @Schema(description = "投保申请ID，续写草稿时传入")
    private String applicationId;
    
    @Schema(description = "草稿ID，续写时传入")
    private String draftId;
    
    @Schema(description = "模板ID，从模板创建时传入")
    private String templateId;
    
    @Schema(description = "历史投保ID，从历史创建时传入")
    private String historyId;
}
