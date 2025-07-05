package com.example.foodfrontendservice.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String getEmailFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.error("Error extracting email from token: {}", e.getMessage());
            return null;
        }
    }

    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Object userIdObj = claims.get("userId");

            if (userIdObj instanceof Number) {
                return ((Number) userIdObj).longValue();
            } else if (userIdObj instanceof String) {
                return Long.parseLong((String) userIdObj);
            }

            log.warn("UserId in token has unexpected type: {}", userIdObj.getClass());
            return null;
        } catch (Exception e) {
            log.error("Error extracting userId from token: {}", e.getMessage());
            return null;
        }
    }

    public String getRoleFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.get("role", String.class);
        } catch (Exception e) {
            log.error("Error extracting role from token: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            log.debug("Token validated successfully for user: {}", claims.getSubject());
            return true;
        } catch (Exception e) {
            log.error("JWT token validation error: {}", e.getMessage());
            return false;
        }
    }

    // ✅ ДОБАВЛЯЕМ метод isTokenValid для контроллера
    public boolean isTokenValid(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.debug("❌ Токен пустой или null");
            return false;
        }

        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            Date now = new Date();

            if (expiration.before(now)) {
                log.warn("❌ Токен истек. Expiration: {}, Current: {}", expiration, now);
                return false;
            }

            log.debug("✅ Токен действителен для пользователя: {} до {}",
                    claims.getSubject(), expiration);
            return true;

        } catch (ExpiredJwtException e) {
            log.warn("❌ Токен истек: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("❌ Некорректный формат токена: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.warn("❌ Неверная подпись токена: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("❌ Пустой или некорректный токен: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("❌ Неожиданная ошибка валидации токена: {}", e.getMessage(), e);
            return false;
        }
    }



    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ✅ УЛУЧШЕННЫЙ метод для получения времени истечения токена
    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration();
        } catch (Exception e) {
            log.error("Error extracting expiration from token: {}", e.getMessage());
            return null;
        }
    }

    // ✅ ДОБАВЛЯЕМ метод для проверки скорого истечения токена
    public boolean isTokenExpiringSoon(String token, long minutesThreshold) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            if (expiration == null) {
                return true; // Считаем что истекает, если не можем получить дату
            }

            Date now = new Date();
            long diffInMillis = expiration.getTime() - now.getTime();
            long diffInMinutes = diffInMillis / (60 * 1000);

            boolean expiringSoon = diffInMinutes <= minutesThreshold;

            if (expiringSoon) {
                log.info("⏰ Токен истекает через {} минут (порог: {})", diffInMinutes, minutesThreshold);
            }

            return expiringSoon;
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    // ✅ ДОБАВЛЯЕМ метод для проверки типа токена (обычный или Remember Me)
    public boolean isRememberMeToken(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            if (expiration == null) {
                return false;
            }

            Date now = new Date();
            long diffInMillis = expiration.getTime() - now.getTime();
            long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);

            // Если токен действует больше 2 дней, считаем что это Remember Me токен
            boolean isRememberMe = diffInDays > 2;

            log.debug("🍪 Токен действует {} дней, Remember Me: {}", diffInDays, isRememberMe);
            return isRememberMe;

        } catch (Exception e) {
            log.error("Error checking if token is Remember Me: {}", e.getMessage());
            return false;
        }
    }
}