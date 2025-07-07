package com.example.foodfrontendservice.controller;

import com.example.foodfrontendservice.dto.UserResponseDto;
import com.example.foodfrontendservice.enums.UserRole;
import com.example.foodfrontendservice.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Slf4j
public class UnifiedDashboardController {

    private final DashboardService dashboardService;

    /**
     * 🏠 Главная страница дашборда - единая для всех ролей
     */
    @GetMapping
    public String dashboard(HttpServletRequest request, Model model) {
        log.info("🏠 Загрузка единого дашборда");

        // ✅ Передаем базовую конфигурацию без проверки токена
        // Проверка авторизации будет выполнена через JavaScript API
        model.addAttribute("needsAuth", true);
        model.addAttribute("apiBaseUrl", "http://localhost:8082");

        // ✅ Передаем информацию о всех доступных ролях для frontend
        model.addAttribute("availableRoles", Map.of(
                "BASE_USER", "Покупатель",
                "BUSINESS_USER", "Владелец магазина",
                "COURIER", "Курьер",
                "ADMIN", "Администратор"
        ));

        log.info("✅ Дашборд загружен - проверка авторизации будет через JavaScript");

        return "dashboard/main";
    }

    /**
     * 🔧 API endpoint для получения конфигурации дашборда
     */
    @GetMapping("/api/config")
    @ResponseBody
    public Map<String, Object> getDashboardConfigApi(HttpServletRequest request) {
        log.debug("🔧 Запрос конфигурации дашборда");

        // ✅ JWT проверка
        UserResponseDto user = dashboardService.getCurrentUserFromSession(request);

        if (user == null) {
            log.warn("❌ Неавторизованный запрос конфигурации дашборда");
            return Map.of(
                    "error", "Пользователь не авторизован",
                    "code", "UNAUTHORIZED",
                    "redirectUrl", "/login"
            );
        }

        // ✅ Проверяем валидность токена
        if (!dashboardService.isUserAuthenticated(request)) {
            log.warn("❌ Токен недействителен для пользователя: {}", user.getEmail());
            return Map.of(
                    "error", "Токен недействителен",
                    "code", "TOKEN_INVALID",
                    "redirectUrl", "/login"
            );
        }

        log.debug("✅ Конфигурация дашборда для пользователя: {} ({})", user.getEmail(), user.getUserRole());

        return Map.of(
                "user", Map.of(
                        "id", user.getId(),
                        "email", user.getEmail(),
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName(),
                        "role", user.getUserRole().name(),
                        "roleDisplayName", user.getUserRole().getDisplayName()
                ),
                "config", getDashboardConfig(user.getUserRole()),
                "tokenInfo", dashboardService.getTokenInfo(request)
        );
    }

    /**
     * 📊 API endpoint для получения данных секции дашборда
     */
    @GetMapping("/api/data/{section}")
    @ResponseBody
    public Map<String, Object> getSectionData(@PathVariable String section, HttpServletRequest request) {
        log.debug("📊 Запрос данных секции: {}", section);

        // ✅ JWT проверка
        UserResponseDto user = dashboardService.getCurrentUserFromSession(request);

        if (user == null) {
            log.warn("❌ Неавторизованный запрос секции: {}", section);
            return Map.of(
                    "error", "Пользователь не авторизован",
                    "code", "UNAUTHORIZED",
                    "section", section
            );
        }

        // ✅ Дополнительная проверка токена
        if (!dashboardService.isUserAuthenticated(request)) {
            log.warn("❌ Недействительный токен при запросе секции {} для пользователя: {}", section, user.getEmail());
            return Map.of(
                    "error", "Токен недействителен",
                    "code", "TOKEN_INVALID",
                    "section", section
            );
        }

        // ✅ Проверяем доступ к секции
        if (!hasAccessToSection(user.getUserRole(), section)) {
            log.warn("🚫 Пользователь {} ({}) пытается получить доступ к секции: {}",
                    user.getEmail(), user.getUserRole(), section);
            return Map.of(
                    "error", "Нет доступа к секции " + section,
                    "code", "ACCESS_DENIED",
                    "section", section,
                    "userRole", user.getUserRole().name()
            );
        }

        log.info("📊 Загрузка данных секции '{}' для пользователя {} ({})",
                section, user.getEmail(), user.getUserRole());

        // ✅ TODO: Здесь будет реальная логика загрузки данных из сервисов
        // Пока возвращаем заглушку что секция в разработке
        return Map.of(
                "message", "Секция '" + section + "' в разработке",
                "section", section,
                "userRole", user.getUserRole().name(),
                "status", "coming_soon",
                "_meta", Map.of(
                        "section", section,
                        "userRole", user.getUserRole().name(),
                        "loadedAt", java.time.LocalDateTime.now(),
                        "userId", user.getId()
                )
        );
    }

