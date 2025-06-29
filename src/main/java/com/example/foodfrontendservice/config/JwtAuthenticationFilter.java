package com.example.foodfrontendservice.config;

import com.example.foodfrontendservice.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements Filter {

    private final JwtUtil jwtUtil;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();

        log.info("🌐 JWT Filter processing request: {} {}", httpRequest.getMethod(), requestURI);

        String token = extractTokenFromRequest(httpRequest);

        if (token != null) {
            log.info("🔍 JWT token found for: {}", requestURI);
            log.info("🔍 Token starts with: {}...", token.substring(0, Math.min(20, token.length())));

            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                String email = jwtUtil.getEmailFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);

                log.info("✅ JWT validated - UserId: {}, Email: {}, Role: {}", userId, email, role);

                HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(httpRequest) {
                    @Override
                    public String getHeader(String name) {
                        switch (name) {
                            case "X-User-Id":
                                log.debug("Returning X-User-Id: {}", userId);
                                return userId != null ? userId.toString() : null;
                            case "X-User-Email":
                                log.debug("Returning X-User-Email: {}", email);
                                return email;
                            case "X-User-Role":
                                log.debug("Returning X-User-Role: {}", role);
                                return role;
                            default:
                                return super.getHeader(name);
                        }
                    }
                };

                log.info("🚀 Forwarding request with JWT data");
                chain.doFilter(requestWrapper, response);
            } else {
                log.warn("❌ Invalid JWT token for: {}", requestURI);
                chain.doFilter(request, response);
            }
        } else {
            log.info("❌ No JWT token found for: {}", requestURI);
            chain.doFilter(request, response);
        }
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        log.info("🔍 Authorization header: {}", bearerToken != null ? "Bearer ***" : "null");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            log.info("🔍 Extracted token length: {}", token.length());
            return token;
        }
        return null;
    }
}