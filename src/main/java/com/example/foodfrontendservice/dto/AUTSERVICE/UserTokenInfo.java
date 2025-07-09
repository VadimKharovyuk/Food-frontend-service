package com.example.foodfrontendservice.dto.AUTSERVICE;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для данных, извлекаемых из JWT токена
 * Содержит только базовую информацию для быстрой авторизации
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTokenInfo {

    /**
     * ID пользователя из JWT
     */
    private Long userId;

    /**
     * Email пользователя из JWT
     */
    private String email;

    /**
     * Роль пользователя из JWT (например: "ROLE_USER", "BASE_USER")
     */
    private String role;

    /**
     * Сам JWT токен (для передачи в микросервисы)
     */
    private String token;

    // ✅ Полезные методы

    /**
     * Проверка, является ли пользователь администратором
     */
    public boolean isAdmin() {
        return "ADMIN".equals(role) || "ROLE_ADMIN".equals(role);
    }

    /**
     * Проверка, является ли пользователь владельцем бизнеса
     */
    public boolean isBusinessUser() {
        return "BUSINESS_USER".equals(role) || "ROLE_BUSINESS_USER".equals(role);
    }

    /**
     * Проверка, является ли пользователь курьером
     */
    public boolean isCourier() {
        return "COURIER".equals(role) || "ROLE_COURIER".equals(role);
    }

    /**
     * Получить роль в читаемом виде
     */
    public String getRoleDisplayName() {
        return switch (role) {
            case "ADMIN", "ROLE_ADMIN" -> "Администратор";
            case "BASE_USER", "ROLE_USER" -> "Пользователь";
            case "BUSINESS_USER", "ROLE_BUSINESS_USER" -> "Владелец магазина";
            case "COURIER", "ROLE_COURIER" -> "Курьер";
            default -> "Неизвестная роль";
        };
    }

    /**
     * Получить Authorization header для запросов к микросервисам
     */
    public String getAuthorizationHeader() {
        return token != null ? "Bearer " + token : null;
    }
}