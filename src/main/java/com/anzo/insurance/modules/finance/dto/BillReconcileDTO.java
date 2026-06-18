package com.anzo.insurance.modules.finance.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 账单对账DTO
 */
@Data
public class BillReconcileDTO {

    /**
     * 账单ID
     */
    @NotBlank(message = "账单ID不能为空")
    private String id;

    /**
     * 对账备注
     */
    private String remark;

    /**
     * 对账文件URL（可选）
     */
    private String reconciliationFileUrl;
}