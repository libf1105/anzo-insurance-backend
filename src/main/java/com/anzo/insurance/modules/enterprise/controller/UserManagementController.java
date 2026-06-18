package com.anzo.insurance.modules.enterprise.controller;

import com.anzo.insurance.common.response.ApiResponse;
import com.anzo.insurance.modules.auth.entity.User;
import com.anzo.insurance.modules.enterprise.dto.UserCreateDTO;
import com.anzo.insurance.modules.enterprise.dto.UserUpdateDTO;
import com.anzo.insurance.modules.enterprise.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器（企业内用户管理）
 */
@Tag(name = "用户管理", description = "企业内用户管理接口")
@RestController
@RequestMapping("/enterprise/{enterpriseId}/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;

    @Operation(summary = "获取企业用户列表")
    @GetMapping
    @PreAuthorize("@enterprisePermissionService.hasEnterpriseAccess(#enterpriseId)")
    public ApiResponse<Page<User>> getEnterpriseUsers(
            @Parameter(description = "企业ID") @PathVariable String enterpriseId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sort,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") String direction) {
        
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortDirection, sort));
        
        Page<User> users = userManagementService.getEnterpriseUsers(enterpriseId, pageable);
        return ApiResponse.success(users);
    }

    @Operation(summary = "搜索企业用户")
    @GetMapping("/search")
    @PreAuthorize("@enterprisePermissionService.hasEnterpriseAccess(#enterpriseId)")
    public ApiResponse<Page<User>> searchEnterpriseUsers(
            @Parameter(description = "企业ID") @PathVariable String enterpriseId,
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer size) {
        
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<User> users = userManagementService.searchEnterpriseUsers(enterpriseId, keyword, pageable);
        return ApiResponse.success(users);
    }

    @Operation(summary = "创建企业用户")
    @PostMapping
    @PreAuthorize("@enterprisePermissionService.hasAdminAccess(#enterpriseId)")
    public ApiResponse<User> createEnterpriseUser(
            @Parameter(description = "企业ID") @PathVariable String enterpriseId,
            @Valid @RequestBody UserCreateDTO createDTO) {
        
        User user = userManagementService.createEnterpriseUser(enterpriseId, createDTO);
        return ApiResponse.success(user);
    }

    @Operation(summary = "获取用户信息")
    @GetMapping("/{userId}")
    @PreAuthorize("@enterprisePermissionService.hasEnterpriseAccess(#enterpriseId)")
    public ApiResponse<User> getUser(
            @Parameter(description = "企业ID") @PathVariable String enterpriseId,
            @Parameter(description = "用户ID") @PathVariable String userId) {
        
        User user = userManagementService.getUser(enterpriseId, userId);
        return ApiResponse.success(user);
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/{userId}")
    @PreAuthorize("@enterprisePermissionService.hasAdminAccess(#enterpriseId) or @enterprisePermissionService.isSelf(#enterpriseId, #userId)")
    public ApiResponse<Void> updateUser(
            @Parameter(description = "企业ID") @PathVariable String enterpriseId,
            @Parameter(description = "用户ID") @PathVariable String userId,
            @Valid @RequestBody UserUpdateDTO updateDTO) {
        
        userManagementService.updateUser(enterpriseId, userId, updateDTO);
        return ApiResponse.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{userId}")
    @PreAuthorize("@enterprisePermissionService.hasAdminAccess(#enterpriseId)")
    public ApiResponse<Void> deleteUser(
            @Parameter(description = "企业ID") @PathVariable String enterpriseId,
            @Parameter(description = "用户ID") @PathVariable String userId) {
        
        userManagementService.deleteUser(enterpriseId, userId);
        return ApiResponse.success();
    }

    @Operation(summary = "启用用户")
    @PostMapping("/{userId}/enable")
    @PreAuthorize("@enterprisePermissionService.hasAdminAccess(#enterpriseId)")
    public ApiResponse<Void> enableUser(
            @Parameter(description = "企业ID") @PathVariable String enterpriseId,
            @Parameter(description = "用户ID") @PathVariable String userId) {
        
        userManagementService.enableUser(enterpriseId, userId);
        return ApiResponse.success();
    }

    @Operation(summary = "禁用用户")
    @PostMapping("/{userId}/disable")
    @PreAuthorize("@enterprisePermissionService.hasAdminAccess(#enterpriseId)")
    public ApiResponse<Void> disableUser(
            @Parameter(description = "企业ID") @PathVariable String enterpriseId,
            @Parameter(description = "用户ID") @PathVariable String userId) {
        
        userManagementService.disableUser(enterpriseId, userId);
        return ApiResponse.success();
    }

    @Operation(summary = "重置用户密码")
    @PostMapping("/{userId}/reset-password")
    @PreAuthorize("@enterprisePermissionService.hasAdminAccess(#enterpriseId) or @enterprisePermissionService.isSelf(#enterpriseId, #userId)")
    public ApiResponse<Void> resetPassword(
            @Parameter(description = "企业ID") @PathVariable String enterpriseId,
            @Parameter(description = "用户ID") @PathVariable String userId,
            @Parameter(description = "新密码") @RequestParam String newPassword) {
        
        userManagementService.resetPassword(enterpriseId, userId, newPassword);
        return ApiResponse.success();
    }

    @Operation(summary = "获取用户统计信息")
    @GetMapping("/statistics")
    @PreAuthorize("@enterprisePermissionService.hasEnterpriseAccess(#enterpriseId)")
    public ApiResponse<UserManagementService.UserStatistics> getUserStatistics(
            @Parameter(description = "企业ID") @PathVariable String enterpriseId) {
        
        UserManagementService.UserStatistics statistics = userManagementService.getUserStatistics(enterpriseId);
        return ApiResponse.success(statistics);
    }
}