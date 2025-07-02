
package com.example.foodfrontendservice.controller;

import com.example.foodfrontendservice.dto.UserResponseDto;
import com.example.foodfrontendservice.enums.UserRole;
import com.example.foodfrontendservice.service.UserIntegrationService;

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
public class DashboardController {

    private final UserIntegrationService userIntegrationService;

    @GetMapping
    public String dashboard(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        return processDashboardRequest(request, model, redirectAttributes, "GET");
    }


    @PostMapping
    @ResponseBody
    public String dashboardPost(HttpServletRequest request,
                                @RequestBody(required = false) Map<String, Object> requestBody) {

        log.info("🏠 POST запрос дашборда от IP: {} с телом: {}",
                getClientIP(request), requestBody);

        try {

            String token = extractTokenFromHeaders(request);

            if (token == null) {

                token = extractToken(request);
            }

            if (token == null) {
                log.warn("❌ Токен не найден в POST запросе");
                return "redirect:/login?error=no_token";
            }

            log.debug("🔑 Токен найден в POST (длина: {})", token.length());

            // Устанавливаем токен и атрибуты для Feign
            request.setAttribute("Authorization", "Bearer " + token);

            // ИСПРАВЛЕНО: Передаем полный Bearer токен
            UserResponseDto user = userIntegrationService.getCurrentUser("Bearer " + token);

            if (user == null || user.getUserRole() == null) {
                log.warn("❌ Не удалось получить данные пользователя");
                return "redirect:/login?error=invalid_user";
            }


            request.getSession().setAttribute("currentUser", user);
            request.getSession().setAttribute("authToken", token);
            request.setAttribute("currentUser", user);

            log.info("✅ POST: Пользователь {} (роль: {}) аутентифицирован",
                    user.getEmail(), user.getUserRole());


            String roleBasedPath = "/dashboard/" + user.getUserRole().name();
            log.info("🔄 POST перенаправление на: {}", roleBasedPath);

            return "redirect:" + roleBasedPath;

        } catch (Exception e) {
            log.error("💥 Ошибка в POST дашборде: {}", e.getMessage(), e);
            return "redirect:/login?error=dashboard_error";
        }
    }

    /**
     * 🔑 Извлечение токена из заголовков (для POST запросов)
     */
    private String extractTokenFromHeaders(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            log.debug("✅ Токен найден в Authorization заголовке");
            return authHeader.substring(7);
        }

        // 2. Кастомный заголовок X-Auth-Token
        String customToken = request.getHeader("X-Auth-Token");
        if (customToken != null && !customToken.trim().isEmpty()) {
            log.debug("✅ Токен найден в X-Auth-Token заголовке");
            return customToken;
        }

