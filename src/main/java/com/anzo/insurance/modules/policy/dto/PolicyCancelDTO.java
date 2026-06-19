package com.anzo.insurance.modules.policy.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 保单撤销/退保DTO
 */
@Data
public class PolicyCancelDTO {

    /**
     * 保单ID
     */
    @NotNull(message = "保单ID不能为空")
    private Long id;

    /**
     * 操作类型：cancel-撤销，surrender-退保
     */
    @NotBlank(message = "操作类型不能为空")
    private String operationType;

    /**
     * 原因
     */
    @NotBlank(message = "原因不能为空")
    private String reason;
}