package com.anzo.insurance.modules.message.repository;

import com.anzo.insurance.modules.message.entity.Notification;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知数据访问接口
 */
@Mapper
public interface NotificationRepository extends BaseMapper<Notification> {

    /**
     * 根据用户ID查询通知
     */
    @Select("SELECT * FROM notifications WHERE user_id = #{userId} " +
            "AND deleted = false ORDER BY created_at DESC LIMIT #{limit}")
    List<Notification> findByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 根据企业ID查询通知
     */
    @Select("SELECT * FROM notifications WHERE enterprise_id = #{enterpriseId} " +
            "AND deleted = false ORDER BY created_at DESC")
    List<Notification> findByEnterpriseId(@Param("enterpriseId") Long enterpriseId);

    /**
     * 根据用户ID和状态查询通知
     */
    @Select("SELECT * FROM notifications WHERE user_id = #{userId} " +
            "AND status = #{status} AND deleted = false ORDER BY created_at DESC")
    List<Notification> findByUserIdAndStatus(@Param("userId") Long userId,
                                              @Param("status") String status);

    /**
     * 根据用户ID获取未读通知数量
     */
    @Select("SELECT COUNT(*) FROM notifications WHERE user_id = #{userId} " +
            "AND status = 'UNREAD' AND deleted = false " +
            "AND (expires_at IS NULL OR expires_at > #{now})")
    Integer getUnreadCountByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * 根据企业ID获取未读通知数量
     */
    @Select("SELECT COUNT(*) FROM notifications WHERE enterprise_id = #{enterpriseId} " +
            "AND status = 'UNREAD' AND deleted = false " +
            "AND (expires_at IS NULL OR expires_at > #{now})")
    Integer getUnreadCountByEnterpriseId(@Param("enterpriseId") Long enterpriseId,
                                         @Param("now") LocalDateTime now);

    /**
     * 标记通知为已读
     */
    @Update("UPDATE notifications SET status = 'READ', read_at = #{readAt} " +
            "WHERE id = #{id} AND deleted = false")
    int markAsRead(@Param("id") Long id, @Param("readAt") LocalDateTime readAt);

    /**
     * 批量标记通知为已读
     */
    @Update("UPDATE notifications SET status = 'READ', read_at = #{readAt} " +
            "WHERE user_id = #{userId} AND status = 'UNREAD' AND deleted = false")
    int markAllAsReadByUserId(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);

    /**
     * 批量标记企业通知为已读
     */
    @Update("UPDATE notifications SET status = 'READ', read_at = #{readAt} " +
            "WHERE enterprise_id = #{enterpriseId} AND status = 'UNREAD' AND deleted = false")
    int markAllAsReadByEnterpriseId(@Param("enterpriseId") Long enterpriseId,
                                     @Param("readAt") LocalDateTime readAt);

    /**
     * 查询用户最新通知
     */
    @Select("SELECT * FROM notifications WHERE user_id = #{userId} AND deleted = false " +
            "AND (expires_at IS NULL OR expires_at > #{now}) " +
            "ORDER BY CASE priority " +
            "  WHEN 'URGENT' THEN 1 " +
            "  WHEN 'HIGH' THEN 2 " +
            "  WHEN 'NORMAL' THEN 3 " +
            "  WHEN 'LOW' THEN 4 " +
            "END, created_at DESC LIMIT #{limit}")
    List<Notification> findLatestByUserId(@Param("userId") Long userId,
                                          @Param("limit") int limit,
                                          @Param("now") LocalDateTime now);

    /**
     * 查询企业最新通知
     */
    @Select("SELECT * FROM notifications WHERE enterprise_id = #{enterpriseId} AND deleted = false " +
            "AND (expires_at IS NULL OR expires_at > #{now}) " +
            "ORDER BY CASE priority " +
            "  WHEN 'URGENT' THEN 1 " +
            "  WHEN 'HIGH' THEN 2 " +
            "  WHEN 'NORMAL' THEN 3 " +
            "  WHEN 'LOW' THEN 4 " +
            "END, created_at DESC")
    List<Notification> findByEnterpriseIdWithPriority(@Param("enterpriseId") Long enterpriseId,
                                                      @Param("now") LocalDateTime now);

    /**
     * 删除过期通知
     */
    @Update("UPDATE notifications SET deleted = true, updated_at = #{now} " +
            "WHERE expires_at < #{now} AND status = 'READ' AND deleted = false")
    int deleteExpiredNotifications(@Param("now") LocalDateTime now);

    /**
     * 获取通知统计信息
     */
    @Select("SELECT " +
            "COUNT(CASE WHEN status = 'UNREAD' THEN 1 END) as unread_count, " +
            "COUNT(CASE WHEN type = 'SYSTEM' THEN 1 END) as system_count, " +
            "COUNT(CASE WHEN type = 'REVIEW' THEN 1 END) as review_count, " +
            "COUNT(CASE WHEN type = 'BALANCE' THEN 1 END) as balance_count, " +
            "COUNT(CASE WHEN type = 'WARNING' THEN 1 END) as warning_count, " +
            "COUNT(*) as total_count " +
            "FROM notifications " +
            "WHERE user_id = #{userId} AND deleted = false " +
            "AND (expires_at IS NULL OR expires_at > #{now})")
    NotificationStatistics getNotificationStatistics(@Param("userId") Long userId,
                                                     @Param("now") LocalDateTime now);

    /**
     * 通知统计信息
     */
    interface NotificationStatistics {
        Integer getUnreadCount();
        Integer getSystemCount();
        Integer getReviewCount();
        Integer getBalanceCount();
        Integer getWarningCount();
        Integer getTotalCount();
    }
}