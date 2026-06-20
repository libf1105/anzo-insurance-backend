package com.anzo.insurance.modules.auth.service;

import com.anzo.insurance.modules.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT服务类
 */
@Service
public class JwtService {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.expiration-time}")
    private long expirationTime;

    @Value("${jwt.refresh-expiration-time}")
    private long refreshExpirationTime;

    /**
     * 生成访问令牌
     */
    public String generateToken(User user) {
        return generateToken(new HashMap<>(), user);
    }

    /**
     * 生成访问令牌（带额外声明）
     */
    public String generateToken(Map<String, Object> extraClaims, User user) {
        return buildToken(extraClaims, user, expirationTime);
    }

    /**
     * 生成访问令牌（带Token版本号）
     */
    public String generateToken(User user, Long tokenVersion) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("version", tokenVersion);
        return buildToken(claims, user, expirationTime);
    }

    /**
     * 生成刷新令牌
     */
    public String generateRefreshToken(User user) {
        return buildToken(new HashMap<>(), user, refreshExpirationTime);
    }

    /**
     * 构建令牌
     */
    private String buildToken(Map<String, Object> extraClaims, User user, long expiration) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .claim("enterpriseId", user.getEnterpriseId())
                .claim("role", user.getRole())
                .setId(java.util.UUID.randomUUID().toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 提取Token版本号
     */
    public Long extractVersion(String token) {
        return extractClaim(token, claims -> claims.get("version", Long.class));
    }

    /**
     * 验证令牌是否有效
     */
    public boolean isTokenValid(String token, User user) {
        final String username = extractUsername(token);
        return (username.equals(user.getUsername())) && !isTokenExpired(token);
    }

    /**
     * 提取用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 提取用户ID
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * 提取企业ID
     */
    public Long extractEnterpriseId(String token) {
        return extractClaim(token, claims -> claims.get("enterpriseId", Long.class));
    }

    /**
     * 提取角色
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * 提取令牌过期时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 提取声明
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 提取所有声明
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 检查令牌是否过期
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 获取签名密钥
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretKey);
        } catch (RuntimeException ex) {
            // 兼容当前环境直接配置原始字符串密钥的情况
            keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 获取令牌过期时间（秒）
     */
    public Long getExpirationTime() {
        return expirationTime / 1000;
    }

    /**
     * 获取刷新令牌过期时间（秒）
     */
    public Long getRefreshExpirationTime() {
        return refreshExpirationTime / 1000;
    }

    /**
     * 提取令牌ID（jti）
     */
    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    /**
     * 获取access token过期时间（毫秒）
     */
    public long getExpirationTimeMillis() {
        return expirationTime;
    }
}
