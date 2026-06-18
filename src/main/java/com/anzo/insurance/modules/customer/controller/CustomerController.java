package com.anzo.insurance.modules.customer.controller;

import com.anzo.insurance.common.response.ApiResponse;
import com.anzo.insurance.modules.customer.dto.*;
import com.anzo.insurance.modules.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "客户管理", description = "客户管理相关接口")
public class CustomerController {
    
    private final CustomerService customerService;
    
    @PostMapping
    @Operation(summary = "创建客户")
    public ApiResponse<CustomerResponseDTO> createCustomer(@Valid @RequestBody CustomerCreateDTO dto) {
        log.info("创建客户: {}", dto);
        CustomerResponseDTO result = customerService.createCustomer(dto);
        return ApiResponse.success("客户创建成功", result);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新客户")
    public ApiResponse<CustomerResponseDTO> updateCustomer(
            @PathVariable String id,
            @Valid @RequestBody CustomerUpdateDTO dto) {
        log.info("更新客户: id={}, data={}", id, dto);
        CustomerResponseDTO result = customerService.updateCustomer(id, dto);
        return ApiResponse.success("客户更新成功", result);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取客户详情")
    public ApiResponse<CustomerResponseDTO> getCustomer(@PathVariable String id) {
        log.info("获取客户详情: id={}", id);
        CustomerResponseDTO result = customerService.getCustomer(id);
        return ApiResponse.success("查询成功", result);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除客户")
    public ApiResponse<Void> deleteCustomer(@PathVariable String id) {
        log.info("删除客户: id={}", id);
        customerService.deleteCustomer(id);
        return ApiResponse.success("客户删除成功");
    }
    
    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除客户")
    public ApiResponse<Void> batchDeleteCustomers(@RequestBody List<String> ids) {
        log.info("批量删除客户: ids={}", ids);
        customerService.batchDeleteCustomers(ids);
        return ApiResponse.success("批量删除成功");
    }
    
    @GetMapping
    @Operation(summary = "分页查询客户")
    public ApiResponse<Object> queryCustomers(@Valid CustomerQueryDTO queryDTO) {
        log.info("查询客户列表: {}", queryDTO);
        return ApiResponse.success("查询成功", customerService.queryCustomers(queryDTO));
    }
    
    @GetMapping("/enterprise/{enterpriseId}")
    @Operation(summary = "根据企业ID查询客户列表")
    public ApiResponse<List<CustomerResponseDTO>> getCustomersByEnterprise(
            @PathVariable String enterpriseId) {
        log.info("根据企业ID查询客户: enterpriseId={}", enterpriseId);
        List<CustomerResponseDTO> result = customerService.getCustomersByEnterprise(enterpriseId);
        return ApiResponse.success("查询成功", result);
    }
    
    @PutMapping("/{id}/status")
    @Operation(summary = "启用/禁用客户")
    public ApiResponse<CustomerResponseDTO> toggleCustomerStatus(
            @PathVariable String id,
            @Parameter(description = "状态: ACTIVE/DISABLED") @RequestParam String status) {
        log.info("修改客户状态: id={}, status={}", id, status);
        CustomerResponseDTO result = customerService.toggleCustomerStatus(id, status);
        return ApiResponse.success("状态修改成功", result);
    }
}