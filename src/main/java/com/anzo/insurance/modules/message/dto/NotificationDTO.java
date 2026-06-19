package com.anzo.insurance.modules.message.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知数据传输对象
 */
@Data
@Accessors(chain = true)
public class NotificationDTO {

    /**
     * 通知ID
     */
    private Long id;

    /**
     * 通知标题
     */
    @NotBlank(message = "通知标题不能为空")
    private String title;

    /**
     * 通知内容
     */
    @NotBlank(message = "通知内容不能为空")
    private String content;

    /**
     * 通知类型
     * SYSTEM - 系统通知
     * REVIEW - 审核通知
     * BALANCE - 余额通知
     * WARNING - 预警通知
     * INFO - 信息通知
     */
    @NotBlank(message = "通知类型不能为空")
    private String type;

    /**
     * 通知状态
     * UNREAD - 未读
     * READ - 已读
     */
    private String status;

    /**
     * 优先级
     * LOW - 低
     * NORMAL - 普通
     * HIGH - 高
     * URGENT - 紧急
     */
    private String priority;

    /**
     * 接收用户ID列表（为空表示发送给企业所有用户）
     */
    private List<Long> userIds;

    /**
     * 企业ID（为空表示从当前用户获取）
     */
    private Long enterpriseId;

    /**
     * 关联的业务ID
     */
    private Long relatedId;

    /**
     * 关联的业务类型
     */
    private String relatedType;

    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;

    /**
     * 额外数据（JSON格式）
     */
    private String extraData;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 阅读时间
     */
    private LocalDateTime readAt;

    /**
     * 发送用户ID（系统发送为null）
     */
    private Long senderId;

    /**
     * 发送用户姓名（用于显示）
     */
    private String senderName;

    /**
     * 接收用户姓名（用于显示）
     */
    private String receiverName;

    /**
     * 企业名称（用于显示）
     */
    private String enterpriseName;

    // 辅助字段
    private Boolean isUnread;
    private Boolean isUrgent;
    private Boolean isHighPriority;
    private String summary; // 内容摘要
    private String typeDescription; // 类型描述
    private String statusDescription; // 状态描述
    private String priorityDescription; // 优先级描述
    private String relatedTypeDescription; // 关联类型描述
    private Boolean expired; // 是否过期
    private Boolean valid; // 是否有效（未过期）
}