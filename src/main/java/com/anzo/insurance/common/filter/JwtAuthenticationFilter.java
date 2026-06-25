package com.anzo.insurance.common.filter;

import com.anzo.insurance.common.util.RedisTokenUtil;
import com.anzo.insurance.modules.auth.entity.Enterprise;
import com.anzo.insurance.modules.auth.entity.User;
import com.anzo.insurance.modules.auth.repository.EnterpriseRepository;
import com.anzo.insurance.modules.auth.repository.UserRepository;
import com.anzo.insurance.modules.auth.service.JwtService;
import com.anzo.insurance.modules.auth.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserRepository userRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final RedisTokenUtil redisTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = extractToken(request);

            if (StringUtils.hasText(token) && jwtService.extractUsername(token) != null) {
                String username = jwtService.extractUsername(token);
                String jti = jwtService.extractJti(token);
                Long tokenUserId = jwtService.extractUserId(token);
                Long tokenEnterpriseId = jwtService.extractEnterpriseId(token);
                
                // 检查Token是否在黑名单中（Redis异常时容错，跳过检查）
                try {
                    if (jti != null && redisTokenUtil.isBlacklisted(jti)) {
                        log.debug("Token已失效（在黑名单中）: {}", jti);
                        filterChain.doFilter(request, response);
                        return;
                    }
                } catch (Exception redisEx) {
                    log.warn("Redis检查Token黑名单失败，跳过黑名单检查: {}", redisEx.getMessage());
                }
                
                // 检查Token版本号（Redis异常时容错，跳过检查）
                try {
                    if (tokenUserId != null) {
                        long currentVersion = redisTokenUtil.getTokenVersion(tokenUserId);
                        Long tokenVersion = jwtService.extractClaim(token, claims -> {
                            Object val = claims.get("version");
                            return val != null ? ((Number) val).longValue() : null;
                        });
                        if (tokenVersion != null && tokenVersion < currentVersion) {
                            log.debug("Token版本已过期: tokenVersion={}, currentVersion={}", tokenVersion, currentVersion);
                            filterChain.doFilter(request, response);
                            return;
                        }
                    }
                } catch (Exception redisEx) {
                    log.warn("Redis检查Token版本失败，跳过版本检查: {}", redisEx.getMessage());
                }

                // 加载用户信息
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                User user = userRepository.findByUsername(username).orElse(null);

                if (user != null && jwtService.isTokenValid(token, user)) {
                    // 验证企业状态
                    Enterprise enterprise = enterpriseRepository.selectById(user.getEnterpriseId());
                    if (enterprise == null || !Enterprise.Status.ACTIVE.getValue().equals(enterprise.getStatus())) {
                        log.warn("企业状态异常，拒绝认证: enterpriseId={}", user.getEnterpriseId());
                        filterChain.doFilter(request, response);
                        return;
                    }
                    
                    // 获取token中的信息
                    Long userId = tokenUserId;
                    Long enterpriseId = tokenEnterpriseId;

                    // 创建认证信息
                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                            user, null, userDetails.getAuthorities());

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 设置到Security上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // 将企业ID和用户ID添加到请求属性中，供后续使用
                    request.setAttribute("enterpriseId", enterpriseId);
                    request.setAttribute("userId", userId);
                }
            }
        } catch (Exception e) {
            log.warn("JWT认证处理异常: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中提取Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
