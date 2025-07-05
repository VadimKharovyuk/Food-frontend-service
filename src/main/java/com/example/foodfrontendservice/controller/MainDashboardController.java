package com.example.foodfrontendservice.controller;
import com.example.foodfrontendservice.config.JwtUtil;
import com.example.foodfrontendservice.service.DashboardService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/dashboard")
public class MainDashboardController {

    private final DashboardService dashboardService;
    private final JwtUtil jwtUtil;


    @GetMapping
    public String dashboard(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        log.info("🏠 GET запрос главного дашборда от IP: {}", getClientIP(request));


        String token = extractTokenFromRequest(request);

        if (token == null || !jwtUtil.isTokenValid(token)) {
            log.warn("❌ Недействительный или отсутствующий токен при доступе к dashboard");
            log.warn("   Token present: {}", token != null);
            log.warn("   Token valid: {}", token != null && jwtUtil.isTokenValid(token));

            redirectAttributes.addAttribute("expired", "true");
            redirectAttributes.addAttribute("reason", "invalid_token");
            return "redirect:/login";
        }

        log.info("✅ Токен валиден, обрабатываем запрос дашборда");
        return dashboardService.processDashboardRedirect(request);
    }

    /**
     * 🏠 POST запрос дашборда с токеном (для JavaScript)
     */
    @PostMapping
    @ResponseBody
    public String dashboardPost(HttpServletRequest request,
                                @RequestBody(required = false) Map<String, Object> requestBody) {
        log.info("🏠 POST запрос главного дашборда от IP: {}", getClientIP(request));

        // ✅ ВАЛИДИРУЕМ ТОКЕН ДЛЯ POST ЗАПРОСОВ
        String token = extractTokenFromRequest(request);

        if (token == null || !jwtUtil.isTokenValid(token)) {
            log.warn("❌ Недействительный или отсутствующий токен при POST запросе к dashboard");

            // Для AJAX запросов возвращаем JSON с ошибкой
            return """
                {
                    "success": false,
                    "error": "INVALID_TOKEN",
                    "message": "Токен недействителен или отсутствует",
                    "redirectUrl": "/login?expired=true"
                }
                """;
        }

        log.info("✅ Токен валиден для POST запроса, обрабатываем");
        return dashboardService.processDashboardPost(request, requestBody);
    }

    /**
     * ✅ НОВЫЙ метод для извлечения токена из разных источников
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        // 1. Проверяем Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            log.debug("🔍 Токен найден в Authorization header");
            return authHeader.substring(7);
        }

        // 2. Проверяем X-Auth-Token header
        String xAuthToken = request.getHeader("X-Auth-Token");
        if (xAuthToken != null && !xAuthToken.trim().isEmpty()) {
            log.debug("🔍 Токен найден в X-Auth-Token header");
            return xAuthToken;
        }

        // 3. Проверяем URL параметры (для fallback редиректа)
        String tokenParam = request.getParameter("token");
        if (tokenParam != null && !tokenParam.trim().isEmpty()) {
            log.debug("🔍 Токен найден в URL параметрах");
            return tokenParam;
        }

        // 4. Проверяем в теле POST запроса (если это form data)
        String formToken = request.getParameter("authToken");
        if (formToken != null && !formToken.trim().isEmpty()) {
            log.debug("🔍 Токен найден в форме");
            return formToken;
        }

        log.debug("❌ Токен не найден ни в одном из источников");
        return null;
    }

    /**
     * ✅ ДОПОЛНИТЕЛЬНЫЙ метод для получения IP клиента
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }

    /**
     * ✅ НОВЫЙ endpoint для проверки токена (для JavaScript)
     */
    @GetMapping("/validate-token")
    @ResponseBody
    public Map<String, Object> validateToken(HttpServletRequest request) {
        log.debug("🔍 Проверка валидности токена");

        String token = extractTokenFromRequest(request);

        if (token == null) {
            return Map.of(
                    "valid", false,
                    "error", "TOKEN_NOT_FOUND",
                    "message", "Токен не найден"
            );
        }

        if (!jwtUtil.isTokenValid(token)) {
            return Map.of(
                    "valid", false,
                    "error", "TOKEN_INVALID",
                    "message", "Токен недействителен или истек"
            );
        }

        // Получаем информацию о пользователе из токена
        try {
            String email = jwtUtil.getEmailFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);

            return Map.of(
                    "valid", true,
                    "email", email,
                    "role", role,
                    "message", "Токен действителен"
            );
        } catch (Exception e) {
            log.error("❌ Ошибка извлечения данных из токена: {}", e.getMessage());
            return Map.of(
                    "valid", false,
                    "error", "TOKEN_PARSE_ERROR",
                    "message", "Ошибка обработки токена"
            );
        }
    }

}