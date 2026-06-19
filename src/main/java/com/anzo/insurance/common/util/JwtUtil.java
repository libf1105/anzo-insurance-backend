package com.anzo.insurance.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT工具类
 */
@Slf4j
@Component
public class JwtUtil {
    
    @Value("${app.jwt.secret}")
    private String secret;
    
    @Value("${app.jwt.expiration}")
    private long expiration;
    
    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;
    
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 生成Access Token
     */
    public String generateAccessToken(Long userId, Long enterpriseId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("enterpriseId", enterpriseId);
        claims.put("username", username);
        claims.put("role", role);
        claims.put("type", "ACCESS");
        
        return generateToken(claims, expiration);
    }
    
    /**
     * 生成Refresh Token
     */
    public String generateRefreshToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "REFRESH");
        
        return generateToken(claims, refreshExpiration);
    }
    
    /**
     * 生成Token
     */
    private String generateToken(Map<String, Object> claims, long expiration) {
        Date issuedAt = new Date();
        Date expirationTime = new Date(issuedAt.getTime() + expiration);
        
        return Jwts.builder()
            .header()
                .type("JWT")
                .add("kid", UUID.randomUUID().toString())
            .and()
            .claims(claims)
            .subject(claims.get("userId").toString())
            .issuedAt(issuedAt)
            .expiration(expirationTime)
            .signWith(getSecretKey(), Jwts.SIG.HS256)
            .compact();
    }
    
    /**
     * 解析Token获取Claims
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (Exception e) {
            log.warn("JWT解析失败: {}", e.getMessage());
            throw new RuntimeException("JWT解析失败");
        }
    }
    
    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }
    
    /**
     * 从Token中获取企业ID
     */
    public Long getEnterpriseIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("enterpriseId", Long.class);
    }
    
    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }
    
    /**
     * 从Token中获取角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }
    
    /**
     * 从Token中获取Token类型
     */
    public String getTokenTypeFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("type", String.class);
    }
    
    /**
     * 验证Token是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    
    /**
     * 获取Token过期时间
     */
    public Date getExpirationFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration();
    }
    
    /**
     * 验证Token
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
