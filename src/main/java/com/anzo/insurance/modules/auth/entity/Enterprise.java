package com.anzo.insurance.modules.auth.entity;

import com.anzo.insurance.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 企业实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("enterprise")
public class Enterprise extends BaseEntity {
    
    private String name;
    
    private String creditCode;
    
    private String contactName;
    
    private String contactPhone;
    
    private String contactEmail;
    
    private String licenseUrl;
    
    @TableField("`status`")
    private String status;
    
    private BigDecimal balance;
    
    private BigDecimal frozenBalance;
    
    private BigDecimal totalRecharged;
    
    private BigDecimal totalConsumed;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime reviewAt;
    
    private String reviewBy;
    
    private String reviewRemark;
    
    // 状态枚举
    public enum Status {
        PENDING_REVIEW("PENDING_REVIEW"),
        ACTIVE("ACTIVE"),
        REJECTED("REJECTED"),
        DISABLED("DISABLED"),
        EXPIRED("EXPIRED");
        
        private final String value;
        
        Status(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
}