package com.anzo.insurance.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisTokenUtil {
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";
    private static final String REFRESH_PREFIX = "jwt:refresh:";
    private static final String LOGIN_LIMIT_PREFIX = "jwt:login:limit:";
    private static final String TOKEN_VERSION_PREFIX = "jwt:version:";

    private final StringRedisTemplate redisTemplate;

    // ==================== 黑名单管理 ====================

    /**
     * 将 Token 加入黑名单（用于登出）
     */
    public void blacklistToken(String jti, long remainingSeconds) {
        String key = BLACKLIST_PREFIX + jti;
        redisTemplate.opsForValue().set(key, "1", remainingSeconds, TimeUnit.SECONDS);
        log.info("Token 已加入黑名单, jti={}", jti);
    }

    /**
     * 检查 Token 是否在黑名单中
     */
    public boolean isBlacklisted(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + jti));
    }

    // ==================== Refresh Token 管理 ====================

    /**
     * 存储 Refresh Token（按 userId 维度）
     */
    public void storeRefreshToken(Long userId, String refreshToken, long ttlSeconds) {
        String key = REFRESH_PREFIX + userId;
        redisTemplate.opsForValue().set(key, refreshToken, ttlSeconds, TimeUnit.SECONDS);
    }

    /**
     * 获取 Refresh Token
     */
    public String getRefreshToken(Long userId) {
        return redisTemplate.opsForValue().get(REFRESH_PREFIX + userId);
    }

    /**
     * 删除 Refresh Token（登出时调用）
     */
    public void removeRefreshToken(Long userId) {
        redisTemplate.delete(REFRESH_PREFIX + userId);
    }

    /**
     * 验证 Refresh Token 是否匹配
     */
    public boolean validateRefreshToken(Long userId, String refreshToken) {
        String stored = getRefreshToken(userId);
        return stored != null && stored.equals(refreshToken);
    }

    /**
     * 删除用户所有 Refresh Token（封禁/修改密码时调用）
     */
    public void removeAllRefreshTokens(Long userId) {
        redisTemplate.delete(REFRESH_PREFIX + userId);
        log.info("已清除用户 {} 的 Refresh Token", userId);
    }

    // ==================== 登录限流 ====================

    /**
     * 记录登录失败次数，返回当前失败次数
     * 超过阈值锁定账户
     */
    public long recordLoginFailure(String username) {
        String key = LOGIN_LIMIT_PREFIX + username;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            // 第一次失败，设置15分钟过期
            redisTemplate.expire(key, 15, TimeUnit.MINUTES);
        }
        return count != null ? count : 0;
    }

    /**
     * 登录成功后清除失败计数
     */
    public void clearLoginFailure(String username) {
        redisTemplate.delete(LOGIN_LIMIT_PREFIX + username);
    }

    /**
     * 检查是否超过失败阈值
     */
    public boolean isLoginLocked(String username, int maxFailures) {
        String count = redisTemplate.opsForValue().get(LOGIN_LIMIT_PREFIX + username);
        return count != null && Integer.parseInt(count) >= maxFailures;
    }

    // ==================== Token 版本管理 ====================

    /**
     * 获取用户 Token 版本号
     * 修改密码/封禁用户时递增版本号，旧 Token 中的版本不匹配即失效
     */
    public long getTokenVersion(Long userId) {
        String version = redisTemplate.opsForValue().get(TOKEN_VERSION_PREFIX + userId);
        return version != null ? Long.parseLong(version) : 0L;
    }

    /**
     * 递增 Token 版本号（修改密码/封禁用户时调用）
     */
    public void incrementTokenVersion(Long userId) {
        redisTemplate.opsForValue().increment(TOKEN_VERSION_PREFIX + userId);
        // 同时清除所有 Refresh Token
        removeAllRefreshTokens(userId);
        log.info("用户 {} Token 版本已递增，所有旧 Token 失效", userId);
    }
}