    /**
     * 🚪 JWT-aware Logout endpoint
     */
    @PostMapping("/logout")
    @ResponseBody
    public Map<String, Object> logout(HttpServletRequest request) {
        log.info("🚪 Запрос выхода из системы");

        try {
            // ✅ Получаем информацию о пользователе перед выходом
            UserResponseDto user = dashboardService.getCurrentUserFromSession(request);
            String userEmail = user != null ? user.getEmail() : "unknown";

            // ✅ Очищаем сессию через DashboardService
            dashboardService.clearUserSession(request);

            log.info("✅ Пользователь {} успешно вышел из системы", userEmail);

            return Map.of(
                    "success", true,
                    "message", "Успешный выход из системы",
                    "redirectUrl", "/login",
                    "userEmail", userEmail,
                    "logoutTime", java.time.LocalDateTime.now()
            );

        } catch (Exception e) {
            log.error("❌ Ошибка при logout: {}", e.getMessage());
            return Map.of(
                    "success", false,
                    "error", "Ошибка выхода",
                    "message", e.getMessage(),
                    "redirectUrl", "/login"
            );
        }
    }

    /**
     * 🔍 API endpoint для проверки статуса токена
     */
    @GetMapping("/api/token-status")
    @ResponseBody
    public Map<String, Object> getTokenStatus(HttpServletRequest request) {
        log.debug("🔍 Проверка статуса токена");

        try {
            DashboardService.TokenInfo tokenInfo = dashboardService.getTokenInfo(request);

            return Map.of(
                    "valid", tokenInfo.getValid(),
                    "message", tokenInfo.getMessage(),
                    "user", tokenInfo.getUser() != null ? Map.of(
                            "email", tokenInfo.getUser().getEmail(),
                            "role", tokenInfo.getUser().getUserRole().name(),
                            "firstName", tokenInfo.getUser().getFirstName()
                    ) : null,
                    "checkedAt", tokenInfo.getCheckedAt()
            );

        } catch (Exception e) {
            log.error("❌ Ошибка проверки статуса токена: {}", e.getMessage());
            return Map.of(
                    "valid", false,
                    "message", "Ошибка проверки токена: " + e.getMessage(),
                    "error", true
            );
        }
    }

    /**
     * 🔄 API endpoint для обновления данных пользователя
     */
    @GetMapping("/api/refresh-user")
    @ResponseBody
    public Map<String, Object> refreshUserData(HttpServletRequest request) {
        log.debug("🔄 Обновление данных пользователя");

        try {
            // ✅ Принудительно получаем свежие данные пользователя
            String token = dashboardService.getTokenFromSession(request);

            if (token == null) {
                return Map.of(
                        "success", false,
                        "error", "Токен отсутствует"
                );
            }

            // Обращаемся напрямую к User Service за свежими данными
            UserResponseDto user = dashboardService.getCurrentUserFromSession(request);

            if (user == null) {
                return Map.of(
                        "success", false,
                        "error", "Пользователь не найден"
                );
            }

            log.debug("✅ Данные пользователя обновлены: {}", user.getEmail());

            return Map.of(
                    "success", true,
                    "user", Map.of(
                            "id", user.getId(),
                            "email", user.getEmail(),
                            "firstName", user.getFirstName(),
                            "lastName", user.getLastName(),
                            "role", user.getUserRole().name(),
                            "roleDisplayName", user.getUserRole().getDisplayName()
                    ),
                    "updatedAt", java.time.LocalDateTime.now()
            );

        } catch (Exception e) {
            log.error("❌ Ошибка обновления данных пользователя: {}", e.getMessage());
            return Map.of(
                    "success", false,
                    "error", "Ошибка обновления данных: " + e.getMessage()
            );
        }
    }

    // ========== PRIVATE МЕТОДЫ ==========

    /**
     * 🔧 Получение конфигурации дашборда для роли
     */
    private Map<String, Object> getDashboardConfig(UserRole role) {
        return switch (role) {
            case ADMIN -> Map.of(
                    "title", "Панель администратора",
                    "sections", List.of("analytics", "users", "restaurants", "categories", "system"),
                    "permissions", List.of("manage_all", "view_analytics", "manage_users"),
                    "theme", "admin"
            );

            case BASE_USER -> Map.of(
                    "title", "Личный кабинет",
                    "sections", List.of("orders", "favorites", "profile", "addresses"),
                    "permissions", List.of("view_orders", "manage_profile"),
                    "theme", "user"
            );

            case BUSINESS_USER -> Map.of(
                    "title", "Кабинет владельца",
                    "sections", List.of("restaurant", "orders", "menu", "analytics", "settings"),
                    "permissions", List.of("manage_restaurant", "view_orders", "manage_menu"),
                    "theme", "business"
            );

            case COURIER -> Map.of(
                    "title", "Кабинет курьера",
                    "sections", List.of("deliveries", "available-orders", "earnings", "profile"),
                    "permissions", List.of("manage_deliveries", "view_orders"),
                    "theme", "courier"
            );
        };
    }

    /**
     * 🔐 Проверка доступа к секции
     */
    private boolean hasAccessToSection(UserRole role, String section) {
        Map<UserRole, List<String>> accessMap = Map.of(
                UserRole.ADMIN, List.of("analytics", "users", "restaurants", "categories", "system"),
                UserRole.BASE_USER, List.of("orders", "favorites", "profile", "addresses"),
                UserRole.BUSINESS_USER, List.of("restaurant", "orders", "menu", "analytics", "settings"),
                UserRole.COURIER, List.of("deliveries", "available-orders", "earnings", "profile")
        );

        return accessMap.getOrDefault(role, List.of()).contains(section);
    }
}