package com.anzo.insurance.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 修改密码 DTO
 */
@Data
@Schema(description = "修改密码请求")
public class ChangePasswordDTO {

    @NotBlank(message = "旧密码不能为空")
    @Schema(description = "旧密码", required = true)
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)\\S{8,}$",
            message = "密码必须包含大小写字母和数字，长度至少8位")
    @Schema(description = "新密码", required = true)
    private String newPassword;
}
