package com.anzo.insurance.modules.insurance.entity;

import com.anzo.insurance.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 投保申请实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("insurance_application")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InsuranceApplication extends BaseEntity {
    
    private String enterpriseId;
    
    private String applicationNo;
    
    // Step 1: 基础信息
    @TableField("trade_direction")
    private String tradeDirection;
    
    @TableField("transport_type")
    private String transportType;
    
    @TableField("insurance_product")
    private String insuranceProduct;
    
    private String insurerId;
    
    private String insurerName;
    
    private String applicantId;
    
    private String insuredId;
    
    // Step 2: 运输信息
    private String departureCountry;
    
    private String departureCity;
    
    private String arrivalCountry;
    
    private String arrivalCity;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate departureDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate arrivalDate;
    
    @TableField("transport_details")
    private String transportDetailsJson;
    
    // Step 3: 货物信息
    private String cargoName;
    
    private String cargoCategory;
    
    private String packingType;
    
    private Integer packingQuantity;
    
    private String shippingMark;
    
    private String currency;
    
    private BigDecimal insuranceAmount;
    
    private BigDecimal invoiceAmount;
    
    private BigDecimal additionRatio;
    
    private BigDecimal deductible;
    
    private String specialTerms;
    
    // 费用信息
    private BigDecimal premium;
    
    private String premiumCurrency;
    
    private BigDecimal rate;
    
    // 状态信息
    @TableField("status")
    private String status;
    
    private String rejectReason;
    
    // 时间戳
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submittedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime underwritingAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiredAt;
    
    // 状态枚举
    public enum Status {
        DRAFT("DRAFT"),
        SUBMITTED("SUBMITTED"),
        UNDERWRITING("UNDERWRITING"),
        UNDERWRITTEN("UNDERWRITTEN"),
        ACTIVE("ACTIVE"),
        EXPIRED("EXPIRED"),
        CANCELLED("CANCELLED"),
        MODIFYING("MODIFYING");
        
        private final String value;
        
        Status(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    // 贸易方向枚举
    public enum TradeDirection {
        IMPORT("IMPORT"),
        EXPORT("EXPORT"),
        DOMESTIC("DOMESTIC");
        
        private final String value;
        
        TradeDirection(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    // 运输方式枚举
    public enum TransportType {
        SEA("SEA"),
        AIR("AIR"),
        RAIL("RAIL"),
        ROAD("ROAD"),
        MULTIMODAL("MULTIMODAL");
        
        private final String value;
        
        TransportType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    // 保险产品枚举
    public enum InsuranceProduct {
        CARGO("CARGO"),
        LIABILITY("LIABILITY");
        
        private final String value;
        
        InsuranceProduct(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    @TableField(exist = false)
    private String applicantName;
    
    @TableField(exist = false)
    private String insuredName;
    
    @TableField(exist = false)
    private String applicantPhone;
    
    @TableField(exist = false)
    private String insuredPhone;
}