package com.example.foodfrontendservice.dto;

import com.example.foodfrontendservice.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

    // === ОСНОВНАЯ ИНФОРМАЦИЯ ===
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole userRole;
    private String roleDisplayName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // === ГЕОЛОКАЦИЯ ПОЛЬЗОВАТЕЛЯ ===

    /**
     * 📍 Широта пользователя
     */
    private BigDecimal latitude;

    /**
     * 📍 Долгота пользователя
     */
    private BigDecimal longitude;

    /**
     * 🏠 Адрес пользователя
     */
    private String street;
    private String city;
    private String region;
    private String country;
    private String postalCode;

    /**
     * 📍 Полный адрес (кэшированный)
     */
    private String fullAddress;

    /**
     * 🕐 Время последнего обновления геолокации
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime locationUpdatedAt;

    // === COMPUTED FIELDS (вычисляемые поля) ===

    /**
     * 📍 Есть ли у пользователя геолокация
     */
    private Boolean hasLocation;

    /**
     * 📍 Форматированные координаты для отображения
     * Например: "50.450100, 30.523400"
     */
    private String formattedCoordinates;

    /**
     * 🏠 Краткий адрес (город, страна)
     * Например: "Киев, Украина"
     */
    private String shortAddress;

    /**
     * 📊 Статус геолокации для UI
     * Например: "Установлена", "Не установлена", "Устарела"
     */
    private String locationStatus;

    // === HELPER МЕТОДЫ ===

    /**
     * Получить отображаемое имя роли
     */
    public String getRoleDisplayName() {
        if (roleDisplayName != null) {
            return roleDisplayName;
        }

        return userRole != null ? switch (userRole) {
            case ADMIN -> "Администратор";
            case BASE_USER -> "Пользователь";
            case BUSINESS_USER -> "Владелец магазина";
            case COURIER -> "Курьер"; // если есть такая роль
        } : "Неизвестная роль";
    }

    /**
     * Проверить, есть ли геолокация
     */
    public Boolean getHasLocation() {
        if (hasLocation != null) {
            return hasLocation;
        }
        return latitude != null && longitude != null;
    }

    /**
     * Получить статус геолокации
     */
    public String getLocationStatus() {
        if (locationStatus != null) {
            return locationStatus;
        }

        if (!getHasLocation()) {
            return "Не установлена";
        }

        if (locationUpdatedAt == null) {
            return "Установлена";
        }

        // Проверяем, не устарела ли геолокация (старше 30 дней)
        LocalDateTime now = LocalDateTime.now();
        if (locationUpdatedAt.isBefore(now.minusDays(30))) {
            return "Устарела";
        }

        return "Актуальна";
    }

    /**
     * Получить форматированные координаты
     */
    public String getFormattedCoordinates() {
        if (formattedCoordinates != null) {
            return formattedCoordinates;
        }

        if (latitude != null && longitude != null) {
            return String.format("%.6f, %.6f", latitude, longitude);
        }

        return null;
    }

    /**
     * Получить краткий адрес
     */
    public String getShortAddress() {
        if (shortAddress != null) {
            return shortAddress;
        }

        if (city == null && country == null) {
            return null;
        }

        if (city != null && country != null) {
            return city + ", " + country;
        }

        return city != null ? city : country;
    }
}