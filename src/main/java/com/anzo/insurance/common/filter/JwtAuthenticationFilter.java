package com.anzo.insurance.common.filter;

import com.anzo.insurance.modules.auth.entity.User;
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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
    private static final HttpClient DEBUG_HTTP_CLIENT = HttpClient.newHttpClient();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = extractToken(request);

            if (StringUtils.hasText(token) && jwtService.extractUsername(token) != null) {
                String username = jwtService.extractUsername(token);
                Long tokenUserId = jwtService.extractUserId(token);
                Long tokenEnterpriseId = jwtService.extractEnterpriseId(token);
                // #region debug-point C:jwt-filter-start
                sendDebugEvent("C", "pre-fix", "src/main/java/com/anzo/insurance/common/filter/JwtAuthenticationFilter.java:47",
                        "[DEBUG] jwt filter token parsed",
                        "{\"path\":\"" + safeJson(request.getRequestURI()) + "\",\"username\":\"" + safeJson(username)
                                + "\",\"tokenUserId\":" + tokenUserId + ",\"tokenEnterpriseId\":" + tokenEnterpriseId + "}");
                // #endregion

                // 加载用户信息
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                User user = userRepository.findByUsername(username).orElse(null);

                if (user != null && jwtService.isTokenValid(token, user)) {
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
                    // #region debug-point C:jwt-filter-user-found
                    sendDebugEvent("C", "pre-fix", "src/main/java/com/anzo/insurance/common/filter/JwtAuthenticationFilter.java:73",
                            "[DEBUG] jwt filter user resolved",
                            "{\"path\":\"" + safeJson(request.getRequestURI()) + "\",\"username\":\"" + safeJson(username)
                                    + "\",\"dbUserId\":" + user.getId() + ",\"dbEnterpriseId\":" + user.getEnterpriseId() + "}");
                    // #endregion
                }
            }
        } catch (Exception e) {
            // #region debug-point C:jwt-filter-error
            sendDebugEvent("C", "pre-fix", "src/main/java/com/anzo/insurance/common/filter/JwtAuthenticationFilter.java:79",
                    "[DEBUG] jwt filter failed",
                    "{\"path\":\"" + safeJson(request.getRequestURI()) + "\",\"message\":\"" + safeJson(e.getMessage()) + "\"}");
            // #endregion
            log.debug("JWT认证失败: {}", e.getMessage());
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

    private void sendDebugEvent(String hypothesisId, String runId, String location, String msg, String dataJson) {
        try {
            String body = "{"
                    + "\"sessionId\":\"user-not-found-logout\","
                    + "\"runId\":\"" + runId + "\","
                    + "\"hypothesisId\":\"" + hypothesisId + "\","
                    + "\"location\":\"" + location + "\","
                    + "\"msg\":\"" + msg + "\","
                    + "\"data\":" + dataJson + ","
                    + "\"ts\":" + System.currentTimeMillis()
                    + "}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:7777/event"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            DEBUG_HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception ignored) {
        }
    }

    private String safeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
