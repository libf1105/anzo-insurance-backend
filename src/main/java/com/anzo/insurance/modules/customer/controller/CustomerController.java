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

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

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
        return ApiResponse.success(result);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新客户")
    public ApiResponse<CustomerResponseDTO> updateCustomer(
            @PathVariable String id,
            @Valid @RequestBody CustomerUpdateDTO dto) {
        log.info("更新客户: id={}, data={}", id, dto);
        CustomerResponseDTO result = customerService.updateCustomer(id, dto);
        return ApiResponse.success(result);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取客户详情")
    public ApiResponse<CustomerResponseDTO> getCustomer(@PathVariable String id) {
        log.info("获取客户详情: id={}", id);
        CustomerResponseDTO result = customerService.getCustomer(id);
        return ApiResponse.success(result);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除客户")
    public ApiResponse<Void> deleteCustomer(@PathVariable String id) {
        log.info("删除客户: id={}", id);
        customerService.deleteCustomer(id);
        return ApiResponse.success();
    }
    
    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除客户")
    public ApiResponse<Void> batchDeleteCustomers(@RequestBody List<String> ids) {
        log.info("批量删除客户: ids={}", ids);
        customerService.batchDeleteCustomers(ids);
        return ApiResponse.success();
    }
    
    @GetMapping
    @Operation(summary = "分页查询客户")
    public ApiResponse<Object> queryCustomers(@Valid CustomerQueryDTO queryDTO) {
        log.info("查询客户列表: {}", queryDTO);
        return ApiResponse.success(customerService.queryCustomers(queryDTO));
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取客户统计")
    public ApiResponse<Map<String, Object>> getCustomerStatistics() {
        return ApiResponse.success(customerService.getCustomerStatistics());
    }

    @GetMapping("/export")
    @Operation(summary = "导出客户列表")
    public void exportCustomers(@Valid CustomerQueryDTO queryDTO, HttpServletResponse response) throws IOException {
        List<CustomerResponseDTO> customers = customerService.listCustomers(queryDTO);
        String fileName = URLEncoder.encode("customers.csv", StandardCharsets.UTF_8);
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
        response.getWriter().write('\uFEFF');
        response.getWriter().write("客户名称,统一社会信用代码,联系人,联系人手机,联系人邮箱,地址,国家,城市,状态,创建时间\n");
        for (CustomerResponseDTO customer : customers) {
            response.getWriter().write(buildCsvLine(customer));
        }
        response.getWriter().flush();
    }
    
    @GetMapping("/enterprise/{enterpriseId}")
    @Operation(summary = "根据企业ID查询客户列表")
    public ApiResponse<List<CustomerResponseDTO>> getCustomersByEnterprise(
            @PathVariable String enterpriseId) {
        log.info("根据企业ID查询客户: enterpriseId={}", enterpriseId);
        List<CustomerResponseDTO> result = customerService.getCustomersByEnterprise(enterpriseId);
        return ApiResponse.success(result);
    }
    
    @PutMapping("/{id}/status")
    @Operation(summary = "启用/禁用客户")
    public ApiResponse<CustomerResponseDTO> toggleCustomerStatus(
            @PathVariable String id,
            @Parameter(description = "状态: ACTIVE/DISABLED") @RequestParam String status) {
        log.info("修改客户状态: id={}, status={}", id, status);
        CustomerResponseDTO result = customerService.toggleCustomerStatus(id, status);
        return ApiResponse.success(result);
    }

    private String buildCsvLine(CustomerResponseDTO customer) {
        return String.join(",",
                escapeCsv(customer.getName()),
                escapeCsv(customer.getCreditCode()),
                escapeCsv(customer.getContactName()),
                escapeCsv(customer.getContactPhone()),
                escapeCsv(customer.getContactEmail()),
                escapeCsv(customer.getAddress()),
                escapeCsv(customer.getCountry()),
                escapeCsv(customer.getCity()),
                escapeCsv(customer.getStatus()),
                escapeCsv(customer.getCreatedAt() == null ? "" : customer.getCreatedAt().toString())
        ) + "\n";
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
