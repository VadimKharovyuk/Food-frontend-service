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
@RequestMapping("/dashboard/COURIER")
@RequiredArgsConstructor
@Slf4j
public class CourierDashboardController {

    private final DashboardService dashboardService;

    /**
     * 🚚 Дашборд курьера
     */
    @GetMapping
    public String courierDashboard(HttpServletRequest request, Model model) {
        log.info("🚚 Загрузка дашборда курьера");
        return dashboardService.loadRoleSpecificDashboard(request, model, UserRole.COURIER, "dashboard/courier");
    }

    /**
     * 📦 AJAX endpoint для активных доставок
     */
    @GetMapping("/active-deliveries")
    @ResponseBody
    public Map<String, Object> getActiveDeliveries(HttpServletRequest request) {
        log.info("📦 Запрос активных доставок курьера");

        if (!dashboardService.hasRole(request, UserRole.COURIER)) {
            return Map.of("success", false, "message", "Недостаточно прав");
        }

        // TODO: Реализовать получение доставок через DeliveryService
        return Map.of(
                "success", true,
                "deliveries", List.of(
                        Map.of("id", 1247, "restaurant", "Пиццерия Мама Миа", "customer", "Иван Петров", "status", "picked_up", "total", "890₽"),
                        Map.of("id", 1249, "restaurant", "Tokyo Sushi", "customer", "Анна Сидорова", "status", "waiting", "total", "1200₽")
                )
        );
    }

    /**
     * 📋 AJAX endpoint для доступных заказов
     */
    @GetMapping("/available-orders")
    @ResponseBody
    public Map<String, Object> getAvailableOrders(HttpServletRequest request) {
        log.info("📋 Запрос доступных заказов для курьера");

        if (!dashboardService.hasRole(request, UserRole.COURIER)) {
            return Map.of("success", false, "message", "Недостаточно прав");
        }

        // TODO: Реализовать получение доступных заказов
        return Map.of(
                "success", true,
                "orders", List.of(
                        Map.of("id", 1250, "restaurant", "Burger King", "distance", "1.2 км", "payment", "750₽ + 120₽ чаевые"),
                        Map.of("id", 1251, "restaurant", "Кафе Уют", "distance", "2.8 км", "payment", "1100₽ + 150₽ чаевые")
                )
        );
    }

    /**
     * ✅ AJAX endpoint для принятия заказа
     */
    @PostMapping("/accept-order")
    @ResponseBody
    public Map<String, Object> acceptOrder(HttpServletRequest request,
                                           @RequestBody Map<String, Object> orderData) {
        log.info("✅ Принятие заказа курьером: {}", orderData);

        if (!dashboardService.hasRole(request, UserRole.COURIER)) {
            return Map.of("success", false, "message", "Недостаточно прав");
        }

        // TODO: Реализовать принятие заказа через DeliveryService
        return Map.of(
                "success", true,
                "message", "Заказ успешно принят"
        );
    }

    /**
     * 📊 AJAX endpoint для статистики курьера
     */
    @GetMapping("/stats")
    @ResponseBody
    public Map<String, Object> getCourierStats(HttpServletRequest request) {
        log.info("📊 Запрос статистики курьера");

        if (!dashboardService.hasRole(request, UserRole.COURIER)) {
            return Map.of("success", false, "message", "Недостаточно прав");
        }

        // TODO: Реализовать получение статистики курьера
        return Map.of(
                "success", true,
                "stats", Map.of(
                        "deliveriesToday", 12,
                        "earningsToday", "1840₽",
                        "rating", 4.9,
                        "averageRoute", "2.3 км"
                )
        );
    }
}

