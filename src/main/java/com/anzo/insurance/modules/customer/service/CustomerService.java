package com.anzo.insurance.modules.customer.service;

import com.anzo.insurance.modules.customer.dto.*;
import com.anzo.insurance.modules.customer.entity.Customer;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 客户服务接口
 */
public interface CustomerService {
    
    /**
     * 创建客户
     */
    CustomerResponseDTO createCustomer(CustomerCreateDTO dto);
    
    /**
     * 更新客户
     */
    CustomerResponseDTO updateCustomer(String customerId, CustomerUpdateDTO dto);
    
    /**
     * 获取客户详情
     */
    CustomerResponseDTO getCustomer(String customerId);
    
    /**
     * 删除客户
     */
    void deleteCustomer(String customerId);
    
    /**
     * 批量删除客户
     */
    void batchDeleteCustomers(List<String> customerIds);
    
    /**
     * 分页查询客户
     */
    Page<CustomerResponseDTO> queryCustomers(CustomerQueryDTO queryDTO);
    
    /**
     * 根据企业ID查询客户列表
     */
    List<CustomerResponseDTO> getCustomersByEnterprise(String enterpriseId);
    
    /**
     * 启用/禁用客户
     */
    CustomerResponseDTO toggleCustomerStatus(String customerId, String status);
    
    /**
     * 根据ID获取客户实体
     */
    Customer getCustomerEntity(String customerId);
}