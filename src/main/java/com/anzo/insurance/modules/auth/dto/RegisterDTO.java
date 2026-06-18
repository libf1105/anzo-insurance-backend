package com.anzo.insurance.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 注册请求DTO
 */
@Data
@Schema(description = "注册请求")
public class RegisterDTO {
    
    // 企业信息
    @NotBlank(message = "企业名称不能为空")
    @Schema(description = "企业名称", required = true)
    private String enterpriseName;
    
    @NotBlank(message = "统一社会信用代码不能为空")
    @Schema(description = "统一社会信用代码", required = true)
    private String creditCode;
    
    @NotBlank(message = "联系人姓名不能为空")
    @Schema(description = "联系人姓名", required = true)
    private String contactName;
    
    @NotBlank(message = "联系人手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "联系人手机号", required = true)
    private String contactPhone;
    
    @Email(message = "邮箱格式不正确")
    @Schema(description = "联系人邮箱")
    private String contactEmail;
    
    // 管理员账号信息
    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名（手机号或邮箱）", required = true)
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$", 
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
}