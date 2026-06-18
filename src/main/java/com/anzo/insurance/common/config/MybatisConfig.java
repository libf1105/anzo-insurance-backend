package com.anzo.insurance.common.config;

import com.anzo.insurance.common.security.SecurityUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus配置
 */
@Configuration
public class MybatisConfig implements MetaObjectHandler {
    
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 分页插件
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInnerInterceptor.setMaxLimit(1000L); // 最大单页限制数量
        paginationInnerInterceptor.setOverflow(true); // 溢出总页数后是否进行处理
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        
        return interceptor;
    }
    
    @Override
    public void insertFill(MetaObject metaObject) {
        String userId = getCurrentUserId();
        
        this.strictInsertFill(metaObject, "createdBy", String.class, userId);
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedBy", String.class, userId);
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
    
    @Override
    public void updateFill(MetaObject metaObject) {
        String userId = getCurrentUserId();
        
        this.strictUpdateFill(metaObject, "updatedBy", String.class, userId);
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
    
    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        try {
            String userId = SecurityUtil.getCurrentUserId();
            if (userId != null && !userId.isBlank()) {
                return userId;
            }
        } catch (Exception e) {
            // 忽略异常，返回默认值
        }
        
        return "system";
    }
}
