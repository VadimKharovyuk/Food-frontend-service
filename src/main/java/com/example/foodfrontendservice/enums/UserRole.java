package com.example.foodfrontendservice.enums;

import lombok.Getter;

/**
 * 👥 Роли пользователей в системе доставки еды
 * (Копия из User Service для Frontend Service)
 */
@Getter
public enum UserRole {

    /**
     * 🛒 Обычный пользователь
     */
    BASE_USER("Покупатель", "USER"),

    /**
     * 🏪 Владелец бизнеса
     */
    BUSINESS_USER("Владелец магазина", "BUSINESS"),

    /**
     * 🚚 Курьер
     */
    COURIER("Курьер", "COURIER"),

    /**
     * 👑 Администратор
     */
    ADMIN("Администратор", "ADMIN");

    private final String displayName;
    private final String authority;

    UserRole(String displayName, String authority) {
        this.displayName = displayName;
        this.authority = "ROLE_" + authority;
    }

    /**
     * 📋 Получить роли, доступные при регистрации
     */
    public static UserRole[] getRegistrationRoles() {
        return new UserRole[]{BASE_USER, BUSINESS_USER, COURIER};
    }

    /**
     * ✅ Проверить, может ли роль быть назначена при регистрации
     */
    public boolean isRegistrationAllowed() {
        return this != ADMIN;
    }
}