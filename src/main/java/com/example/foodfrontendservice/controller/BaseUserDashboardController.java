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
@RequestMapping("/dashboard/BASE_USER")
@Slf4j
public class BaseUserDashboardController extends BaseDashboardController {

    public BaseUserDashboardController(DashboardService dashboardService) {
        super(dashboardService);
    }

    /**
     * 🛒 Дашборд покупателя
     */
    @GetMapping
    public String baseUserDashboard(HttpServletRequest request, Model model) {
        log.info("🛒 Загрузка дашборда покупателя");
        return dashboardService.loadRoleSpecificDashboard(request, model, UserRole.BASE_USER, "dashboard/user");
    }

    /**
     * 📋 AJAX endpoint для получения заказов пользователя
     */
    @GetMapping("/orders")
    @ResponseBody
    public Map<String, Object> getUserOrders(HttpServletRequest request) {
        log.info("📋 Запрос заказов покупателя");

        if (!dashboardService.hasRole(request, UserRole.BASE_USER)) {
            return Map.of("success", false, "message", "Недостаточно прав");
        }

        // TODO: Реализовать получение заказов через OrderService
        return Map.of(
                "success", true,
                "orders", List.of(
                        Map.of("id", 1, "restaurant", "Пиццерия Мама Миа", "status", "В пути", "total", "890₽"),
                        Map.of("id", 2, "restaurant", "Tokyo Sushi", "status", "Доставлено", "total", "1200₽")
                )
        );
    }

    /**
     * ❤️ AJAX endpoint для избранных ресторанов
     */
    @GetMapping("/favorites")
    @ResponseBody
    public Map<String, Object> getFavoriteRestaurants(HttpServletRequest request) {
        log.info("❤️ Запрос избранных ресторанов");

        if (!dashboardService.hasRole(request, UserRole.BASE_USER)) {
            return Map.of("success", false, "message", "Недостаточно прав");
        }

        // TODO: Реализовать получение избранного через RestaurantService
        return Map.of(
                "success", true,
                "favorites", List.of(
                        Map.of("id", 1, "name", "Пиццерия Мама Миа", "rating", 4.9, "cuisine", "Итальянская"),
                        Map.of("id", 2, "name", "Tokyo Sushi", "rating", 4.7, "cuisine", "Японская")
                )
        );
    }
}