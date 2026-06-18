package com.anzo.insurance.modules.claim.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 理赔统计DTO
 */
@Data
public class ClaimStatisticsDTO {
    
    /**
     * 理赔中数量
     */
    private Integer processingCount = 0;
    
    /**
     * 已赔付数量
     */
    private Integer paidCount = 0;
    
    /**
     * 赔付总额
     */
    private BigDecimal totalPaymentAmount = BigDecimal.ZERO;
    
    /**
     * 赔付率
     */
    private BigDecimal paymentRate = BigDecimal.ZERO;
    
    /**
     * 本月新增数量
     */
    private Integer monthlyNewCount = 0;
    
    /**
     * 已拒赔数量
     */
    private Integer rejectedCount = 0;
    
    /**
     * 已撤回数量
     */
    private Integer withdrawnCount = 0;
    
    /**
     * 总数量
     */
    private Integer totalCount = 0;
}