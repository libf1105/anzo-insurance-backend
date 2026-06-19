package com.anzo.insurance.modules.insurance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 投保步骤3: 货物信息DTO
 */
@Data
@Schema(description = "投保步骤3 - 货物信息")
public class ApplicationStep3DTO {
    
    @NotBlank(message = "货物名称不能为空")
    @Schema(description = "货物名称", required = true, example = "电子产品")
    private String cargoName;
    
    @NotBlank(message = "货物类别不能为空")
    @Schema(description = "货物类别", required = true, example = "电子设备")
    private String cargoCategory;
    
    @NotBlank(message = "包装方式不能为空")
    @Schema(description = "包装方式", required = true, example = "箱装")
    private String packingType;
    
    @NotNull(message = "包装数量不能为空")
    @Positive(message = "包装数量必须大于0")
    @Schema(description = "包装数量", required = true, example = "100")
    private Integer packingQuantity;
    
    @Schema(description = "唛头标记", example = "MADE IN CHINA")
    private String shippingMark;
    
    @NotBlank(message = "币种不能为空")
    @Schema(description = "币种", required = true, example = "CNY")
    private String currency;
    
    @NotNull(message = "保险金额不能为空")
    @Positive(message = "保险金额必须大于0")
    @Schema(description = "保险金额", required = true, example = "1000000")
    private BigDecimal insuranceAmount;
    
    @Schema(description = "发票金额", example = "950000")
    private BigDecimal invoiceAmount;
    
    @Schema(description = "加成比例，默认1.10", example = "1.10")
    private BigDecimal additionRatio = BigDecimal.valueOf(1.10);
    
    @Schema(description = "免赔额", example = "1000")
    private BigDecimal deductible;
    
    @Schema(description = "特别约定", example = "投保货物需在指定仓库存储")
    private String specialTerms;
    
    @Schema(description = "投保申请ID")
    private Long applicationId;
}