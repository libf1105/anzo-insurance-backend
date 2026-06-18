package com.anzo.insurance.modules.insurance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 投保模板DTO
 */
@Data
@Schema(description = "投保模板")
public class InsuranceTemplateDTO {
    @Schema(description = "模板ID")
    private String id;

    @Schema(description = "模板名称")
    private String name;

    @Schema(description = "贸易方向")
    private String tradeDirection;

    @Schema(description = "运输方式")
    private String transportType;

    @Schema(description = "保险产品")
    private String insuranceProduct;

    @Schema(description = "保司ID")
    private String insurerId;

    @Schema(description = "投保人ID")
    private String applicantId;

    @Schema(description = "投保人名称")
    private String applicantName;

    @Schema(description = "被保险人ID")
    private String insuredId;

    @Schema(description = "被保险人名称")
    private String insuredName;

    @Schema(description = "起运国家")
    private String departureCountry;

    @Schema(description = "起运城市")
    private String departureCity;

    @Schema(description = "目的国家")
    private String arrivalCountry;

    @Schema(description = "目的城市")
    private String arrivalCity;

    @Schema(description = "货物类别")
    private String cargoCategory;

    @Schema(description = "包装方式")
    private String packingType;

    @Schema(description = "加成比例")
    private BigDecimal additionRatio;

    @Schema(description = "特别约定")
    private String specialTerms;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
