package com.anzo.insurance.modules.auth.service;

import com.anzo.insurance.modules.auth.dto.LoginDTO;
import com.anzo.insurance.modules.auth.dto.RegisterDTO;
import com.anzo.insurance.modules.auth.dto.AuthResponseDTO;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 用户登录
     */
    AuthResponseDTO login(LoginDTO loginDTO);
    
    /**
     * 用户注册
     */
    AuthResponseDTO register(RegisterDTO registerDTO);
    
    /**
     * 用户登出
     */
    void logout();
    
    /**
     * 刷新令牌
     */
    AuthResponseDTO refreshToken(String refreshToken);
    
    /**
     * 获取当前用户信息
     */
    AuthResponseDTO getCurrentUser();
}