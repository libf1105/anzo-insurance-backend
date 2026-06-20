package com.anzo.insurance.common.exception;

import lombok.Getter;

/**
 * 错误码定义
 */
@Getter
public enum ErrorCode {
    
    // 系统级错误
    SUCCESS("SUCCESS", "操作成功"),
    SYSTEM_ERROR("SYSTEM_ERROR", "系统异常"),
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", "服务不可用"),
    PARAMETER_ERROR("PARAMETER_ERROR", "参数错误"),
    PARAM_ERROR("PARAM_ERROR", "参数错误"), // 兼容旧代码
    
    // 认证授权错误
    UNAUTHORIZED("UNAUTHORIZED", "未授权访问"),
    FORBIDDEN("FORBIDDEN", "权限不足"),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "Token已过期"),
    TOKEN_INVALID("TOKEN_INVALID", "Token无效"),
    MAX_LOGIN_FAILURES("MAX_LOGIN_FAILURES", "登录失败次数过多"),
    
    // 业务级错误
    BUSINESS_ERROR("BUSINESS_ERROR", "业务异常"),
    DATA_NOT_FOUND("DATA_NOT_FOUND", "数据不存在"),
    DATA_DUPLICATE("DATA_DUPLICATE", "数据重复"),
    OPERATION_FAILED("OPERATION_FAILED", "操作失败"),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "资源不存在"),
    PERMISSION_DENIED("PERMISSION_DENIED", "权限不足"),
    FEATURE_NOT_IMPLEMENTED("FEATURE_NOT_IMPLEMENTED", "功能未实现"),
    
    // 认证授权业务错误
    USER_NOT_FOUND("USER_NOT_FOUND", "用户不存在"),
    USER_EXISTS("USER_EXISTS", "用户已存在"),
    USER_DISABLED("USER_DISABLED", "用户已被禁用"),
    PASSWORD_ERROR("PASSWORD_ERROR", "密码错误"),
    VERIFICATION_CODE_ERROR("VERIFICATION_CODE_ERROR", "验证码错误"),
    
    // 企业相关错误
    ENTERPRISE_NOT_FOUND("ENTERPRISE_NOT_FOUND", "企业不存在"),
    ENTERPRISE_EXISTS("ENTERPRISE_EXISTS", "企业已存在"),
    ENTERPRISE_DISABLED("ENTERPRISE_DISABLED", "企业已被禁用"),
    ENTERPRISE_REVIEW_PENDING("ENTERPRISE_REVIEW_PENDING", "企业待审核"),
    ENTERPRISE_REJECTED("ENTERPRISE_REJECTED", "企业审核未通过"),
    ENTERPRISE_INACTIVE("ENTERPRISE_INACTIVE", "企业未激活"),
    
    // 客户管理错误
    CUSTOMER_NOT_FOUND("CUSTOMER_NOT_FOUND", "客户不存在"),
    CUSTOMER_EXISTS("CUSTOMER_EXISTS", "客户已存在"),
    CUSTOMER_DISABLED("CUSTOMER_DISABLED", "客户已被禁用"),
    CUSTOMER_CREDIT_CODE_EXISTS("CUSTOMER_CREDIT_CODE_EXISTS", "信用代码已存在"),
    CUSTOMER_CREATE_FAILED("CUSTOMER_CREATE_FAILED", "客户创建失败"),
    CUSTOMER_UPDATE_FAILED("CUSTOMER_UPDATE_FAILED", "客户更新失败"),
    CUSTOMER_DELETE_FAILED("CUSTOMER_DELETE_FAILED", "客户删除失败"),
    
    // 投保业务错误
    INSUFFICIENT_BALANCE("INSUFFICIENT_BALANCE", "余额不足"),
    DUPLICATE_APPLICATION("DUPLICATE_APPLICATION", "重复投保"),
    POLICY_LIMIT_EXCEEDED("POLICY_LIMIT_EXCEEDED", "投保限额超限"),
    RATE_NOT_FOUND("RATE_NOT_FOUND", "费率配置不存在"),
    INSURER_UNAVAILABLE("INSURER_UNAVAILABLE", "保司服务不可用"),
    
    // 财务错误
    PAYMENT_FAILED("PAYMENT_FAILED", "支付失败"),
    INVOICE_LIMIT_EXCEEDED("INVOICE_LIMIT_EXCEEDED", "开票限额超限"),
    RECHARGE_FAILED("RECHARGE_FAILED", "充值失败"),
    
    // 理赔错误
    CLAIM_MATERIAL_INCOMPLETE("CLAIM_MATERIAL_INCOMPLETE", "理赔材料不完整"),
    CLAIM_PERIOD_EXPIRED("CLAIM_PERIOD_EXPIRED", "理赔时效已过"),
    CLAIM_STATUS_INVALID("CLAIM_STATUS_INVALID", "理赔状态无效"),
    
    // 文件上传错误
    FILE_UPLOAD_FAILED("FILE_UPLOAD_FAILED", "文件上传失败"),
    FILE_TYPE_NOT_SUPPORTED("FILE_TYPE_NOT_SUPPORTED", "文件类型不支持"),
    FILE_SIZE_EXCEEDED("FILE_SIZE_EXCEEDED", "文件大小超过限制"),
    FILE_TOO_LARGE("FILE_TOO_LARGE", "文件大小超过限制"),
    FILE_NOT_FOUND("FILE_NOT_FOUND", "文件不存在"),
    FILE_EXISTS("FILE_EXISTS", "文件已存在"),
    FILE_OPERATION_ERROR("FILE_OPERATION_ERROR", "文件操作失败"),
    
    // 数据导入错误
    EXCEL_PARSE_ERROR("EXCEL_PARSE_ERROR", "Excel解析错误"),
    DATA_VALIDATION_FAILED("DATA_VALIDATION_FAILED", "数据校验失败");
    
    private final String code;
    private final String message;
    
    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public static ErrorCode fromCode(String code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        return SYSTEM_ERROR;
    }
}