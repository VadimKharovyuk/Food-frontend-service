package com.example.foodfrontendservice.controller;

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
@RequestMapping("/dashboard/BUSINESS_USER")
@RequiredArgsConstructor
@Slf4j
public class BusinessUserDashboardController {

    private final DashboardService dashboardService;

    /**
     * 🏪 Дашборд владельца бизнеса
     */
    @GetMapping
    public String businessUserDashboard(HttpServletRequest request, Model model) {
        log.info("🏪 Загрузка дашборда владельца бизнеса");
        return dashboardService.loadRoleSpecificDashboard(request, model, UserRole.BUSINESS_USER, "dashboard/business");
    }

    /**
     * 📦 AJAX endpoint для активных заказов ресторана
     */
    @GetMapping("/active-orders")
    @ResponseBody
    public Map<String, Object> getActiveOrders(HttpServletRequest request) {
        log.info("📦 Запрос активных заказов ресторана");

        if (!dashboardService.hasRole(request, UserRole.BUSINESS_USER)) {
            return Map.of("success", false, "message", "Недостаточно прав");
        }

        // TODO: Реализовать получение заказов через OrderService
        return Map.of(
                "success", true,
                "orders", List.of(
                        Map.of("id", 1247, "items", "2x Пицца Маргарита, 1x Кока-кола", "status", "cooking", "total", "890₽", "time", "12 мин назад"),
                        Map.of("id", 1248, "items", "1x Бургер Делюкс, 1x Картофель фри", "status", "new", "total", "650₽", "time", "8 мин назад")
                )
        );
    }

    /**
     * 📊 AJAX endpoint для статистики ресторана
     */
    @GetMapping("/stats")
    @ResponseBody
    public Map<String, Object> getRestaurantStats(HttpServletRequest request) {
        log.info("📊 Запрос статистики ресторана");

        if (!dashboardService.hasRole(request, UserRole.BUSINESS_USER)) {
            return Map.of("success", false, "message", "Недостаточно прав");
        }

        // TODO: Реализовать получение статистики
        return Map.of(
                "success", true,
                "stats", Map.of(
                        "ordersToday", 47,
                        "revenueToday", "15240₽",
                        "rating", 4.7,
                        "averageTime", "23 мин"
                )
        );
    }

    /**
     * ⚙️ AJAX endpoint для обновления настроек ресторана
     */
    @PostMapping("/settings")
    @ResponseBody
    public Map<String, Object> updateRestaurantSettings(HttpServletRequest request,
                                                        @RequestBody Map<String, Object> settings) {
        log.info("⚙️ Обновление настроек ресторана: {}", settings);

        if (!dashboardService.hasRole(request, UserRole.BUSINESS_USER)) {
            return Map.of("success", false, "message", "Недостаточно прав");
        }

        // TODO: Реализовать обновление настроек через RestaurantService
        return Map.of(
                "success", true,
                "message", "Настройки успешно обновлены"
        );
    }
}
