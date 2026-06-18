package com.anzo.insurance.modules.auth.service;

import com.anzo.insurance.modules.auth.dto.LoginDTO;
import com.anzo.insurance.modules.auth.dto.RegisterDTO;
import com.anzo.insurance.modules.auth.dto.AuthResponseDTO;
import com.anzo.insurance.modules.auth.dto.ChangePasswordDTO;
import com.anzo.insurance.modules.auth.dto.ResetPasswordDTO;
import com.anzo.insurance.modules.auth.dto.UpdateProfileDTO;

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

    /**
     * 更新当前用户资料
     */
    AuthResponseDTO updateProfile(UpdateProfileDTO updateProfileDTO);

    /**
     * 修改当前用户密码
     */
    void changePassword(ChangePasswordDTO changePasswordDTO);

    /**
     * 找回密码
     */
    void resetPassword(ResetPasswordDTO resetPasswordDTO);
}
