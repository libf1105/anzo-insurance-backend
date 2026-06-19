package com.anzo.insurance.modules.message.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知查询DTO
 */
@Data
public class NotificationQueryDTO {

    /**
     * 通知类型
     */
    private String type;

    /**
     * 通知状态
     */
    private String status;

    /**
     * 优先级
     */
    private String priority;

    /**
     * 关联业务类型
     */
    private String relatedType;

    /**
     * 关键词（标题或内容搜索）
     */
    private String keyword;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 是否只查询未过期通知
     */
    private Boolean onlyValid = true;

    /**
     * 是否包含已删除
     */
    private Boolean includeDeleted = false;

    /**
     * 企业ID
     */
    private Long enterpriseId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 排序字段
     */
    private String sortBy = "createdAt";

    /**
     * 排序方向
     */
    private String sortDirection = "DESC";
}