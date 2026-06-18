package com.anzo.insurance.modules.claim.dto;

import lombok.Data;

/**
 * 理赔材料创建DTO
 */
@Data
public class ClaimMaterialCreateDTO {
    
    /**
     * 材料类型
     */
    private String materialType;
    
    /**
     * 材料名称
     */
    private String materialName;
    
    /**
     * 材料描述
     */
    private String materialDescription;
    
    /**
     * 文件URL
     */
    private String fileUrl;
    
    /**
     * 文件名称
     */
    private String fileName;
    
    /**
     * 文件大小(字节)
     */
    private Long fileSize;
    
    /**
     * 文件类型
     */
    private String fileType;
    
    /**
     * 是否必需材料
     */
    private Boolean required = true;
}