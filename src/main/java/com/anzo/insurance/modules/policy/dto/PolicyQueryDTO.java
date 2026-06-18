package com.anzo.insurance.modules.policy.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 保单查询DTO
 */
@Data
public class PolicyQueryDTO {

    /**
     * 保单号/投保人名称/货物名称（模糊查询）
     */
    private String keyword;

    /**
     * 保单状态
     */
    private Integer status;

    /**
     * 保险公司ID
     */
    private String insurerId;

    /**
     * 贸易方向
     */
    private Integer tradeDirection;

    /**
     * 运输方式
     */
    private Integer transportMode;

    /**
     * 投保日期开始
     */
    private LocalDate applicationDateStart;

    /**
     * 投保日期结束
     */
    private LocalDate applicationDateEnd;

    /**
     * 保单生效日期开始
     */
    private LocalDate effectiveDateStart;

    /**
     * 保单生效日期结束
     */
    private LocalDate effectiveDateEnd;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 20;

    /**
     * 排序字段
     */
    private String orderBy = "application_date";

    /**
     * 排序方向：asc/desc
     */
    private String orderDirection = "desc";

    /**
     * 企业ID（用于权限控制）
     */
    private String enterpriseId;

    /**
     * 投保人用户ID（用于权限控制）
     */
    private String applicantUserId;
}