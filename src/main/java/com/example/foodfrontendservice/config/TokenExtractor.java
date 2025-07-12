package com.example.foodfrontendservice.config;

import com.example.foodfrontendservice.dto.AUTSERVICE.UserTokenInfo;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class TokenExtractor {

    private final JwtUtil jwtUtil;

    // ✅ Константы для имен cookies и атрибутов
    private static final String[] COOKIE_NAMES = {"authToken", "jwt", "accessToken"};
    private static final String CACHED_USER_INFO_ATTR = "cached_user_info";
    private static final String CACHED_TOKEN_ATTR = "cached_token";

    /**
     * ✅ ОСНОВНОЙ метод извлечения токена
     */
    public String extractToken(HttpServletRequest request) {
        // Проверяем кэш для текущего запроса
        String cachedToken = (String) request.getAttribute(CACHED_TOKEN_ATTR);
        if (cachedToken != null) {
            return cachedToken;
        }

        String token = null;

        // 1. Проверяем Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            log.debug("🔐 Токен найден в Authorization header");
        }

        // 2. Если не найден в header, проверяем cookies
        if (token == null) {
            token = extractTokenFromCookie(request);
        }

        // Кэшируем токен для текущего запроса (если найден)
        if (token != null && !token.trim().isEmpty()) {
            request.setAttribute(CACHED_TOKEN_ATTR, token);
        }

        return token;
    }

    /**
     * ✅ УЛУЧШЕННЫЙ метод извлечения из cookie с поддержкой нескольких имен
     */
    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            // Ищем по приоритету: authToken -> jwt -> accessToken
            for (String cookieName : COOKIE_NAMES) {
                for (Cookie cookie : cookies) {
                    if (cookieName.equals(cookie.getName())) {
                        String token = cookie.getValue();
                        if (token != null && !token.trim().isEmpty()) {
                            log.debug("🍪 Токен найден в cookie: {}", cookieName);
                            return token;
                        }
                    }
                }
            }
        }
        log.debug("🍪 Токен не найден в cookies");
        return null;
    }

    /**
     * ✅ ОПТИМИЗИРОВАННЫЙ метод - получение полной информации с кэшированием
     */
    public UserTokenInfo getCurrentUserInfo(HttpServletRequest request) {
        // Проверяем кэш для текущего запроса
        UserTokenInfo cachedInfo = (UserTokenInfo) request.getAttribute(CACHED_USER_INFO_ATTR);
        if (cachedInfo != null) {
            log.debug("📋 Используем кэшированную информацию пользователя");
            return cachedInfo;
        }

        String token = extractToken(request);
        if (token == null) {
            log.debug("❌ Токен не найден");
            return null;
        }

        if (!jwtUtil.isTokenValid(token)) {
            log.debug("❌ Токен недействителен");
            return null;
        }

        try {
            UserTokenInfo userInfo = UserTokenInfo.builder()
                    .userId(jwtUtil.getUserIdFromToken(token))
                    .email(jwtUtil.getEmailFromToken(token))
                    .role(jwtUtil.getRoleFromToken(token))
                    .token(token)
                    .build();

            // Кэшируем для текущего запроса
            request.setAttribute(CACHED_USER_INFO_ATTR, userInfo);

            log.debug("✅ Информация пользователя получена: {} (ID: {})",
                    userInfo.getEmail(), userInfo.getUserId());

            return userInfo;

        } catch (Exception e) {
            log.warn("⚠️ Ошибка извлечения данных из токена: {}", e.getMessage());
            return null;
        }
    }

    /**
     * ✅ ОПТИМИЗИРОВАННАЯ проверка авторизации
     */
    public boolean isAuthenticated(HttpServletRequest request) {
        // Если уже есть кэшированная информация, используем её
        UserTokenInfo cachedInfo = (UserTokenInfo) request.getAttribute(CACHED_USER_INFO_ATTR);
        if (cachedInfo != null) {
            return true;
        }

        String token = extractToken(request);
        return token != null && jwtUtil.isTokenValid(token);
    }

    /**
     * ✅ ОПТИМИЗИРОВАННЫЕ методы получения отдельных данных
     */
    public Long getCurrentUserId(HttpServletRequest request) {
        UserTokenInfo userInfo = getCurrentUserInfo(request);
        return userInfo != null ? userInfo.getUserId() : null;
    }

    public String getCurrentUserEmail(HttpServletRequest request) {
        UserTokenInfo userInfo = getCurrentUserInfo(request);
        return userInfo != null ? userInfo.getEmail() : null;
    }

    public String getCurrentUserRole(HttpServletRequest request) {
        UserTokenInfo userInfo = getCurrentUserInfo(request);
        return userInfo != null ? userInfo.getRole() : null;
    }

    /**
     * ✅ ДОПОЛНИТЕЛЬНЫЙ метод для принудительного обновления кэша
     */
    public UserTokenInfo refreshUserInfo(HttpServletRequest request) {
        // Очищаем кэш
        request.removeAttribute(CACHED_USER_INFO_ATTR);
        request.removeAttribute(CACHED_TOKEN_ATTR);

        // Получаем свежую информацию
        return getCurrentUserInfo(request);
    }

    /**
     * ✅ УТИЛИТАРНЫЙ метод для проверки валидности токена без извлечения данных
     */
    public boolean isTokenValid(HttpServletRequest request) {
        String token = extractToken(request);
        return token != null && jwtUtil.isTokenValid(token);
    }

    /**
     * ✅ УТИЛИТАРНЫЙ метод для логирования информации о токене (для отладки)
     */
    public void logTokenInfo(HttpServletRequest request) {
        if (log.isDebugEnabled()) {
            String token = extractToken(request);
            if (token != null) {
                boolean isValid = jwtUtil.isTokenValid(token);
                log.debug("🔍 Token Info: length={}, valid={}", token.length(), isValid);

                if (isValid) {
                    try {
                        String email = jwtUtil.getEmailFromToken(token);
                        Long userId = jwtUtil.getUserIdFromToken(token);
                        String role = jwtUtil.getRoleFromToken(token);
                        log.debug("👤 User Info: email={}, id={}, role={}", email, userId, role);
                    } catch (Exception e) {
                        log.debug("⚠️ Ошибка парсинга токена: {}", e.getMessage());
                    }
                }
            } else {
                log.debug("❌ Токен отсутствует");
            }
        }
    }
}