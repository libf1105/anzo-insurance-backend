package com.anzo.insurance.modules.claim.dto;

import lombok.Data;

/**
 * 理赔材料审核DTO
 */
@Data
public class ClaimMaterialReviewDTO {
    
    /**
     * 是否审核通过
     */
    private boolean approved;
    
    /**
     * 审核意见
     */
    private String reviewRemark;
}