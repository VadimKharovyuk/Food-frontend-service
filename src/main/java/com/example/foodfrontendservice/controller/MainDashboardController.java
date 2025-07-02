package com.example.foodfrontendservice.controller;
import com.example.foodfrontendservice.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;


@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/dashboard")
public class MainDashboardController {

    private final DashboardService dashboardService;

    /**
     * 🏠 GET запрос главного дашборда - перенаправляет на роль-специфичный дашборд
     */
    @GetMapping
    public String dashboard(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        log.info("🏠 GET запрос главного дашборда");
        return dashboardService.processDashboardRedirect(request);
    }

    /**
     * 🏠 POST запрос дашборда с токеном (для JavaScript)
     */
    @PostMapping
    @ResponseBody
    public String dashboardPost(HttpServletRequest request,
                                @RequestBody(required = false) Map<String, Object> requestBody) {
        log.info("🏠 POST запрос главного дашборда");
        return dashboardService.processDashboardPost(request, requestBody);
    }
}