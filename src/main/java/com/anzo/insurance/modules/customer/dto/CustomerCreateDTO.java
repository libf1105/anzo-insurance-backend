package com.anzo.insurance.modules.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 客户创建DTO
 */
@Data
public class CustomerCreateDTO {
    
    /**
     * 客户企业名称
     */
    @NotBlank(message = "客户名称不能为空")
    private String name;
    
    /**
     * 统一社会信用代码
     */
    private String creditCode;
    
    /**
     * 联系人姓名
     */
    @NotBlank(message = "联系人姓名不能为空")
    private String contactName;
    
    /**
     * 联系人手机
     */
    @NotBlank(message = "联系人手机不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String contactPhone;
    
    /**
     * 联系人邮箱
     */
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)$", message = "邮箱格式不正确")
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
}