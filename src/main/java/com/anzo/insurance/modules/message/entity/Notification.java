package com.anzo.insurance.modules.message.entity;

import com.anzo.insurance.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 通知实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notifications")
public class Notification extends BaseEntity {

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 通知类型
     * SYSTEM - 系统通知
     * REVIEW - 审核通知
     * BALANCE - 余额通知
     * WARNING - 预警通知
     * INFO - 信息通知
     */
    private String type;

    /**
     * 通知状态
     * UNREAD - 未读
     * READ - 已读
     */
    private String status;

    /**
     * 接收用户ID
     */
    private Long userId;

    /**
     * 企业ID
     */
    private Long enterpriseId;

    /**
     * 发送用户ID（如果是系统发送则为null）
     */
    private Long senderId;

    /**
     * 关联的业务ID（如保单ID、理赔ID等）
     */
    private Long relatedId;

    /**
     * 关联的业务类型
     */
    private String relatedType;

    /**
     * 优先级
     * LOW - 低
     * NORMAL - 普通
     * HIGH - 高
     * URGENT - 紧急
     */
    private String priority;

    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;

    /**
     * 阅读时间
     */
    private LocalDateTime readAt;

    /**
     * 额外数据（JSON格式）
     */
    private String extraData;

    /**
     * 通知类型枚举
     */
    public enum Type {
        SYSTEM("系统通知"),
        REVIEW("审核通知"),
        BALANCE("余额通知"),
        WARNING("预警通知"),
        INFO("信息通知");

        private final String description;

        Type(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public static Type fromValue(String value) {
            for (Type type : Type.values()) {
                if (type.name().equals(value)) {
                    return type;
                }
            }
            return INFO;
        }
    }

    /**
     * 通知状态枚举
     */
    public enum Status {
        UNREAD("未读"),
        READ("已读");

        private final String description;

        Status(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 优先级枚举
     */
    public enum Priority {
        LOW("低"),
        NORMAL("普通"),
        HIGH("高"),
        URGENT("紧急");

        private final String description;

        Priority(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 关联业务类型枚举
     */
    public enum RelatedType {
        POLICY("保单"),
        CLAIM("理赔"),
        ENTERPRISE("企业"),
        USER("用户"),
        FINANCE("财务"),
        APPLICATION("投保申请"),
        SYSTEM("系统");

        private final String description;

        RelatedType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 辅助方法

    /**
     * 获取通知类型描述
     */
    public String getTypeDescription() {
        try {
            return Type.valueOf(type).getDescription();
        } catch (IllegalArgumentException e) {
            return "未知类型";
        }
    }

    /**
     * 获取通知状态描述
     */
    public String getStatusDescription() {
        try {
            return Status.valueOf(status).getDescription();
        } catch (IllegalArgumentException e) {
            return "未知状态";
        }
    }

    /**
     * 获取优先级描述
     */
    public String getPriorityDescription() {
        try {
            return Priority.valueOf(priority).getDescription();
        } catch (IllegalArgumentException e) {
            return "普通";
        }
    }

    /**
     * 获取关联业务类型描述
     */
    public String getRelatedTypeDescription() {
        try {
            return RelatedType.valueOf(relatedType).getDescription();
        } catch (IllegalArgumentException e) {
            return "";
        }
    }

    /**
     * 通知是否未读
     */
    public boolean isUnread() {
        return Status.UNREAD.name().equals(status);
    }

    /**
     * 通知是否已读
     */
    public boolean isRead() {
        return Status.READ.name().equals(status);
    }

    /**
     * 是否是系统通知
     */
    public boolean isSystemNotification() {
        return Type.SYSTEM.name().equals(type);
    }

    /**
     * 是否是余额通知
     */
    public boolean isBalanceNotification() {
        return Type.BALANCE.name().equals(type);
    }

    /**
     * 是否是审核通知
     */
    public boolean isReviewNotification() {
        return Type.REVIEW.name().equals(type);
    }

    /**
     * 是否是紧急通知
     */
    public boolean isUrgent() {
        return Priority.URGENT.name().equals(priority);
    }

    /**
     * 是否是高优先级通知
     */
    public boolean isHighPriority() {
        return Priority.HIGH.name().equals(priority) || isUrgent();
    }

    /**
     * 通知是否已过期
     */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * 通知是否有效（未过期）
     */
    public boolean isValid() {
        return !isExpired();
    }

    /**
     * 获取通知摘要（用于列表显示）
     */
    public String getSummary() {
        if (content == null || content.length() <= 50) {
            return content;
        }
        return content.substring(0, 50) + "...";
    }

    /**
     * 标记为已读
     */
    public void markAsRead() {
        this.status = Status.READ.name();
        this.readAt = LocalDateTime.now();
    }
}