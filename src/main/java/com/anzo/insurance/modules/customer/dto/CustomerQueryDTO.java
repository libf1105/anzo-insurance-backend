package com.anzo.insurance.modules.customer.dto;

import lombok.Data;

/**
 * 客户查询DTO
 */
@Data
public class CustomerQueryDTO {
    
    /**
     * 客户名称（支持模糊查询）
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
     * 状态
     */
    private String status;
    
    /**
     * 页码
     */
    private Integer page = 1;
    
    /**
     * 每页大小
     */
    private Integer size = 10;
}