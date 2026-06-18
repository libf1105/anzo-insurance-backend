package com.anzo.insurance.modules.enterprise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户更新DTO
 */
@Data
@Schema(description = "用户更新请求")
public class UserUpdateDTO {
    
    @Schema(description = "真实姓名")
    private String realName;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号")
    private String phone;
    
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱")
    private String email;
    
    @Schema(description = "角色", allowableValues = {"OPERATOR", "FINANCE"})
    private String role;
    
    @Schema(description = "状态", allowableValues = {"ACTIVE", "DISABLED"})
    private String status;
}