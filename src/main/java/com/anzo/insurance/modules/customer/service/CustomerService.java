package com.anzo.insurance.modules.customer.service;

import com.anzo.insurance.modules.customer.dto.*;
import com.anzo.insurance.modules.customer.entity.Customer;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Map;
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
    CustomerResponseDTO updateCustomer(Long customerId, CustomerUpdateDTO dto);
    
    /**
     * 获取客户详情
     */
    CustomerResponseDTO getCustomer(Long customerId);
    
    /**
     * 删除客户
     */
    void deleteCustomer(Long customerId);
    
    /**
     * 批量删除客户
     */
    void batchDeleteCustomers(List<Long> customerIds);
    
    /**
     * 分页查询客户
     */
    Page<CustomerResponseDTO> queryCustomers(CustomerQueryDTO queryDTO);

    /**
     * 查询客户列表（不分页，用于导出）
     */
    List<CustomerResponseDTO> listCustomers(CustomerQueryDTO queryDTO);

    /**
     * 获取客户统计
     */
    Map<String, Object> getCustomerStatistics();
    
    /**
     * 根据企业ID查询客户列表
     */
    List<CustomerResponseDTO> getCustomersByEnterprise(Long enterpriseId);
    
    /**
     * 启用/禁用客户
     */
    CustomerResponseDTO toggleCustomerStatus(Long customerId, String status);
    
    /**
     * 根据ID获取客户实体
     */
    Customer getCustomerEntity(Long customerId);
}
