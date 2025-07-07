package com.example.foodfrontendservice.service;

import com.example.foodfrontendservice.dto.UserResponseDto;
import com.example.foodfrontendservice.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpServletRequest;
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final UserIntegrationService userIntegrationService;

    /**
     * 🔑 Получение JWT токена из запроса
     * Проверяет Header Authorization и Cookie
     */
    public String extractTokenFromRequest(HttpServletRequest request) {
        // ✅ 1. Проверяем Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.debug("🔑 Токен найден в Authorization header");
            return token;
        }

        // ✅ 2. Проверяем cookies (если используются)
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName()) || "authToken".equals(cookie.getName())) {
                    log.debug("🍪 Токен найден в cookie: {}", cookie.getName());
                    return cookie.getValue();
                }
            }
        }

        // ✅ 3. Проверяем параметр запроса (для особых случаев)
        String tokenParam = request.getParameter("token");
        if (tokenParam != null && !tokenParam.isEmpty()) {
            log.debug("🔗 Токен найден в параметре запроса");
            return tokenParam;
        }

        log.debug("❌ JWT токен не найден в запросе");
        return null;
    }

    /**
     * 👤 Получение текущего пользователя через JWT токен
     */
    public UserResponseDto getCurrentUserFromSession(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);

            if (token == null) {
                log.debug("❌ Токен отсутствует - пользователь не авторизован");
                return null;
            }

            // ✅ Получаем пользователя через User Service
            UserResponseDto user = userIntegrationService.getUserByToken(token);

            if (user != null) {
                log.debug("✅ Пользователь получен: {} ({})", user.getEmail(), user.getUserRole());
                return user;
            } else {
                log.debug("❌ Пользователь не найден по токену");
                return null;
            }

        } catch (Exception e) {
            log.error("❌ Ошибка получения пользователя: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 🔑 Получение токена из запроса (публичный метод)
     */
    public String getTokenFromSession(HttpServletRequest request) {
        return extractTokenFromRequest(request);
    }

    /**
     * ✅ Проверка авторизации пользователя
     */
    public boolean isUserAuthenticated(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);

            if (token == null) {
                return false;
            }

            // ✅ Быстрая валидация токена без получения полных данных
            boolean isValid = userIntegrationService.validateToken(token);

            log.debug("🔍 Результат валидации токена: {}", isValid);
            return isValid;

        } catch (Exception e) {
            log.error("❌ Ошибка проверки авторизации: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 🔒 Проверка роли пользователя
     */
    public boolean hasRole(HttpServletRequest request, UserRole requiredRole) {
        try {
            UserResponseDto user = getCurrentUserFromSession(request);

            if (user == null) {
                log.debug("❌ Пользователь не найден - доступ запрещен");
                return false;
            }

            boolean hasRole = user.getUserRole() == requiredRole;
            log.debug("🔒 Проверка роли {} для пользователя {}: {}",
                    requiredRole, user.getEmail(), hasRole);

            return hasRole;

        } catch (Exception e) {
            log.error("❌ Ошибка проверки роли: {}", e.getMessage());
            return false;
        }
    }



    /**
     * 🔍 Получение информации о токене
     */
    public TokenInfo getTokenInfo(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);

            if (token == null) {
                return TokenInfo.builder()
                        .valid(false)
                        .message("Токен отсутствует")
                        .build();
            }

            boolean isValid = userIntegrationService.validateToken(token);
            UserResponseDto user = null;

            if (isValid) {
                user = userIntegrationService.getUserByToken(token);
            }

            return TokenInfo.builder()
                    .valid(isValid)
                    .token(token.substring(0, Math.min(20, token.length())) + "...")
                    .user(user)
                    .message(isValid ? "Токен действителен" : "Токен недействителен")
                    .build();

        } catch (Exception e) {
            log.error("❌ Ошибка получения информации о токене: {}", e.getMessage());
            return TokenInfo.builder()
                    .valid(false)
                    .message("Ошибка проверки токена: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 🧹 Очистка токена (для logout)
     */
    public void clearUserSession(HttpServletRequest request) {
        try {
            // Очищаем сессию если используется
            if (request.getSession(false) != null) {
                request.getSession().invalidate();
                log.debug("🧹 HTTP сессия очищена");
            }

            // Токен очищается на клиенте (localStorage/sessionStorage)
            log.info("✅ Пользовательская сессия очищена");

        } catch (Exception e) {
            log.error("❌ Ошибка очистки сессии: {}", e.getMessage());
        }
    }

    // ========== DTO КЛАССЫ ==========

    /**
     * DTO для информации о токене
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TokenInfo {
        private Boolean valid;
        private String token;
        private UserResponseDto user;
        private String message;
        private java.time.LocalDateTime checkedAt = java.time.LocalDateTime.now();
    }
}