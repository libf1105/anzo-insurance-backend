package com.anzo.insurance.modules.enterprise.controller;

import com.anzo.insurance.common.response.ApiResponse;
import com.anzo.insurance.modules.auth.entity.Enterprise;
import com.anzo.insurance.modules.enterprise.dto.EnterpriseQueryDTO;
import com.anzo.insurance.modules.enterprise.dto.EnterpriseRechargeDTO;
import com.anzo.insurance.modules.enterprise.dto.EnterpriseReviewDTO;
import com.anzo.insurance.modules.enterprise.dto.EnterpriseUpdateDTO;
import com.anzo.insurance.modules.enterprise.service.EnterpriseService;
import com.anzo.insurance.modules.finance.dto.TransactionQueryDTO;
import com.anzo.insurance.modules.finance.dto.TransactionRecordDTO;
import com.anzo.insurance.modules.finance.dto.WalletDTO;
import com.anzo.insurance.modules.finance.service.TransactionRecordService;
import com.anzo.insurance.modules.finance.service.WalletService;
import com.anzo.insurance.modules.message.service.NotificationService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 企业管理控制器
 */
@Tag(name = "企业管理", description = "企业相关管理接口")
@RestController
@RequestMapping("/enterprise")
@RequiredArgsConstructor
public class EnterpriseController {

    private final EnterpriseService enterpriseService;
    private final WalletService walletService;
    private final TransactionRecordService transactionRecordService;
    private final NotificationService notificationService;

    @Operation(summary = "获取企业信息")
    @GetMapping("/{enterpriseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Enterprise> getEnterprise(
            @Parameter(description = "企业ID") @PathVariable Long enterpriseId) {
        Enterprise enterprise = enterpriseService.getEnterpriseById(enterpriseId);
        return ApiResponse.success(enterprise);
    }

    @Operation(summary = "获取当前用户的企业信息")
    @GetMapping("/info")
    public ApiResponse<Enterprise> getCurrentEnterprise() {
        Enterprise enterprise = enterpriseService.getCurrentEnterprise();
        return ApiResponse.success(enterprise);
    }

    @Operation(summary = "更新企业信息")
    @PutMapping("/{enterpriseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Void> updateEnterprise(
            @Parameter(description = "企业ID") @PathVariable Long enterpriseId,
            @Valid @RequestBody EnterpriseUpdateDTO updateDTO) {
        enterpriseService.updateEnterprise(enterpriseId, updateDTO);
        return ApiResponse.success();
    }

    @Operation(summary = "更新当前用户的企业信息")
    @PutMapping("/info")
    public ApiResponse<Void> updateCurrentEnterprise(@Valid @RequestBody EnterpriseUpdateDTO updateDTO) {
        enterpriseService.updateCurrentEnterprise(updateDTO);
        return ApiResponse.success();
    }

