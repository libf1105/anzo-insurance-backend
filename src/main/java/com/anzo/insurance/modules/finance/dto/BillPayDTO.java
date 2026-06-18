package com.anzo.insurance.modules.finance.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 账单支付DTO
 */
@Data
public class BillPayDTO {

    /**
     * 账单ID
     */
    @NotBlank(message = "账单ID不能为空")
    private String id;

    /**
     * 支付金额
     */
    @NotNull(message = "支付金额不能为空")
    private BigDecimal paymentAmount;

    /**
     * 支付方式（1-余额扣款，2-在线支付，3-银行转账）
     */
    @NotNull(message = "支付方式不能为空")
    private Integer paymentMethod;

    /**
     * 支付备注
     */
    private String remark;

    /**
     * 支付流水号（第三方支付流水号）
     */
    private String paymentNo;
}