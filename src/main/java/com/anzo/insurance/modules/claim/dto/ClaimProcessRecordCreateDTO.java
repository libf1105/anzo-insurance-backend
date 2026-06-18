package com.anzo.insurance.modules.claim.dto;

import lombok.Data;

/**
 * 理赔处理记录创建DTO
 */
@Data
public class ClaimProcessRecordCreateDTO {
    
    /**
     * 原状态
     */
    private String fromStatus;
    
    /**
     * 新状态
     */
    private String toStatus;
    
    /**
     * 处理类型
     */
    private String processType;
    
    /**
     * 处理内容
     */
    private String processContent;
    
    /**
     * 附件URL
     */
    private String attachmentUrl;
    
    /**
     * 附件名称
     */
    private String attachmentName;
    
    /**
     * 是否内部处理记录
     */
    private Boolean internal = false;
}