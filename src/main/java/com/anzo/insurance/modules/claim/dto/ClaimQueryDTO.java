package com.anzo.insurance.modules.claim.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 理赔查询DTO
 */
@Data
public class ClaimQueryDTO {
    
    /**
     * 理赔编号/保单号（支持模糊查询）
     */
    private String searchKeyword;
    
    /**
     * 理赔状态
     */
    private String status;
    
    /**
     * 出险类型
     */
    private String accidentType;
    
    /**
     * 报案开始日期
     */
    private LocalDate reportStartDate;
    
    /**
     * 报案结束日期
     */
    private LocalDate reportEndDate;
    
    /**
     * 页码
     */
    private Integer page = 1;
    
    /**
     * 每页大小
     */
    private Integer size = 10;
    
    /**
     * 排序字段
     */
    private String sortField = "reportDate";
    
    /**
     * 排序方向
     */
    private String sortOrder = "desc";
}