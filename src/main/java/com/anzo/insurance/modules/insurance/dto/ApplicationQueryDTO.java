package com.anzo.insurance.modules.insurance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 投保申请查询DTO
 */
@Data
@Schema(description = "投保申请查询条件")
public class ApplicationQueryDTO {
    
    @Schema(description = "页码", example = "1")
    private Integer page = 1;
    
    @Schema(description = "每页条数", example = "10")
    private Integer pageSize = 10;
    
    @Schema(description = "关键字搜索(投保单号、货物名称、客户名称)")
    private String keyword;
    
    @Schema(description = "投保状态")
    private String status;
    
    @Schema(description = "贸易方向")
    private String tradeDirection;
    
    @Schema(description = "运输方式")
    private String transportType;
    
    @Schema(description = "保险产品")
    private String insuranceProduct;
    
    @Schema(description = "开始日期")
    private LocalDate startDate;
    
    @Schema(description = "结束日期")
    private LocalDate endDate;
    
    @Schema(description = "排序字段", example = "createdAt")
    private String sortBy = "createdAt";
    
    @Schema(description = "排序方向", example = "DESC")
    private String sortOrder = "DESC";
    
    @Schema(description = "投保人ID")
    private Long applicantId;
}