    @Operation(summary = "分页查询企业列表")
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Page<Enterprise>> queryEnterprises(
            @Valid EnterpriseQueryDTO queryDTO,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sort,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") String direction) {
        
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortDirection, sort));
        
        Page<Enterprise> enterprises = enterpriseService.queryEnterprises(queryDTO, pageable);
        return ApiResponse.success(enterprises);
    }

    @Operation(summary = "获取待审核企业列表")
    @GetMapping("/pending-review")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<List<Enterprise>> getPendingReviewEnterprises() {
        List<Enterprise> enterprises = enterpriseService.getPendingReviewEnterprises();
        return ApiResponse.success(enterprises);
    }

    @Operation(summary = "审核企业")
    @PostMapping("/review")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Void> reviewEnterprise(@Valid @RequestBody EnterpriseReviewDTO reviewDTO) {
        enterpriseService.reviewEnterprise(reviewDTO);
        return ApiResponse.success();
    }

    @Operation(summary = "启用企业")
    @PostMapping("/{enterpriseId}/enable")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Void> enableEnterprise(
            @Parameter(description = "企业ID") @PathVariable Long enterpriseId) {
        enterpriseService.enableEnterprise(enterpriseId);
        return ApiResponse.success();
    }

    @Operation(summary = "禁用企业")
    @PostMapping("/{enterpriseId}/disable")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Void> disableEnterprise(
            @Parameter(description = "企业ID") @PathVariable Long enterpriseId,
            @Parameter(description = "禁用原因") @RequestParam String reason) {
        enterpriseService.disableEnterprise(enterpriseId, reason);
        return ApiResponse.success();
    }

    @Operation(summary = "获取企业统计信息")
    @GetMapping("/{enterpriseId}/statistics")
    public ApiResponse<EnterpriseService.EnterpriseStatistics> getEnterpriseStatistics(
            @Parameter(description = "企业ID") @PathVariable Long enterpriseId) {
        EnterpriseService.EnterpriseStatistics statistics = enterpriseService.getEnterpriseStatistics(enterpriseId);
        return ApiResponse.success(statistics);
    }

    @Operation(summary = "检查企业状态")
    @GetMapping("/{enterpriseId}/status")
    public ApiResponse<Boolean> checkEnterpriseStatus(
            @Parameter(description = "企业ID") @PathVariable Long enterpriseId) {
        boolean isActive = enterpriseService.checkEnterpriseStatus(enterpriseId);
        return ApiResponse.success(isActive);
    }

    @Operation(summary = "获取企业余额信息")
    @GetMapping("/{enterpriseId}/balance")
    public ApiResponse<Object> getEnterpriseBalance(
            @Parameter(description = "企业ID") @PathVariable Long enterpriseId) {
        Enterprise enterprise = enterpriseService.getEnterpriseById(enterpriseId);
        
        return ApiResponse.success(Map.of(
            "balance", enterprise.getBalance() != null ? enterprise.getBalance().toString() : "0",
            "frozenBalance", enterprise.getFrozenBalance() != null ? enterprise.getFrozenBalance().toString() : "0",
            "totalRecharged", enterprise.getTotalRecharged() != null ? enterprise.getTotalRecharged().toString() : "0",
            "totalConsumed", enterprise.getTotalConsumed() != null ? enterprise.getTotalConsumed().toString() : "0"
        ));
    }

    // ============ 新增接口 ============

    @Operation(summary = "获取企业详细信息")
    @GetMapping("/detail")
    public ApiResponse<Map<String, Object>> getEnterpriseDetail() {
        Enterprise enterprise = enterpriseService.getCurrentEnterprise();
        
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", enterprise.getId());
        detail.put("name", enterprise.getName());
        detail.put("code", enterprise.getCreditCode());
        detail.put("legalPerson", enterprise.getContactName());
        detail.put("contactPhone", enterprise.getContactPhone());
        detail.put("email", enterprise.getContactEmail());
        detail.put("address", enterprise.getAddress());
        detail.put("description", enterprise.getDescription());
        detail.put("status", enterprise.getStatus());
        detail.put("balance", enterprise.getBalance() != null ? enterprise.getBalance() : BigDecimal.ZERO);
        detail.put("reviewRemark", enterprise.getReviewRemark());
        detail.put("reviewedAt", enterprise.getReviewAt());
        detail.put("createdAt", enterprise.getCreatedAt());
        detail.put("updatedAt", enterprise.getUpdatedAt());
        
        return ApiResponse.success(detail);
    }

    @Operation(summary = "更新企业状态")
    @PutMapping("/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Void> updateEnterpriseStatus(
            @RequestParam @NotBlank(message = "状态不能为空") String status) {
        // 这里需要根据业务逻辑实现状态更新
        // 目前先返回成功
        return ApiResponse.success();
    }

    @Operation(summary = "企业充值")
    @PostMapping("/recharge")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'FINANCE')")
    public ApiResponse<Map<String, Object>> rechargeBalance(
            @Valid @RequestBody EnterpriseRechargeDTO rechargeDTO) {
        
        Enterprise enterprise = enterpriseService.getCurrentEnterprise();

        WalletDTO wallet = walletService.getWalletByEnterpriseId(enterprise.getId());
        walletService.recharge(wallet.getId(), rechargeDTO.getAmount(), 2, null, rechargeDTO.getRemark());
        enterpriseService.updateEnterpriseBalance(
                enterprise.getId(), 
                rechargeDTO.getAmount().toString(),
                "RECHARGE"
        );
        enterprise = enterpriseService.getEnterpriseById(enterprise.getId());

        notificationService.sendBalanceNotification(
                enterprise.getId(),
                null,
                "RECHARGE",
                rechargeDTO.getAmount().toPlainString(),
                enterprise.getBalance() != null ? enterprise.getBalance().toPlainString() : "0",
                rechargeDTO.getRemark()
        );
        
        Map<String, Object> result = new HashMap<>();
        result.put("balance", enterprise.getBalance());
        result.put("success", true);
        result.put("message", "充值成功");
        
        return ApiResponse.success(result);
    }

    @Operation(summary = "获取余额变动记录")
    @GetMapping("/balance/history")
    public ApiResponse<Map<String, Object>> getBalanceHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Enterprise enterprise = enterpriseService.getCurrentEnterprise();
        TransactionQueryDTO queryDTO = new TransactionQueryDTO();
        queryDTO.setEnterpriseId(enterprise.getId());
        queryDTO.setPageNum(page);
        queryDTO.setPageSize(pageSize != null ? pageSize : (size != null ? size : 20));
        queryDTO.setStartTime(startDate);
        queryDTO.setEndTime(endDate);

        IPage<TransactionRecordDTO> transactionPage = transactionRecordService.queryTransactionPage(queryDTO);
        List<Map<String, Object>> history = transactionPage.getRecords().stream().map(record -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", record.getId());
            item.put("type", mapTransactionType(record.getTransactionType()));
            item.put("amount", record.getAmount());
            item.put("balanceAfter", record.getAfterBalance());
            item.put("remark", record.getRemark() != null ? record.getRemark() : record.getBusinessDesc());
            item.put("createdAt", record.getCreateTime());
            return item;
        }).toList();

        Map<String, Object> result = new HashMap<>();
        result.put("data", history);
        result.put("page", Map.of(
                "page", (int) transactionPage.getCurrent(),
                "pageSize", (int) transactionPage.getSize(),
                "total", transactionPage.getTotal(),
                "totalPages", transactionPage.getPages()
        ));
        return ApiResponse.success(result);
    }

    @Operation(summary = "获取审核状态")
    @GetMapping("/review-status")
    public ApiResponse<Map<String, Object>> getReviewStatus() {
        Enterprise enterprise = enterpriseService.getCurrentEnterprise();
        
        Map<String, Object> status = new HashMap<>();
        status.put("status", enterprise.getStatus());
        
        if ("REJECTED".equals(enterprise.getStatus())) {
            status.put("remark", enterprise.getReviewRemark());
        }
        
        if (enterprise.getReviewAt() != null) {
            status.put("reviewedAt", enterprise.getReviewAt());
        }
        
        return ApiResponse.success(status);
    }

    @Operation(summary = "获取企业基本信息")
    @GetMapping("/basic-info")
    public ApiResponse<Map<String, Object>> getBasicInfo() {
        Enterprise enterprise = enterpriseService.getCurrentEnterprise();
        
        Map<String, Object> info = new HashMap<>();
        info.put("name", enterprise.getName());
        info.put("code", enterprise.getCreditCode());
        info.put("legalPerson", enterprise.getContactName());
        info.put("contactPhone", enterprise.getContactPhone());
        info.put("email", enterprise.getContactEmail());
        info.put("address", enterprise.getAddress());
        info.put("description", enterprise.getDescription());
        info.put("status", enterprise.getStatus());
        
        return ApiResponse.success(info);
    }

    private String mapTransactionType(Integer transactionType) {
        if (transactionType == null) {
            return "ADJUST";
        }
        switch (transactionType) {
            case 1:
                return "RECHARGE";
            case 2:
                return "CONSUME";
            case 4:
                return "REFUND";
            default:
                return "ADJUST";
        }
    }
}
