package com.anzo.insurance.modules.insurance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("application_template")
public class ApplicationTemplate {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("enterprise_id")
    private String enterpriseId;

    private String name;

    @TableField("trade_direction")
    private String tradeDirection;

    @TableField("transport_type")
    private String transportType;

    @TableField("insurance_product")
    private String insuranceProduct;

    @TableField("insurer_id")
    private String insurerId;

    @TableField("applicant_id")
    private String applicantId;

    @TableField("insured_id")
    private String insuredId;

    @TableField("departure_country")
    private String departureCountry;

    @TableField("departure_city")
    private String departureCity;

    @TableField("arrival_country")
    private String arrivalCountry;

    @TableField("arrival_city")
    private String arrivalCity;

    @TableField("cargo_category")
    private String cargoCategory;

    @TableField("packing_type")
    private String packingType;

    @TableField("addition_ratio")
    private BigDecimal additionRatio;

    @TableField("special_terms")
    private String specialTerms;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
