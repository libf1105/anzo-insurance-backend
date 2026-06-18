package com.anzo.insurance.modules.auth;

import com.anzo.insurance.modules.auth.dto.LoginDTO;
import com.anzo.insurance.modules.auth.dto.RegisterDTO;
import com.anzo.insurance.modules.auth.dto.AuthResponseDTO;
import com.anzo.insurance.modules.auth.service.AuthService;
import com.anzo.insurance.modules.auth.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 认证服务测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Test
    void testLoginSuccess() {
        // 准备测试数据（假设数据库中已有测试用户）
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("admin@anzo.com");
        loginDTO.setPassword("Admin123456");
        loginDTO.setRememberMe(false);

        // 执行登录
        AuthResponseDTO response = authService.login(loginDTO);

        // 验证结果
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertNotNull(response.getUser());
        assertEquals("admin@anzo.com", response.getUser().getUsername());
        assertNotNull(response.getEnterprise());
    }

    @Test
    void testLoginWithInvalidPassword() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("admin@anzo.com");
        loginDTO.setPassword("wrongpassword");

        // 验证抛出密码错误异常
        assertThrows(Exception.class, () -> {
            authService.login(loginDTO);
        });
    }

    @Test
    void testLoginWithNonExistentUser() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("nonexistent@example.com");
        loginDTO.setPassword("password123");

        // 验证抛出用户不存在异常
        assertThrows(Exception.class, () -> {
            authService.login(loginDTO);
        });
    }

    @Test
    void testRegisterNewEnterprise() {
        RegisterDTO registerDTO = new RegisterDTO();
        // 企业信息
        registerDTO.setEnterpriseName("测试科技有限公司");
        registerDTO.setCreditCode("91110000100000000X");
        registerDTO.setContactName("张测试");
        registerDTO.setContactPhone("13800000002");
        registerDTO.setContactEmail("test@example.com");
        
        // 管理员信息
        registerDTO.setUsername("testadmin@example.com");
        registerDTO.setPassword("Test@123456");
        registerDTO.setRealName("测试管理员");
        registerDTO.setPhone("13800000003");
        registerDTO.setEmail("testadmin@example.com");

        // 执行注册
        AuthResponseDTO response = authService.register(registerDTO);

        // 验证结果
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertNotNull(response.getUser());
        assertEquals("testadmin@example.com", response.getUser().getUsername());
        assertEquals("ADMIN", response.getUser().getRole());
        assertNotNull(response.getEnterprise());
        assertEquals("测试科技有限公司", response.getEnterprise().getName());
        assertEquals("PENDING_REVIEW", response.getEnterprise().getStatus());
    }

    @Test
    void testRegisterWithExistingCreditCode() {
        // 假设数据库中已有信用代码为91310000100000000X的企业
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setEnterpriseName("重复企业有限公司");
        registerDTO.setCreditCode("91310000100000000X"); // 已存在的信用代码
        registerDTO.setContactName("李重复");
        registerDTO.setContactPhone("13800000004");
        
        registerDTO.setUsername("duplicate@example.com");
        registerDTO.setPassword("Test@123456");
        registerDTO.setRealName("重复用户");
        registerDTO.setPhone("13800000005");

        // 验证抛出企业已存在异常
        assertThrows(Exception.class, () -> {
            authService.register(registerDTO);
        });
    }

    @Test
    void testRegisterWithExistingUsername() {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setEnterpriseName("新企业有限公司");
        registerDTO.setCreditCode("91110000100000001X");
        registerDTO.setContactName("王新");
        registerDTO.setContactPhone("13800000006");
        
        registerDTO.setUsername("admin@anzo.com"); // 已存在的用户名
        registerDTO.setPassword("Test@123456");
        registerDTO.setRealName("新用户");
        registerDTO.setPhone("13800000007");

        // 验证抛出用户已存在异常
        assertThrows(Exception.class, () -> {
            authService.register(registerDTO);
        });
    }

    @Test
    void testJwtTokenValidation() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("admin@anzo.com");
        loginDTO.setPassword("Admin123456");

        AuthResponseDTO response = authService.login(loginDTO);
        String token = response.getToken();

        // 验证JWT令牌
        assertNotNull(jwtService.extractUsername(token));
        assertNotNull(jwtService.extractUserId(token));
        assertNotNull(jwtService.extractEnterpriseId(token));
        assertNotNull(jwtService.extractRole(token));
        assertTrue(jwtService.getExpirationTime() > 0);
    }

    @Test
    void testRefreshToken() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("admin@anzo.com");
        loginDTO.setPassword("Admin123456");

        AuthResponseDTO loginResponse = authService.login(loginDTO);
        String refreshToken = loginResponse.getRefreshToken();

        // 执行刷新令牌
        AuthResponseDTO refreshResponse = authService.refreshToken(refreshToken);

        // 验证结果
        assertNotNull(refreshResponse);
        assertNotNull(refreshResponse.getToken());
        assertEquals("Bearer", refreshResponse.getTokenType());
        assertNotNull(refreshResponse.getUser());
        assertEquals("admin@anzo.com", refreshResponse.getUser().getUsername());
    }

    @Test
    void testGetCurrentUser() {
        // 先登录
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("admin@anzo.com");
        loginDTO.setPassword("Admin123456");
        authService.login(loginDTO);

        // 获取当前用户信息
        AuthResponseDTO response = authService.getCurrentUser();

        // 验证结果
        assertNotNull(response);
        assertNotNull(response.getUser());
        assertEquals("admin@anzo.com", response.getUser().getUsername());
        assertNotNull(response.getEnterprise());
    }

    @Test
    void testLogout() {
        // 先登录
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("admin@anzo.com");
        loginDTO.setPassword("Admin123456");
        authService.login(loginDTO);

        // 执行登出
        authService.logout();

        // 验证登出后无法获取当前用户（应该抛出异常）
        assertThrows(Exception.class, () -> {
            authService.getCurrentUser();
        });
    }
}