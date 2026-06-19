package com.anzo.insurance.modules.finance.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 钱包查询DTO
 */
@Data
public class WalletQueryDTO {

    /**
     * 企业ID
     */
    private Long enterpriseId;

    /**
     * 企业名称（模糊查询）
     */
    private String enterpriseName;

    /**
     * 钱包状态
     */
    private Integer status;

    /**
     * 最小余额
     */
    private java.math.BigDecimal minBalance;

    /**
     * 最大余额
     */
    private java.math.BigDecimal maxBalance;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 页码
     */
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @NotNull(message = "每页大小不能为空")
    @Min(value = 1, message = "每页大小必须大于0")
    @Min(value = 100, message = "每页大小不能超过100")
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField = "createdAt";

    /**
     * 排序方向（asc/desc）
     */
    private String sortOrder = "desc";
}