package com.anzo.insurance.modules.customer.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客户响应DTO
 */
@Data
public class CustomerResponseDTO {
    
    /**
     * 客户ID
     */
    private Long id;
    
    /**
     * 客户企业名称
     */
    private String name;
    
    /**
     * 统一社会信用代码
     */
    private String creditCode;
    
    /**
     * 联系人姓名
     */
    private String contactName;
    
    /**
     * 联系人手机
     */
    private String contactPhone;
    
    /**
     * 联系人邮箱
     */
    private String contactEmail;
    
    /**
     * 地址
     */
    private String address;
    
    /**
     * 国家
     */
    private String country;
    
    /**
     * 城市
     */
    private String city;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}