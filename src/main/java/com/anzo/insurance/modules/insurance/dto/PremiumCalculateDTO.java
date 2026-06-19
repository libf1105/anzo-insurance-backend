package com.anzo.insurance.modules.insurance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 保费试算请求DTO
 */
@Data
@Schema(description = "保费试算请求")
public class PremiumCalculateDTO {
    
    @NotBlank(message = "贸易方向不能为空")
    @Schema(description = "贸易方向", required = true, example = "EXPORT")
    private String tradeDirection;
    
    @NotBlank(message = "运输方式不能为空")
    @Schema(description = "运输方式", required = true, example = "SEA")
    private String transportType;
    
    @NotBlank(message = "保险产品不能为空")
    @Schema(description = "保险产品", required = true, example = "CARGO")
    private String insuranceProduct;
    
    @NotBlank(message = "货物类别不能为空")
    @Schema(description = "货物类别", required = true, example = "电子产品")
    private String cargoCategory;
    
    @NotNull(message = "保险金额不能为空")
    @Positive(message = "保险金额必须大于0")
    @Schema(description = "保险金额", required = true, example = "1000000")
    private BigDecimal insuranceAmount;
    
    @NotBlank(message = "币种不能为空")
    @Schema(description = "币种", required = true, example = "CNY")
    private String currency;
    
    @Schema(description = "保司ID，不传则使用默认保司")
    private Long insurerId;
    
    @Schema(description = "加成比例", example = "1.10")
    private BigDecimal additionRatio = BigDecimal.valueOf(1.10);
    
    @Schema(description = "投保申请ID，用于续写时试算")
    private Long applicationId;
}