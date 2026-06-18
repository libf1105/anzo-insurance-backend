package com.anzo.insurance.modules.customer.service.impl;

import com.anzo.insurance.common.exception.BusinessException;
import com.anzo.insurance.common.exception.ErrorCode;
import com.anzo.insurance.common.security.SecurityUtil;
import com.anzo.insurance.modules.customer.dto.*;
import com.anzo.insurance.modules.customer.entity.Customer;
import com.anzo.insurance.modules.customer.repository.CustomerMapper;
import com.anzo.insurance.modules.customer.service.CustomerService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 客户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    
    private final CustomerMapper customerMapper;
    
    @Override
    @Transactional
    public CustomerResponseDTO createCustomer(CustomerCreateDTO dto) {
        // 检查信用代码是否已存在
        if (dto.getCreditCode() != null && !dto.getCreditCode().isEmpty()) {
            boolean exists = customerMapper.existsByCreditCode(SecurityUtil.getCurrentEnterpriseId(), dto.getCreditCode());
            if (exists) {
                throw new BusinessException(ErrorCode.CUSTOMER_CREDIT_CODE_EXISTS);
            }
        }
        
        Customer customer = new Customer();
        BeanUtils.copyProperties(dto, customer);
        customer.setEnterpriseId(SecurityUtil.getCurrentEnterpriseId());
        customer.setStatus("ACTIVE");
        customer.setCreatedBy(SecurityUtil.getCurrentUserId());
        
        int result = customerMapper.insert(customer);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.CUSTOMER_CREATE_FAILED);
        }
        
        return convertToResponseDTO(customer);
    }
    
    @Override
    @Transactional
    public CustomerResponseDTO updateCustomer(String customerId, CustomerUpdateDTO dto) {
        Customer customer = getCustomerEntity(customerId);
        
        // 检查信用代码是否已存在（排除当前客户）
        if (dto.getCreditCode() != null && !dto.getCreditCode().isEmpty() 
                && !dto.getCreditCode().equals(customer.getCreditCode())) {
            boolean exists = customerMapper.existsByCreditCode(SecurityUtil.getCurrentEnterpriseId(), dto.getCreditCode());
            if (exists) {
                throw new BusinessException(ErrorCode.CUSTOMER_CREDIT_CODE_EXISTS);
            }
        }
        
        BeanUtils.copyProperties(dto, customer, "id", "enterpriseId");
        customer.setUpdatedBy(SecurityUtil.getCurrentUserId());
        
        int result = customerMapper.updateById(customer);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.CUSTOMER_UPDATE_FAILED);
        }
        
        return convertToResponseDTO(customer);
    }
    
    @Override
    public CustomerResponseDTO getCustomer(String customerId) {
        Customer customer = getCustomerEntity(customerId);
        return convertToResponseDTO(customer);
    }
    
    @Override
    @Transactional
    public void deleteCustomer(String customerId) {
        Customer customer = getCustomerEntity(customerId);
        customer.setDeleted(true);
        customer.setUpdatedBy(SecurityUtil.getCurrentUserId());
        
        int result = customerMapper.updateById(customer);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.CUSTOMER_DELETE_FAILED);
        }
    }
    
    @Override
    @Transactional
    public void batchDeleteCustomers(List<String> customerIds) {
        for (String customerId : customerIds) {
            deleteCustomer(customerId);
        }
    }
    
    @Override
    public Page<CustomerResponseDTO> queryCustomers(CustomerQueryDTO queryDTO) {
        LambdaQueryWrapper<Customer> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Customer::getEnterpriseId, SecurityUtil.getCurrentEnterpriseId())
                    .eq(Customer::getDeleted, false);
        
        // 添加查询条件
        if (queryDTO.getName() != null && !queryDTO.getName().isEmpty()) {
            queryWrapper.like(Customer::getName, queryDTO.getName());
        }
        if (queryDTO.getCreditCode() != null && !queryDTO.getCreditCode().isEmpty()) {
            queryWrapper.eq(Customer::getCreditCode, queryDTO.getCreditCode());
        }
        if (queryDTO.getContactName() != null && !queryDTO.getContactName().isEmpty()) {
            queryWrapper.like(Customer::getContactName, queryDTO.getContactName());
        }
        if (queryDTO.getContactPhone() != null && !queryDTO.getContactPhone().isEmpty()) {
            queryWrapper.eq(Customer::getContactPhone, queryDTO.getContactPhone());
        }
        if (queryDTO.getStatus() != null && !queryDTO.getStatus().isEmpty()) {
            queryWrapper.eq(Customer::getStatus, queryDTO.getStatus());
        }
        
        queryWrapper.orderByDesc(Customer::getCreatedAt);
        
        Page<Customer> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        Page<Customer> customerPage = customerMapper.selectPage(page, queryWrapper);
        
        Page<CustomerResponseDTO> resultPage = new Page<>();
        BeanUtils.copyProperties(customerPage, resultPage);
        
        List<CustomerResponseDTO> records = customerPage.getRecords().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        
        resultPage.setRecords(records);
        return resultPage;
    }
    
    @Override
    public List<CustomerResponseDTO> getCustomersByEnterprise(String enterpriseId) {
        List<Customer> customers = customerMapper.findByEnterpriseId(enterpriseId);
        return customers.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public CustomerResponseDTO toggleCustomerStatus(String customerId, String status) {
        Customer customer = getCustomerEntity(customerId);
        customer.setStatus(status);
        customer.setUpdatedBy(SecurityUtil.getCurrentUserId());
        
        int result = customerMapper.updateById(customer);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.CUSTOMER_UPDATE_FAILED);
        }
        
        return convertToResponseDTO(customer);
    }
    
    @Override
    public Customer getCustomerEntity(String customerId) {
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null || customer.getDeleted()) {
            throw new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND);
        }
        
        // 检查权限：只能操作本企业的客户
        if (!customer.getEnterpriseId().equals(SecurityUtil.getCurrentEnterpriseId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        return customer;
    }
    
    private CustomerResponseDTO convertToResponseDTO(Customer customer) {
        CustomerResponseDTO dto = new CustomerResponseDTO();
        BeanUtils.copyProperties(customer, dto);
        dto.setCreatedAt(customer.getCreatedAt());
        dto.setUpdatedAt(customer.getUpdatedAt());
        return dto;
    }
}