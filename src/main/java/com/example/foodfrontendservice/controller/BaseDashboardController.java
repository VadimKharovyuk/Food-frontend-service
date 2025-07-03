package com.example.foodfrontendservice.controller;


import com.example.foodfrontendservice.dto.UserResponseDto;
import com.example.foodfrontendservice.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;


@Slf4j
@RequiredArgsConstructor
public abstract class BaseDashboardController {

    protected final DashboardService dashboardService;


    @GetMapping("/current-user")
    @ResponseBody
    public Map<String, Object> getCurrentUser(HttpServletRequest request) {
        log.debug("👤 Запрос информации о текущем пользователе");

        try {
            UserResponseDto user = dashboardService.getCurrentUserFromSession(request);

            if (user != null) {
                return Map.of(
                        "success", true,
                        "user", Map.of(
                                "id", user.getId(),
                                "firstName", user.getFirstName(),
                                "lastName", user.getLastName(),
                                "email", user.getEmail(),
                                "role", user.getUserRole().name(),
                                "roleDisplayName", user.getUserRole().getDisplayName()
                        )
                );
            } else {
                return Map.of(
                        "success", false,
                        "message", "Пользователь не найден в сессии"
                );
            }
        } catch (Exception e) {
            log.error("❌ Ошибка получения информации о пользователе: {}", e.getMessage());
            return Map.of(
                    "success", false,
                    "message", "Ошибка получения данных пользователя"
            );
        }
    }

    /**
     * 🚪 Общий AJAX endpoint для выхода из системы
     */
    @PostMapping("/logout")
    @ResponseBody
    public Map<String, Object> logout(HttpServletRequest request) {
        log.info("🚪 Выход пользователя из системы");

        try {
            // Очищаем сессию
            request.getSession().invalidate();

            return Map.of(
                    "success", true,
                    "message", "Успешный выход из системы",
                    "redirectUrl", "/login"
            );
        } catch (Exception e) {
            log.error("❌ Ошибка при выходе из системы: {}", e.getMessage());
            return Map.of(
                    "success", false,
                    "message", "Ошибка при выходе из системы"
            );
        }
    }

    /**
     * 🔍 Общий AJAX endpoint для проверки статуса сессии
     */
    @GetMapping("/session-status")
    @ResponseBody
    public Map<String, Object> getSessionStatus(HttpServletRequest request) {
        log.debug("🔍 Проверка статуса сессии");

        boolean isAuthenticated = dashboardService.isUserAuthenticated(request);
        UserResponseDto user = dashboardService.getCurrentUserFromSession(request);

        return Map.of(
                "authenticated", isAuthenticated,
                "sessionId", request.getSession().getId(),
                "userRole", user != null ? user.getUserRole().name() : null,
                "timestamp", System.currentTimeMillis()
        );
    }

    /**
     * 📊 Общий AJAX endpoint для получения уведомлений
     */
    @GetMapping("/notifications")
    @ResponseBody
    public Map<String, Object> getNotifications(HttpServletRequest request) {
        log.debug("📊 Запрос уведомлений пользователя");

        if (!dashboardService.isUserAuthenticated(request)) {
            return Map.of("success", false, "message", "Пользователь не авторизован");
        }

        // TODO: Реализовать получение уведомлений через NotificationService
        return Map.of(
                "success", true,
                "notifications", List.of(
                        Map.of("id", 1, "type", "info", "message", "Добро пожаловать в QuickFood!", "time", "5 мин назад"),
                        Map.of("id", 2, "type", "success", "message", "Ваш профиль обновлен", "time", "1 час назад")
                ),
                "unreadCount", 2
        );
    }

    /**
     * ⚙️ Общий AJAX endpoint для обновления профиля
     */
    @PostMapping("/update-profile")
    @ResponseBody
    public Map<String, Object> updateProfile(HttpServletRequest request,
                                             @RequestBody Map<String, Object> profileData) {
        log.info("⚙️ Обновление профиля пользователя: {}", profileData);

        if (!dashboardService.isUserAuthenticated(request)) {
            return Map.of("success", false, "message", "Пользователь не авторизован");
        }

        try {
            // TODO: Реализовать обновление профиля через UserService
            return Map.of(
                    "success", true,
                    "message", "Профиль успешно обновлен"
            );
        } catch (Exception e) {
            log.error("❌ Ошибка обновления профиля: {}", e.getMessage());
            return Map.of(
                    "success", false,
                    "message", "Ошибка обновления профиля"
            );
        }
    }

    /**
     * 🔔 AJAX endpoint для отметки уведомления как прочитанного
     */
    @PostMapping("/notifications/{id}/read")
    @ResponseBody
    public Map<String, Object> markNotificationAsRead(HttpServletRequest request,
                                                      @PathVariable Long id) {
        log.debug("🔔 Отметка уведомления {} как прочитанного", id);

        if (!dashboardService.isUserAuthenticated(request)) {
            return Map.of("success", false, "message", "Пользователь не авторизован");
        }

        // TODO: Реализовать отметку уведомления
        return Map.of(
                "success", true,
                "message", "Уведомление отмечено как прочитанное"
        );
    }
}
