package com.example.foodfrontendservice.config;

import com.example.foodfrontendservice.dto.AUTSERVICE.UserTokenInfo;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
@RequiredArgsConstructor
@Component
public class TokenExtractor {
    private final JwtUtil jwtUtil;

    public String extractToken(HttpServletRequest request) {
        // 1. Проверяем Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 2. Проверяем cookie
        return extractTokenFromCookie(request);
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("authToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
    // ✅ НОВЫЕ МЕТОДЫ - всё в одном компоненте

    public Long getCurrentUserId(HttpServletRequest request) {
        String token = extractToken(request);
        if (token != null && jwtUtil.isTokenValid(token)) {
            return jwtUtil.getUserIdFromToken(token);
        }
        return null;
    }

    public String getCurrentUserEmail(HttpServletRequest request) {
        String token = extractToken(request);
        if (token != null && jwtUtil.isTokenValid(token)) {
            return jwtUtil.getEmailFromToken(token);
        }
        return null;
    }

    public String getCurrentUserRole(HttpServletRequest request) {
        String token = extractToken(request);
        if (token != null && jwtUtil.isTokenValid(token)) {
            return jwtUtil.getRoleFromToken(token);
        }
        return null;
    }

    public boolean isAuthenticated(HttpServletRequest request) {
        String token = extractToken(request);
        return token != null && jwtUtil.isTokenValid(token);
    }

    // ✅ Полная информация о пользователе одним вызовом
    public UserTokenInfo getCurrentUserInfo(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || !jwtUtil.isTokenValid(token)) {
            return null;
        }

        return UserTokenInfo.builder()
                .userId(jwtUtil.getUserIdFromToken(token))
                .email(jwtUtil.getEmailFromToken(token))
                .role(jwtUtil.getRoleFromToken(token))
                .token(token)
                .build();
    }
}
