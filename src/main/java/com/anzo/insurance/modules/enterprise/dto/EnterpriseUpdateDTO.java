package com.anzo.insurance.modules.enterprise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 企业更新DTO
 */
@Data
@Schema(description = "企业更新请求")
public class EnterpriseUpdateDTO {
    
    @NotBlank(message = "企业名称不能为空")
    @Schema(description = "企业名称", required = true)
    private String name;
    
    @NotBlank(message = "联系人姓名不能为空")
    @Schema(description = "联系人姓名", required = true)
    private String contactName;
    
    @NotBlank(message = "联系人手机不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "联系人手机", required = true)
    private String contactPhone;
    
    @Email(message = "邮箱格式不正确")
    @Schema(description = "联系人邮箱")
    private String contactEmail;
    
    @Schema(description = "地址")
    private String address;
    
    @Schema(description = "企业介绍")
    private String description;
}