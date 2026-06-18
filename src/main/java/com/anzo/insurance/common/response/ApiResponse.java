package com.anzo.insurance.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 统一API响应格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> implements Serializable {
    
    private boolean success;
    private String code;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private PageInfo page;
    
    @Data
    @Builder
    public static class PageInfo {
        private Integer page;
        private Integer pageSize;
        private Long total;
        private Integer totalPages;
    }
    
    /**
     * 成功响应（无数据）
     */
    public static ApiResponse<Void> success() {
        return ApiResponse.<Void>builder()
            .success(true)
            .code("SUCCESS")
            .message("操作成功")
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    /**
     * 成功响应（有数据）
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .code("SUCCESS")
            .message("操作成功")
            .data(data)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    /**
     * 成功响应（分页数据）
     */
    public static <T> ApiResponse<Page<T>> success(Page<T> page) {
        PageInfo pageInfo = PageInfo.builder()
            .page(page.getNumber() + 1)
            .pageSize(page.getSize())
            .total(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .build();
        
        return ApiResponse.<Page<T>>builder()
            .success(true)
            .code("SUCCESS")
            .message("查询成功")
            .data(page)
            .page(pageInfo)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    /**
     * 失败响应
     */
    public static ApiResponse<Void> fail(String code, String message) {
        return ApiResponse.<Void>builder()
            .success(false)
            .code(code)
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    /**
     * 失败响应（带数据）
     */
    public static <T> ApiResponse<T> fail(String code, String message, T data) {
        return ApiResponse.<T>builder()
            .success(false)
            .code(code)
            .message(message)
            .data(data)
            .timestamp(LocalDateTime.now())
            .build();
    }
}