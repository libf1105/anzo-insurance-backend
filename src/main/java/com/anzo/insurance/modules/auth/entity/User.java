package com.anzo.insurance.modules.auth.entity;

import com.anzo.insurance.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {
    
    private Long enterpriseId;
    
    private String username;
    
    private String passwordHash;
    
    private String realName;
    
    private String phone;
    
    private String email;
    
    @TableField("`role`")
    private String role;
    
    @TableField("`status`")
    private String status;
    
    private LocalDateTime lastLoginAt;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @TableField(exist = false)
    private String enterpriseName;
    
    @TableField(exist = false)
    private String enterpriseStatus;
    
    // 状态枚举
    public enum Status {
        ACTIVE("ACTIVE"),
        DISABLED("DISABLED");
        
        private final String value;
        
        Status(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    // 角色枚举
    public enum Role {
        SUPER_ADMIN("SUPER_ADMIN"),
        ADMIN("ADMIN"),
        OPERATOR("OPERATOR"),
        FINANCE("FINANCE");
        
        private final String value;
        
        Role(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
}