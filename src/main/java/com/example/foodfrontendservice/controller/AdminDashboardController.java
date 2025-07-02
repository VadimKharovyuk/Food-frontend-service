package com.example.foodfrontendservice.controller;
import com.example.foodfrontendservice.Client.StoreServiceClient;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.StoreResponseWrapper;
import com.example.foodfrontendservice.enums.UserRole;
import com.example.foodfrontendservice.service.DashboardService;
import com.example.foodfrontendservice.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dashboard/ADMIN")
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardController {

    private final DashboardService dashboardService;
    private final StoreService storeService;


    @GetMapping("/restaurants")
    public String restaurants(HttpServletRequest request, Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size) {

        log.info("🏪 Загрузка страницы управления ресторанами (page: {}, size: {})", page, size);

        // Проверяем права администратора
        if (!dashboardService.hasRole(request, UserRole.ADMIN)) {
            log.warn("🚫 Попытка доступа к управлению ресторанами без прав администратора");
            return "redirect:/dashboard?error=access_denied";
        }

        try {
            // Получаем базовые данные для дашборда
            String dashboardView = dashboardService.loadRoleSpecificDashboard(
                    request, model, UserRole.ADMIN, "dashboard/admin-restaurants"
            );


            if (dashboardView.startsWith("redirect:")) {
                return dashboardView;
            }


            StoreResponseWrapper storeResponse = storeService.getActiveStores(page, size);

            if (storeResponse != null && Boolean.TRUE.equals(storeResponse.getSuccess())) {

                int totalPages = calculateTotalPages(storeResponse.getTotalCount(), size);

                model.addAttribute("stores", storeResponse.getStores());
                model.addAttribute("totalPages", totalPages);
                model.addAttribute("currentPage", page);
                model.addAttribute("pageSize", size);
                model.addAttribute("totalCount", storeResponse.getTotalCount());


                model.addAttribute("hasNext", Boolean.TRUE.equals(storeResponse.getHasNext()));
                model.addAttribute("hasPrevious", Boolean.TRUE.equals(storeResponse.getHasPrevious()));
                model.addAttribute("nextPage", page + 1);
                model.addAttribute("previousPage", page - 1);

                log.info("✅ Загружено {} ресторанов для страницы {}",
                        storeResponse.getStores() != null ? storeResponse.getStores().size() : 0, page);
            } else {
                log.warn("⚠️ Не удалось получить данные о ресторанах: {}",
                        storeResponse != null ? storeResponse.getMessage() : "null response");


                model.addAttribute("error", "Не удалось загрузить данные о ресторанах");
                model.addAttribute("stores", Collections.emptyList());
                model.addAttribute("totalPages", 0);
                model.addAttribute("currentPage", 0);
                model.addAttribute("totalCount", 0);
                model.addAttribute("hasNext", false);
                model.addAttribute("hasPrevious", false);
                model.addAttribute("nextPage", 0);
                model.addAttribute("previousPage", 0);
            }

            return "admin/admin-restaurants";

        } catch (Exception e) {
            log.error("💥 Ошибка загрузки страницы управления ресторанами: {}", e.getMessage(), e);
            model.addAttribute("error", "Ошибка загрузки данных");
            model.addAttribute("stores", Collections.emptyList());
            model.addAttribute("totalPages", 0);
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalCount", 0);
            model.addAttribute("hasNext", false);
            model.addAttribute("hasPrevious", false);
            return "admin/admin-restaurants";
        }
    }


    /**
     * Вспомогательный метод для расчета общего количества страниц
     */
    private int calculateTotalPages(Integer totalCount, int pageSize) {
        if (totalCount == null || totalCount <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalCount / pageSize);
    }


        /**
         * 🏪 AJAX endpoint для управления ресторанами
         */
    @GetMapping("/restaurants-count")
    @ResponseBody
    public Map<String, Object> getRestaurants(HttpServletRequest request,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        log.info("🏪 Запрос списка ресторанов (page: {}, size: {})", page, size);

        if (!dashboardService.hasRole(request, UserRole.ADMIN)) {
            return Map.of("success", false, "message", "Недостаточно прав");
        }

        // TODO: Реализовать получение списка ресторанов
        return Map.of(
                "success", true,
                "restaurants", List.of(
                        Map.of("id", 1, "name", "Пиццерия Мама Миа", "status", "pending", "owner", "Иван Петров"),
                        Map.of("id", 2, "name", "Tokyo Sushi", "status", "active", "owner", "Анна Сидорова"),
                        Map.of("id", 3, "name", "Фаст Фуд Экспресс", "status", "blocked", "owner", "Петр Васильев")
                ),
                "totalPages", 5,
                "currentPage", page
        );
    }


    @GetMapping
    public String adminDashboard(HttpServletRequest request, Model model) {


        log.info("👑 Загрузка дашборда администратора");
        return dashboardService.loadRoleSpecificDashboard(request, model, UserRole.ADMIN, "dashboard/admin");
    }

    /**
     * 👥 AJAX endpoint для статистики пользователей
     */
    @GetMapping("/user-stats")
    @ResponseBody
    public Map<String, Object> getUserStats(HttpServletRequest request) {
        log.info("👥 Запрос статистики пользователей");

        if (!dashboardService.hasRole(request, UserRole.ADMIN)) {
            return Map.of("success", false, "message", "Недостаточно прав");
        }

        // TODO: Реализовать получение статистики через UserService
        return Map.of(
                "success", true,
                "stats", Map.of(
                        "totalUsers", 1247,
                        "baseUsers", 987,
                        "businessUsers", 86,
                        "couriers", 174,
                        "admins", 3
                )
        );
    }


    /**
     * ✅ AJAX endpoint для одобрения ресторана
     */
    @PostMapping("/restaurants/{id}/approve")
    @ResponseBody
    public Map<String, Object> approveRestaurant(HttpServletRequest request,
                                                 @PathVariable Long id) {
        log.info("✅ Одобрение ресторана с ID: {}", id);

        if (!dashboardService.hasRole(request, UserRole.ADMIN)) {
            return Map.of("success", false, "message", "Недостаточно прав");
        }

        // TODO: Реализовать одобрение ресторана через RestaurantService
        return Map.of(
                "success", true,
                "message", "Ресторан успешно одобрен"
        );
    }

    /**
     * ❌ AJAX endpoint для отклонения ресторана
     */
    @PostMapping("/restaurants/{id}/reject")
    @ResponseBody
    public Map<String, Object> rejectRestaurant(HttpServletRequest request,
                                                @PathVariable Long id,
                                                @RequestBody Map<String, String> reasonData) {
        log.info("❌ Отклонение ресторана с ID: {}, причина: {}", id, reasonData.get("reason"));

        if (!dashboardService.hasRole(request, UserRole.ADMIN)) {
            return Map.of("success", false, "message", "Недостаточно прав");
        }

        // TODO: Реализовать отклонение ресторана
        return Map.of(
                "success", true,
                "message", "Ресторан отклонен"
        );
    }

    /**
     * 🚫 AJAX endpoint для блокировки ресторана
     */
    @PostMapping("/restaurants/{id}/block")
    @ResponseBody
    public Map<String, Object> blockRestaurant(HttpServletRequest request,
                                               @PathVariable Long id,
                                               @RequestBody Map<String, String> reasonData) {
        log.info("🚫 Блокировка ресторана с ID: {}", id);

        if (!dashboardService.hasRole(request, UserRole.ADMIN)) {
            return Map.of("success", false, "message", "Недостаточно прав");
        }

        // TODO: Реализовать блокировку ресторана
        return Map.of(
                "success", true,
                "message", "Ресторан заблокирован"
        );
    }

    /**
     * 📊 AJAX endpoint для системной аналитики
     */
    @GetMapping("/analytics")
    @ResponseBody
    public Map<String, Object> getSystemAnalytics(HttpServletRequest request,
                                                  @RequestParam(defaultValue = "day") String period) {
        log.info("📊 Запрос системной аналитики за период: {}", period);

        if (!dashboardService.hasRole(request, UserRole.ADMIN)) {
            return Map.of("success", false, "message", "Недостаточно прав");
        }

        // TODO: Реализовать получение аналитики
        return Map.of(
                "success", true,
                "analytics", Map.of(
                        "period", period,
                        "totalOrders", 5432,
                        "totalRevenue", "234560₽",
                        "averageDeliveryTime", "28 мин",
                        "successRate", "97.2%",
                        "userGrowth", "+12.5%",
                        "orderGrowth", "+8.3%"
                )
        );
    }

    /**
     * 🔧 AJAX endpoint для системных настроек
     */
    @PostMapping("/system-settings")
    @ResponseBody
    public Map<String, Object> updateSystemSettings(HttpServletRequest request,
                                                    @RequestBody Map<String, Object> settings) {
        log.info("🔧 Обновление системных настроек: {}", settings);

        if (!dashboardService.hasRole(request, UserRole.ADMIN)) {
            return Map.of("success", false, "message", "Недостаточно прав");
        }

        // TODO: Реализовать обновление системных настроек
        return Map.of(
                "success", true,
                "message", "Настройки успешно обновлены"
        );
    }
}
