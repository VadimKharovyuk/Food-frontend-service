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

        // ✅ НОВЫЙ ПОДХОД: Не проверяем токен на сервере при первом запросе
        // Токен будет проверен через JavaScript API

        // ✅ Передаем базовую конфигурацию без проверки токена
        model.addAttribute("needsAuth", true);
        model.addAttribute("apiBaseUrl", "http://localhost:8082");

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

        try {
            // ✅ Возвращаем данные в зависимости от роли и секции
            Map<String, Object> sectionData = loadSectionData(user.getUserRole(), section, user);

            // Добавляем метаданные
            sectionData.put("_meta", Map.of(
                    "section", section,
                    "userRole", user.getUserRole().name(),
                    "loadedAt", java.time.LocalDateTime.now(),
                    "userId", user.getId()
            ));

            return sectionData;

        } catch (Exception e) {
            log.error("❌ Ошибка загрузки данных секции {}: {}", section, e.getMessage());
            return Map.of(
                    "error", "Ошибка загрузки данных секции",
                    "code", "DATA_LOAD_ERROR",
                    "section", section,
                    "message", e.getMessage()
            );
        }
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

    /**
     * 📋 Загрузка данных секции
     */
    private Map<String, Object> loadSectionData(UserRole role, String section, UserResponseDto user) {
        // ✅ В зависимости от роли и секции возвращаем нужные данные
        return switch (role) {
            case ADMIN -> loadAdminSectionData(section);
            case BASE_USER -> loadUserSectionData(section, user);
            case BUSINESS_USER -> loadBusinessSectionData(section, user);
            case COURIER -> loadCourierSectionData(section, user);
        };
    }

    // ========== ДАННЫЕ ДЛЯ РОЛЕЙ ==========

    private Map<String, Object> loadAdminSectionData(String section) {
        return switch (section) {
            case "analytics" -> Map.of(
                    "totalUsers", 1247,
                    "totalOrders", 5432,
                    "totalRevenue", "234560₽",
                    "growthRate", "+12.5%",
                    "period", "Текущий месяц"
            );

            case "users" -> Map.of(
                    "users", List.of(
                            Map.of("id", 1, "name", "Иван Петров", "role", "BASE_USER", "status", "active", "lastLogin", "2 часа назад"),
                            Map.of("id", 2, "name", "Анна Сидорова", "role", "BUSINESS_USER", "status", "active", "lastLogin", "1 день назад"),
                            Map.of("id", 3, "name", "Петр Курьеров", "role", "COURIER", "status", "active", "lastLogin", "30 минут назад")
                    ),
                    "totalCount", 1247,
                    "activeUsers", 987,
                    "newUsersToday", 12
            );

            case "restaurants" -> Map.of(
                    "restaurants", List.of(
                            Map.of("id", 1, "name", "Пиццерия Мама Миа", "status", "active", "owner", "Иван Петров", "rating", 4.8),
                            Map.of("id", 2, "name", "Tokyo Sushi", "status", "pending", "owner", "Анна Сидорова", "rating", 4.6),
                            Map.of("id", 3, "name", "Burger Street", "status", "blocked", "owner", "Михаил Петров", "rating", 3.2)
                    ),
                    "totalCount", 86,
                    "pendingApproval", 5,
                    "activeRestaurants", 78
            );

            case "categories" -> Map.of(
                    "categories", List.of(
                            Map.of("id", 1, "name", "Пицца", "restaurantsCount", 15, "status", "active"),
                            Map.of("id", 2, "name", "Суши", "restaurantsCount", 8, "status", "active"),
                            Map.of("id", 3, "name", "Бургеры", "restaurantsCount", 23, "status", "active")
                    ),
                    "totalCount", 12
            );

            case "system" -> Map.of(
                    "systemInfo", Map.of(
                            "version", "1.0.0",
                            "uptime", "15 дней",
                            "activeConnections", 1250,
                            "memoryUsage", "68%",
                            "diskUsage", "45%"
                    ),
                    "recentLogs", List.of(
                            Map.of("time", "10:30", "level", "INFO", "message", "Новый пользователь зарегистрирован"),
                            Map.of("time", "10:25", "level", "WARN", "message", "Превышен лимит запросов для IP"),
                            Map.of("time", "10:20", "level", "ERROR", "message", "Ошибка подключения к базе данных")
                    )
            );

            default -> Map.of("error", "Секция не найдена");
        };
    }

    private Map<String, Object> loadUserSectionData(String section, UserResponseDto user) {
        return switch (section) {
            case "orders" -> Map.of(
                    "orders", List.of(
                            Map.of("id", 1247, "restaurant", "Пиццерия Мама Миа", "status", "delivered", "total", "890₽", "date", "Сегодня 14:30"),
                            Map.of("id", 1248, "restaurant", "Tokyo Sushi", "status", "cooking", "total", "1200₽", "date", "Сегодня 15:45"),
                            Map.of("id", 1249, "restaurant", "Burger Street", "status", "cancelled", "total", "650₽", "date", "Вчера 19:20")
                    ),
                    "totalCount", 15,
                    "totalSpent", "15420₽",
                    "avgOrderValue", "1028₽"
            );

            case "favorites" -> Map.of(
                    "favorites", List.of(
                            Map.of("id", 1, "name", "Пиццерия Мама Миа", "cuisine", "Итальянская", "rating", 4.9, "deliveryTime", "25-35 мин"),
                            Map.of("id", 2, "name", "Tokyo Sushi", "cuisine", "Японская", "rating", 4.7, "deliveryTime", "30-40 мин"),
                            Map.of("id", 3, "name", "Burger Street", "cuisine", "Американская", "rating", 4.5, "deliveryTime", "15-25 мин")
                    ),
                    "totalCount", 5
            );

            case "profile" -> Map.of(
                    "profile", Map.of(
                            "id", user.getId(),
                            "email", user.getEmail(),
                            "firstName", user.getFirstName(),
                            "lastName", user.getLastName(),
                            "phone", "+7 XXX XXX-XX-XX",
                            "registeredAt", "15 января 2024",
                            "totalOrders", 15,
                            "favoriteRestaurants", 5
                    )
            );

            case "addresses" -> Map.of(
                    "addresses", List.of(
                            Map.of("id", 1, "title", "Дом", "address", "ул. Пушкина, д. 10, кв. 5", "isDefault", true),
                            Map.of("id", 2, "title", "Работа", "address", "пр. Ленина, д. 50, оф. 205", "isDefault", false)
                    ),
                    "totalCount", 2
            );

            default -> Map.of("error", "Секция не найдена");
        };
    }

    private Map<String, Object> loadBusinessSectionData(String section, UserResponseDto user) {
        return switch (section) {
            case "restaurant" -> Map.of(
                    "restaurant", Map.of(
                            "name", "Ресторан пользователя",
                            "status", "active",
                            "rating", 4.8,
                            "ordersToday", 23,
                            "revenueToday", "15420₽",
                            "avgDeliveryTime", "28 мин"
                    )
            );

            case "orders" -> Map.of(
                    "orders", List.of(
                            Map.of("id", 1247, "items", "2x Пицца Маргарита, 1x Кока-кола", "status", "cooking", "total", "890₽", "time", "12 мин назад"),
                            Map.of("id", 1248, "items", "1x Бургер Делюкс, 1x Картофель фри", "status", "new", "total", "650₽", "time", "8 мин назад"),
                            Map.of("id", 1249, "items", "3x Суши сет, 2x Мисо суп", "status", "ready", "total", "1850₽", "time", "5 мин назад")
                    ),
                    "totalCount", 47,
                    "newOrders", 3,
                    "cookingOrders", 8
            );

            case "menu" -> Map.of(
                    "menuItems", List.of(
                            Map.of("id", 1, "name", "Пицца Маргарита", "price", "450₽", "category", "Пицца", "available", true),
                            Map.of("id", 2, "name", "Бургер Делюкс", "price", "380₽", "category", "Бургеры", "available", true),
                            Map.of("id", 3, "name", "Суши сет", "price", "850₽", "category", "Суши", "available", false)
                    ),
                    "totalItems", 45,
                    "availableItems", 42
            );

            case "analytics" -> Map.of(
                    "revenue", Map.of(
                            "today", "15420₽",
                            "week", "98750₽",
                            "month", "425630₽"
                    ),
                    "orders", Map.of(
                            "today", 23,
                            "week", 156,
                            "month", 687
                    ),
                    "topDishes", List.of(
                            Map.of("name", "Пицца Маргарита", "orders", 45),
                            Map.of("name", "Бургер Делюкс", "orders", 38),
                            Map.of("name", "Суши сет", "orders", 29)
                    )
            );

            default -> Map.of("error", "Секция не найдена");
        };
    }

    private Map<String, Object> loadCourierSectionData(String section, UserResponseDto user) {
        return switch (section) {
            case "deliveries" -> Map.of(
                    "activeDeliveries", List.of(
                            Map.of("id", 1247, "restaurant", "Пиццерия Мама Миа", "customer", "Иван П.", "status", "picked_up", "distance", "2.1 км", "payment", "890₽"),
                            Map.of("id", 1248, "restaurant", "Tokyo Sushi", "customer", "Анна С.", "status", "waiting", "distance", "1.5 км", "payment", "1200₽")
                    ),
                    "totalCount", 2,
                    "completedToday", 8
            );

            case "available-orders" -> Map.of(
                    "availableOrders", List.of(
                            Map.of("id", 1250, "restaurant", "Burger Street", "distance", "1.2 км", "payment", "750₽", "tip", "120₽"),
                            Map.of("id", 1251, "restaurant", "Кафе Уют", "distance", "2.8 км", "payment", "1100₽", "tip", "150₽"),
                            Map.of("id", 1252, "restaurant", "Пиццерия Мама Миа", "distance", "0.8 км", "payment", "650₽", "tip", "80₽")
                    ),
                    "totalAvailable", 15
            );

            case "earnings" -> Map.of(
                    "today", "1840₽",
                    "thisWeek", "12450₽",
                    "thisMonth", "54230₽",
                    "deliveriesToday", 12,
                    "avgDeliveryTime", "24 мин",
                    "rating", 4.9,
                    "tips", Map.of(
                            "today", "340₽",
                            "week", "2150₽",
                            "month", "8970₽"
                    )
            );

            case "profile" -> Map.of(
                    "profile", Map.of(
                            "id", user.getId(),
                            "email", user.getEmail(),
                            "firstName", user.getFirstName(),
                            "lastName", user.getLastName(),
                            "phone", "+7 XXX XXX-XX-XX",
                            "vehicle", "Велосипед",
                            "workingSince", "15 марта 2024",
                            "totalDeliveries", 892,
                            "rating", 4.9
                    )
            );

            default -> Map.of("error", "Секция не найдена");
        };
    }
}