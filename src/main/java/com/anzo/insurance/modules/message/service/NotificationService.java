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
    NotificationDTO getNotificationById(Long notificationId);

    /**
     * 获取用户通知列表
     */
    List<NotificationDTO> getUserNotifications(Long userId);

    /**
     * 获取企业通知列表
     */
    List<NotificationDTO> getEnterpriseNotifications(Long enterpriseId);

    /**
     * 分页查询通知
     */
    Page<NotificationDTO> queryNotifications(NotificationQueryDTO queryDTO, Pageable pageable);

    /**
     * 标记通知为已读
     */
    void markAsRead(Long notificationId);

    /**
     * 批量标记通知为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 批量标记企业通知为已读
     */
    void markAllAsReadByEnterprise(Long enterpriseId);

    /**
     * 删除通知（逻辑删除）
     */
    void deleteNotification(Long notificationId);

    /**
     * 批量删除通知
     */
    void batchDeleteNotifications(List<Long> notificationIds);

    /**
     * 获取未读通知数量
     */
    Integer getUnreadCount(Long userId);

    /**
     * 获取企业未读通知数量
     */
    Integer getEnterpriseUnreadCount(Long enterpriseId);

    /**
     * 发送系统通知
     */
    void sendSystemNotification(String title, String content, Long enterpriseId, List<Long> userIds);

    /**
     * 发送余额变动通知
     */
    void sendBalanceNotification(Long enterpriseId, Long userId, String changeType, 
                                 String amount, String balanceAfter, String remark);

    /**
     * 发送审核通知
     */
    void sendReviewNotification(Long enterpriseId, Long userId, String reviewType,
                               String reviewResult, String targetName, String remark);

    /**
     * 发送预警通知
     */
    void sendWarningNotification(Long enterpriseId, Long userId, String warningType,
                                 String warningContent, String actionRequired);

    /**
     * 获取最新通知
     */
    List<NotificationDTO> getLatestNotifications(Long userId, int limit);

    /**
     * 获取企业最新通知
     */
    List<NotificationDTO> getEnterpriseLatestNotifications(Long enterpriseId, int limit);

    /**
     * 清理过期通知
     */
    void cleanupExpiredNotifications();

    /**
     * 获取通知统计信息
     */
    NotificationStatistics getNotificationStatistics(Long userId);

    /**
     * 获取企业通知统计信息
     */
    NotificationStatistics getEnterpriseNotificationStatistics(Long enterpriseId);

    /**
     * 发送保单相关通知
     */
    void sendPolicyNotification(Long enterpriseId, Long userId, Long policyId,
                               String notificationType, String content);

    /**
     * 发送理赔相关通知
     */
    void sendClaimNotification(Long enterpriseId, Long userId, Long claimId,
                              String notificationType, String content);

    /**
     * 发送企业状态变更通知
     */
    void sendEnterpriseStatusNotification(Long enterpriseId, String oldStatus, 
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