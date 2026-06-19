package com.anzo.insurance.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 认证响应DTO
 */
@Data
@Builder
@Schema(description = "认证响应")
public class AuthResponseDTO {
    
    @Schema(description = "访问令牌")
    private String token;
    
    @Schema(description = "刷新令牌")
    private String refreshToken;
    
    @Schema(description = "令牌类型")
    private String tokenType = "Bearer";
    
    @Schema(description = "过期时间（秒）")
    private Long expiresIn;
    
    @Schema(description = "用户信息")
    private UserDTO user;
    
    @Schema(description = "企业信息")
    private EnterpriseDTO enterprise;
    
    @Data
    @Builder
    @Schema(description = "用户信息")
    public static class UserDTO {
        @Schema(description = "用户ID")
        private Long id;
        
        @Schema(description = "用户名")
        private String username;
        
        @Schema(description = "真实姓名")
        private String realName;
        
        @Schema(description = "手机号")
        private String phone;
        
        @Schema(description = "邮箱")
        private String email;
        
        @Schema(description = "角色")
        private String role;
        
        @Schema(description = "状态")
        private String status;
        
        @Schema(description = "最后登录时间")
        private String lastLoginAt;
    }
    
    @Data
    @Builder
    @Schema(description = "企业信息")
    public static class EnterpriseDTO {
        @Schema(description = "企业ID")
        private Long id;
        
        @Schema(description = "企业名称")
        private String name;
        
        @Schema(description = "统一社会信用代码")
        private String creditCode;
        
        @Schema(description = "状态")
        private String status;
        
        @Schema(description = "余额")
        private String balance;
        
        @Schema(description = "联系人姓名")
        private String contactName;
        
        @Schema(description = "联系人手机号")
        private String contactPhone;
    }
}