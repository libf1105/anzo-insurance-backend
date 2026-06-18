package com.anzo.insurance.modules.auth.controller;

import com.anzo.insurance.common.response.ApiResponse;
import com.anzo.insurance.modules.auth.dto.ChangePasswordDTO;
import com.anzo.insurance.modules.auth.dto.LoginDTO;
import com.anzo.insurance.modules.auth.dto.RegisterDTO;
import com.anzo.insurance.modules.auth.dto.AuthResponseDTO;
import com.anzo.insurance.modules.auth.dto.ResetPasswordDTO;
import com.anzo.insurance.modules.auth.dto.UpdateProfileDTO;
import com.anzo.insurance.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "用户认证相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ApiResponse<AuthResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        AuthResponseDTO response = authService.login(loginDTO);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public ApiResponse<AuthResponseDTO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        AuthResponseDTO response = authService.register(registerDTO);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.success();
    }
    
    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh-token")
    public ApiResponse<AuthResponseDTO> refreshToken(@RequestParam String refreshToken) {
        AuthResponseDTO response = authService.refreshToken(refreshToken);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public ApiResponse<AuthResponseDTO> getCurrentUser() {
        AuthResponseDTO response = authService.getCurrentUser();
        return ApiResponse.success(response);
    }

    @Operation(summary = "更新当前用户资料")
    @PutMapping("/profile")
    public ApiResponse<AuthResponseDTO> updateProfile(@Valid @RequestBody UpdateProfileDTO updateProfileDTO) {
        return ApiResponse.success(authService.updateProfile(updateProfileDTO));
    }

    @Operation(summary = "修改密码")
    @PutMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        authService.changePassword(changePasswordDTO);
        return ApiResponse.success();
    }

    @Operation(summary = "找回密码")
    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO) {
        authService.resetPassword(resetPasswordDTO);
        return ApiResponse.success();
    }
}
