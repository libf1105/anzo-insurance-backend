package com.anzo.insurance.modules.enterprise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户创建DTO
 */
@Data
@Schema(description = "用户创建请求")
public class UserCreateDTO {
    
    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名（手机号或邮箱）", required = true)
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)\\S{8,}$",
             message = "密码必须包含大小写字母和数字，长度至少8位")
    @Schema(description = "密码", required = true)
    private String password;
    
    @NotBlank(message = "真实姓名不能为空")
    @Schema(description = "真实姓名", required = true)
    private String realName;
    
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", required = true)
    private String phone;
    
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱")
    private String email;
    
    @NotBlank(message = "角色不能为空")
    @Schema(description = "角色", required = true, allowableValues = {"OPERATOR", "FINANCE"})
    private String role;
}
