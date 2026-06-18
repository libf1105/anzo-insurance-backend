package com.anzo.insurance.modules.finance.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 钱包更新DTO
 */
@Data
public class WalletUpdateDTO {

    /**
     * 钱包ID
     */
    @NotBlank(message = "钱包ID不能为空")
    private String id;

    /**
     * 操作类型（1-充值，2-扣款，3-冻结，4-解冻，5-调整）
     */
    @NotNull(message = "操作类型不能为空")
    private Integer operationType;

    /**
     * 操作金额
     */
    @NotNull(message = "操作金额不能为空")
    private BigDecimal amount;

    /**
     * 业务类型（1-投保扣费，2-保单退费，3-手动调整，4-其他）
     */
    @NotNull(message = "业务类型不能为空")
    private Integer businessType;

    /**
     * 业务ID（保单ID、申请单ID等）
     */
    private String businessId;

    /**
     * 业务描述
     */
    private String businessDesc;

    /**
     * 操作备注
     */
    private String remark;
}