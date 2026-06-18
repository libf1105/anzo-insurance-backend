package com.anzo.insurance.modules.message.service;

import com.anzo.insurance.modules.message.dto.NotificationDTO;
import com.anzo.insurance.modules.message.dto.NotificationQueryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知服务接口
 */
public interface NotificationService {

    /**
     * 创建通知
     */
    NotificationDTO createNotification(NotificationDTO notificationDTO);

    /**
     * 根据ID获取通知
     */
    NotificationDTO getNotificationById(String notificationId);

    /**
     * 获取用户通知列表
     */
    List<NotificationDTO> getUserNotifications(String userId);

    /**
     * 获取企业通知列表
     */
    List<NotificationDTO> getEnterpriseNotifications(String enterpriseId);

    /**
     * 分页查询通知
     */
    Page<NotificationDTO> queryNotifications(NotificationQueryDTO queryDTO, Pageable pageable);

    /**
     * 标记通知为已读
     */
    void markAsRead(String notificationId);

    /**
     * 批量标记通知为已读
     */
    void markAllAsRead(String userId);

    /**
     * 批量标记企业通知为已读
     */
    void markAllAsReadByEnterprise(String enterpriseId);

    /**
     * 删除通知（逻辑删除）
     */
    void deleteNotification(String notificationId);

    /**
     * 批量删除通知
     */
    void batchDeleteNotifications(List<String> notificationIds);

    /**
     * 获取未读通知数量
     */
    Integer getUnreadCount(String userId);

    /**
     * 获取企业未读通知数量
     */
    Integer getEnterpriseUnreadCount(String enterpriseId);

    /**
     * 发送系统通知
     */
    void sendSystemNotification(String title, String content, String enterpriseId, List<String> userIds);

    /**
     * 发送余额变动通知
     */
    void sendBalanceNotification(String enterpriseId, String userId, String changeType, 
                                 String amount, String balanceAfter, String remark);

    /**
     * 发送审核通知
     */
    void sendReviewNotification(String enterpriseId, String userId, String reviewType,
                               String reviewResult, String targetName, String remark);

    /**
     * 发送预警通知
     */
    void sendWarningNotification(String enterpriseId, String userId, String warningType,
                                 String warningContent, String actionRequired);

    /**
     * 获取最新通知
     */
    List<NotificationDTO> getLatestNotifications(String userId, int limit);

    /**
     * 获取企业最新通知
     */
    List<NotificationDTO> getEnterpriseLatestNotifications(String enterpriseId, int limit);

    /**
     * 清理过期通知
     */
    void cleanupExpiredNotifications();

    /**
     * 获取通知统计信息
     */
    NotificationStatistics getNotificationStatistics(String userId);

    /**
     * 获取企业通知统计信息
     */
    NotificationStatistics getEnterpriseNotificationStatistics(String enterpriseId);

    /**
     * 发送保单相关通知
     */
    void sendPolicyNotification(String enterpriseId, String userId, String policyId,
                               String notificationType, String content);

    /**
     * 发送理赔相关通知
     */
    void sendClaimNotification(String enterpriseId, String userId, String claimId,
                              String notificationType, String content);

    /**
     * 发送企业状态变更通知
     */
    void sendEnterpriseStatusNotification(String enterpriseId, String oldStatus, 
                                         String newStatus, String remark);

    /**
     * 通知统计接口
     */
    interface NotificationStatistics {
        Integer getUnreadCount();
        Integer getTotalCount();
        Integer getSystemCount();
        Integer getReviewCount();
        Integer getBalanceCount();
        Integer getWarningCount();
        Integer getInfoCount();
        Integer getTodayCount();
        Integer getThisWeekCount();
        Integer getThisMonthCount();
    }
}