        log.debug("❌ Токен не найден в заголовках");
        return null;
    }

    /**
     * 🔧 Общая логика обработки запросов дашборда
     */
    private String processDashboardRequest(HttpServletRequest request, Model model,
                                           RedirectAttributes redirectAttributes, String method) {

        log.info("🏠 Запрос главного дашборда ({}) от IP: {}", method, getClientIP(request));

        try {
            // Получаем токен из разных источников
            String token = extractToken(request);

            if (token == null) {
                log.warn("❌ Токен авторизации не найден");
                return "redirect:/login?error=no_token";
            }

            log.debug("🔑 Токен найден (длина: {}), получаем профиль пользователя...", token.length());

            // Устанавливаем токен в атрибуты запроса для Feign
            request.setAttribute("Authorization", "Bearer " + token);

            // Получаем профиль пользователя через сервис
            UserResponseDto user = userIntegrationService.getCurrentUser("Bearer " + token);

            if (user == null || user.getUserRole() == null) {
                log.warn("❌ Не удалось получить данные пользователя или роль отсутствует");
                return "redirect:/login?error=invalid_user";
            }


            request.getSession().setAttribute("currentUser", user);
            request.getSession().setAttribute("authToken", token);

            // 🏷️ Устанавливаем атрибуты для текущего запроса (для Feign)
            request.setAttribute("currentUser", user);

            log.info("✅ Пользователь {} (роль: {}) успешно аутентифицирован",
                    user.getEmail(), user.getUserRole());

            // Перенаправляем на роль-специфичный дашборд
            String roleBasedPath = "/dashboard/" + user.getUserRole().name();
            log.info("🔄 Перенаправление {} на {}", user.getEmail(), roleBasedPath);

            return "redirect:" + roleBasedPath;

        } catch (Exception e) {
            log.error("💥 Ошибка в главном дашборде: {}", e.getMessage(), e);
            return "redirect:/login?error=dashboard_error";
        }
    }


    @GetMapping("/BASE_USER")
    public String baseUserDashboard(HttpServletRequest request, Model model) {
        log.info("🛒 Загрузка дашборда покупателя");
        return loadRoleSpecificDashboard(request, model, UserRole.BASE_USER, "dashboard/user");
    }


    @GetMapping("/BUSINESS_USER")
    public String businessUserDashboard(HttpServletRequest request, Model model) {
        log.info("🏪 Загрузка дашборда владельца бизнеса");
        return loadRoleSpecificDashboard(request, model, UserRole.BUSINESS_USER, "dashboard/business");
    }


    @GetMapping("/COURIER")
    public String courierDashboard(HttpServletRequest request, Model model) {
        log.info("🚚 Загрузка дашборда курьера");
        return loadRoleSpecificDashboard(request, model, UserRole.COURIER, "dashboard/courier");
    }


    @GetMapping("/ADMIN")
    public String adminDashboard(HttpServletRequest request, Model model) {
        log.info("👑 Загрузка дашборда администратора");
        return loadRoleSpecificDashboard(request, model, UserRole.ADMIN, "dashboard/admin");
    }

    /**
     * 🔧 Универсальный метод загрузки роль-специфичного дашборда
     */
    private String loadRoleSpecificDashboard(HttpServletRequest request, Model model,
                                             UserRole expectedRole, String viewName) {
        try {
            // Пытаемся получить пользователя из сессии
            UserResponseDto user = (UserResponseDto) request.getSession().getAttribute("currentUser");
            String token = (String) request.getSession().getAttribute("authToken");

            // Если нет в сессии, получаем заново
            if (user == null || token == null) {
                token = extractToken(request);
                if (token != null) {
                    user = userIntegrationService.getCurrentUser("Bearer " + token);
                    if (user != null) {
                        request.getSession().setAttribute("currentUser", user);
                        request.getSession().setAttribute("authToken", token);
                    }
                }
            }

            if (user == null) {
                log.warn("❌ Пользователь не найден для роли: {}", expectedRole);
                return "redirect:/login?error=user_not_found";
            }

            // Проверяем соответствие роли
            if (user.getUserRole() != expectedRole) {
                log.warn("🚫 Несоответствие ролей. Ожидается: {}, получена: {}",
                        expectedRole, user.getUserRole());
                return "redirect:/dashboard";
            }

            // Добавляем данные в модель
            model.addAttribute("user", user);
            model.addAttribute("userRole", user.getUserRole());
            model.addAttribute("roleDisplayName", user.getUserRole().getDisplayName());

            // Добавляем токен для AJAX запросов
            model.addAttribute("authToken", token);

            log.info("✅ Успешная загрузка дашборда {} для {}", expectedRole, user.getEmail());

            return viewName;

        } catch (Exception e) {
            log.error("💥 Ошибка загрузки дашборда для роли {}: {}", expectedRole, e.getMessage(), e);
            model.addAttribute("error", "Ошибка загрузки дашборда");
            return "error/dashboard";
        }
    }

    /**
     * 🔑 Извлечение токена из запроса с детальным логированием
     */
    private String extractToken(HttpServletRequest request) {
        log.debug("🔍 Поиск токена авторизации...");

        // 1. Authorization заголовок (приоритет)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            log.debug("✅ Токен найден в Authorization заголовке");
            return authHeader.substring(7);
        }

        // 2. Параметр запроса (при первичном переходе)
        String tokenParam = request.getParameter("token");
        if (tokenParam != null && !tokenParam.isEmpty()) {
            log.debug("✅ Токен найден в параметрах запроса");
            return tokenParam;
        }

        // 3. Кастомный заголовок
        String customHeader = request.getHeader("X-Auth-Token");
        if (customHeader != null && !customHeader.isEmpty()) {
            log.debug("✅ Токен найден в X-Auth-Token заголовке");
            return customHeader;
        }

        // 4. Из сессии
        String sessionToken = (String) request.getSession().getAttribute("authToken");
        if (sessionToken != null && !sessionToken.isEmpty()) {
            log.debug("✅ Токен найден в сессии");
            return sessionToken;
        }

        log.warn("❌ Токен не найден ни в одном из источников");
        return null;
    }

    /**
     * 🌐 Получение IP адреса клиента
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
}
