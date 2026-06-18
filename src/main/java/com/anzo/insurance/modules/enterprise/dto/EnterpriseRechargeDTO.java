package com.anzo.insurance.modules.enterprise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 企业充值请求
 */
@Data
@Schema(description = "企业充值请求")
public class EnterpriseRechargeDTO {

    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "0.01", message = "充值金额必须大于0")
    @Schema(description = "充值金额", required = true)
    private BigDecimal amount;

    @Schema(description = "充值备注")
    private String remark;
}
