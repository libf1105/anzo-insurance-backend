package com.anzo.insurance.modules.policy.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 批量操作DTO
 */
@Data
public class BatchOperationDTO {

    /**
     * 保单ID列表
     */
    @NotNull(message = "保单ID列表不能为空")
    @Size(min = 1, message = "至少选择一个保单")
    private List<String> policyIds;

    /**
     * 操作类型：cancel-撤销，export-导出，download-下载保单
     */
    @NotBlank(message = "操作类型不能为空")
    private String operationType;

    /**
     * 撤销原因（撤销操作时需要）
     */
    private String cancelReason;
}