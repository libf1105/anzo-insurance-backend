package com.anzo.insurance.modules.message.controller;

import com.anzo.insurance.common.response.ApiResponse;
import com.anzo.insurance.modules.message.dto.NotificationDTO;
import com.anzo.insurance.modules.message.dto.NotificationQueryDTO;
import com.anzo.insurance.modules.message.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * 通知管理控制器
 */
@RestController
@RequestMapping("/api/enterprise/notifications")
@Tag(name = "通知管理", description = "企业通知管理接口")
@Slf4j
@RequiredArgsConstructor
@Validated
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @Operation(summary = "创建通知")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<NotificationDTO> createNotification(@Valid @RequestBody NotificationDTO notificationDTO) {
        log.info("收到创建通知请求: title={}, type={}", notificationDTO.getTitle(), notificationDTO.getType());
        
        NotificationDTO result = notificationService.createNotification(notificationDTO);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取通知详情")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR', 'FINANCE')")
    public ApiResponse<NotificationDTO> getNotification(@PathVariable String id) {
        NotificationDTO notification = notificationService.getNotificationById(id);
        return ApiResponse.success(notification);
    }

    @GetMapping("/user")
    @Operation(summary = "获取用户通知列表")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR', 'FINANCE')")
    public ApiResponse<List<NotificationDTO>> getUserNotifications() {
        // 从token中获取用户ID（服务层处理）
        List<NotificationDTO> notifications = notificationService.getUserNotifications(null);
        return ApiResponse.success(notifications);
    }

    @GetMapping("/enterprise")
    @Operation(summary = "获取企业通知列表")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR', 'FINANCE')")
    public ApiResponse<List<NotificationDTO>> getEnterpriseNotifications() {
        // 从token中获取企业ID（服务层处理）
        List<NotificationDTO> notifications = notificationService.getEnterpriseNotifications(null);
        return ApiResponse.success(notifications);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询通知")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR', 'FINANCE')")
    public ApiResponse<Page<NotificationDTO>> queryNotifications(
            @Valid NotificationQueryDTO queryDTO,
            @PageableDefault(page = 0, size = 20) Pageable pageable) {
        
        Page<NotificationDTO> page = notificationService.queryNotifications(queryDTO, pageable);
        return ApiResponse.success(page);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记通知为已读")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR', 'FINANCE')")
    public ApiResponse<Void> markAsRead(@PathVariable String id) {
        notificationService.markAsRead(id);
        return ApiResponse.success();
    }

    @PutMapping("/mark-all-read")
    @Operation(summary = "标记所有通知为已读")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR', 'FINANCE')")
    public ApiResponse<Void> markAllAsRead() {
        // 从token中获取用户ID（服务层处理）
        notificationService.markAllAsRead(null);
        return ApiResponse.success();
    }

    @PutMapping("/enterprise/mark-all-read")
    @Operation(summary = "标记企业所有通知为已读")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Void> markAllAsReadByEnterprise() {
        // 从token中获取企业ID（服务层处理）
        notificationService.markAllAsReadByEnterprise(null);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Void> deleteNotification(@PathVariable String id) {
        notificationService.deleteNotification(id);
        return ApiResponse.success();
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除通知")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Void> batchDeleteNotifications(@RequestParam List<String> ids) {
        notificationService.batchDeleteNotifications(ids);
        return ApiResponse.success();
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取未读通知数量")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR', 'FINANCE')")
    public ApiResponse<Integer> getUnreadCount() {
        Integer count = notificationService.getUnreadCount(null);
        return ApiResponse.success(count);
    }

    @GetMapping("/enterprise/unread-count")
    @Operation(summary = "获取企业未读通知数量")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Integer> getEnterpriseUnreadCount() {
        Integer count = notificationService.getEnterpriseUnreadCount(null);
        return ApiResponse.success(count);
    }

    @PostMapping("/system")
    @Operation(summary = "发送系统通知")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Void> sendSystemNotification(
            @RequestParam @NotBlank(message = "标题不能为空") String title,
            @RequestParam @NotBlank(message = "内容不能为空") String content,
            @RequestParam(required = false) String enterpriseId,
            @RequestParam(required = false) List<String> userIds) {
        
        notificationService.sendSystemNotification(title, content, enterpriseId, userIds);
        return ApiResponse.success();
    }

    @GetMapping("/latest")
    @Operation(summary = "获取最新通知")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR', 'FINANCE')")
    public ApiResponse<List<NotificationDTO>> getLatestNotifications(
            @RequestParam(defaultValue = "10") int limit) {
        
        List<NotificationDTO> notifications = notificationService.getLatestNotifications(null, limit);
        return ApiResponse.success(notifications);
    }

    @GetMapping("/enterprise/latest")
    @Operation(summary = "获取企业最新通知")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<List<NotificationDTO>> getEnterpriseLatestNotifications(
            @RequestParam(defaultValue = "20") int limit) {
        
        List<NotificationDTO> notifications = notificationService.getEnterpriseLatestNotifications(null, limit);
        return ApiResponse.success(notifications);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取通知统计信息")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR', 'FINANCE')")
    public ApiResponse<NotificationService.NotificationStatistics> getNotificationStatistics() {
        NotificationService.NotificationStatistics stats = notificationService.getNotificationStatistics(null);
        return ApiResponse.success(stats);
    }

    @GetMapping("/enterprise/statistics")
    @Operation(summary = "获取企业通知统计信息")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<NotificationService.NotificationStatistics> getEnterpriseNotificationStatistics() {
        NotificationService.NotificationStatistics stats = notificationService.getEnterpriseNotificationStatistics(null);
        return ApiResponse.success(stats);
    }

    @PostMapping("/balance")
    @Operation(summary = "发送余额变动通知")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'FINANCE')")
    public ApiResponse<Void> sendBalanceNotification(
            @RequestParam @NotBlank(message = "变动类型不能为空") String changeType,
            @RequestParam @NotBlank(message = "金额不能为空") String amount,
            @RequestParam @NotBlank(message = "变动后余额不能为空") String balanceAfter,
            @RequestParam(required = false) String remark,
            @RequestParam(required = false) String enterpriseId,
            @RequestParam(required = false) String userId) {
        
        notificationService.sendBalanceNotification(enterpriseId, userId, changeType, amount, balanceAfter, remark);
        return ApiResponse.success();
    }

    @PostMapping("/review")
    @Operation(summary = "发送审核通知")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Void> sendReviewNotification(
            @RequestParam @NotBlank(message = "审核类型不能为空") String reviewType,
            @RequestParam @NotBlank(message = "审核结果不能为空") String reviewResult,
            @RequestParam @NotBlank(message = "审核对象名称不能为空") String targetName,
            @RequestParam(required = false) String remark,
            @RequestParam(required = false) String enterpriseId,
            @RequestParam(required = false) String userId) {
        
        notificationService.sendReviewNotification(enterpriseId, userId, reviewType, reviewResult, targetName, remark);
        return ApiResponse.success();
    }

    @PostMapping("/warning")
    @Operation(summary = "发送预警通知")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Void> sendWarningNotification(
            @RequestParam @NotBlank(message = "预警类型不能为空") String warningType,
            @RequestParam @NotBlank(message = "预警内容不能为空") String warningContent,
            @RequestParam @NotBlank(message = "需要采取的措施不能为空") String actionRequired,
            @RequestParam(required = false) String enterpriseId,
            @RequestParam(required = false) String userId) {
        
        notificationService.sendWarningNotification(enterpriseId, userId, warningType, warningContent, actionRequired);
        return ApiResponse.success();
    }

    @PostMapping("/policy")
    @Operation(summary = "发送保单通知")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR')")
    public ApiResponse<Void> sendPolicyNotification(
            @RequestParam @NotBlank(message = "保单ID不能为空") String policyId,
            @RequestParam @NotBlank(message = "通知类型不能为空") String notificationType,
            @RequestParam @NotBlank(message = "通知内容不能为空") String content,
            @RequestParam(required = false) String enterpriseId,
            @RequestParam(required = false) String userId) {
        
        notificationService.sendPolicyNotification(enterpriseId, userId, policyId, notificationType, content);
        return ApiResponse.success();
    }

    @PostMapping("/claim")
    @Operation(summary = "发送理赔通知")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR')")
    public ApiResponse<Void> sendClaimNotification(
            @RequestParam @NotBlank(message = "理赔ID不能为空") String claimId,
            @RequestParam @NotBlank(message = "通知类型不能为空") String notificationType,
            @RequestParam @NotBlank(message = "通知内容不能为空") String content,
            @RequestParam(required = false) String enterpriseId,
            @RequestParam(required = false) String userId) {
        
        notificationService.sendClaimNotification(enterpriseId, userId, claimId, notificationType, content);
        return ApiResponse.success();
    }

    @PostMapping("/enterprise-status")
    @Operation(summary = "发送企业状态变更通知")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Void> sendEnterpriseStatusNotification(
            @RequestParam @NotBlank(message = "旧状态不能为空") String oldStatus,
            @RequestParam @NotBlank(message = "新状态不能为空") String newStatus,
            @RequestParam(required = false) String remark,
            @RequestParam(required = false) String enterpriseId) {
        
        notificationService.sendEnterpriseStatusNotification(enterpriseId, oldStatus, newStatus, remark);
        return ApiResponse.success();
    }

    @GetMapping("/types")
    @Operation(summary = "获取通知类型列表")
    public ApiResponse<List<TypeInfo>> getNotificationTypes() {
        List<TypeInfo> types = List.of(
                new TypeInfo("SYSTEM", "系统通知", "el-icon-s-promotion", "#409eff"),
                new TypeInfo("REVIEW", "审核通知", "el-icon-s-opportunity", "#e6a23c"),
                new TypeInfo("BALANCE", "余额通知", "el-icon-money", "#67c23a"),
                new TypeInfo("WARNING", "预警通知", "el-icon-warning-outline", "#f56c6c"),
                new TypeInfo("INFO", "信息通知", "el-icon-bell", "#909399")
        );
        
        return ApiResponse.success(types);
    }

    @GetMapping("/priorities")
    @Operation(summary = "获取优先级列表")
    public ApiResponse<List<PriorityInfo>> getPriorities() {
        List<PriorityInfo> priorities = List.of(
                new PriorityInfo("LOW", "低", "#909399"),
                new PriorityInfo("NORMAL", "普通", "#67c23a"),
                new PriorityInfo("HIGH", "高", "#e6a23c"),
                new PriorityInfo("URGENT", "紧急", "#f56c6c")
        );
        
        return ApiResponse.success(priorities);
    }

    @GetMapping("/related-types")
    @Operation(summary = "获取关联业务类型列表")
    public ApiResponse<List<RelatedTypeInfo>> getRelatedTypes() {
        List<RelatedTypeInfo> relatedTypes = List.of(
                new RelatedTypeInfo("POLICY", "保单"),
                new RelatedTypeInfo("CLAIM", "理赔"),
                new RelatedTypeInfo("ENTERPRISE", "企业"),
                new RelatedTypeInfo("USER", "用户"),
                new RelatedTypeInfo("FINANCE", "财务"),
                new RelatedTypeInfo("APPLICATION", "投保申请"),
                new RelatedTypeInfo("SYSTEM", "系统")
        );
        
        return ApiResponse.success(relatedTypes);
    }

    /**
     * 类型信息
     */
    public static class TypeInfo {
        private final String code;
        private final String name;
        private final String icon;
        private final String color;

        public TypeInfo(String code, String name, String icon, String color) {
            this.code = code;
            this.name = name;
            this.icon = icon;
            this.color = color;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public String getIcon() {
            return icon;
        }

        public String getColor() {
            return color;
        }
    }

    /**
     * 优先级信息
     */
    public static class PriorityInfo {
        private final String code;
        private final String name;
        private final String color;

        public PriorityInfo(String code, String name, String color) {
            this.code = code;
            this.name = name;
            this.color = color;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public String getColor() {
            return color;
        }
    }

    /**
     * 关联业务类型信息
     */
    public static class RelatedTypeInfo {
        private final String code;
        private final String name;

        public RelatedTypeInfo(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }
    }
}