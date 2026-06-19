package com.anzo.insurance.modules.insurance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 保费试算响应DTO
 */
@Data
@Builder
@Schema(description = "保费试算响应")
public class PremiumCalculateResponseDTO {
    
    @Schema(description = "保费金额", example = "5000.00")
    private BigDecimal premium;
    
    @Schema(description = "保费币种", example = "CNY")
    private String currency;
    
    @Schema(description = "综合费率(百分比)", example = "0.005")
    private BigDecimal rate;
    
    @Schema(description = "费率显示文本", example = "0.50‱")
    private String rateDisplay;
    
    @Schema(description = "免赔额", example = "1000.00")
    private BigDecimal deductible;
    
    @Schema(description = "基础费率")
    private BigDecimal baseRate;
    
    @Schema(description = "附加费率列表")
    private List<AdditionalRateDTO> additionalRates;
    
    @Schema(description = "加成比例", example = "1.10")
    private BigDecimal additionRatio;
    
    @Schema(description = "保司名称", example = "太平洋保险")
    private String insurerName;
    
    @Schema(description = "保司代码", example = "CPIC")
    private String insurerCode;
    
    @Schema(description = "试算有效期至", example = "2024-01-15T14:30:00")
    private LocalDateTime validUntil;
    
    @Schema(description = "试算参考ID，用于后续提交")
    private String calculationId;
    
    @Data
    @Builder
    @Schema(description = "附加费率")
    public static class AdditionalRateDTO {
        
        @Schema(description = "费率名称", example = "战争附加险")
        private String name;
        
        @Schema(description = "费率类型", example = "WAR_RISK")
        private String type;
        
        @Schema(description = "费率值(百分比)", example = "0.0002")
        private BigDecimal rate;
        
        @Schema(description = "是否必选", example = "false")
        private boolean required;
        
        @Schema(description = "费率描述", example = "针对战争风险的附加费率")
        private String description;
    }
}
