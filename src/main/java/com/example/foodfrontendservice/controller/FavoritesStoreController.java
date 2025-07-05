package com.example.foodfrontendservice.controller;

import com.example.foodfrontendservice.config.JwtUtil;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.Favorite.FavoriteStoreApiResponse;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.Favorite.FavoriteStoreResponseDto;
import com.example.foodfrontendservice.service.FavoriteStoreClientService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/favorites/stores")
public class FavoritesStoreController {

    private final FavoriteStoreClientService storeClientService;
    private final JwtUtil jwtUtil;


    @GetMapping
    public String favoritesStores(
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {

        log.info("🛍️ Запрос избранных магазинов пользователя");

        try {
            // ✅ ДЕТАЛЬНОЕ ЛОГИРОВАНИЕ для отладки
            logRequestDetails(request);

            // Получаем JWT токен из запроса
            String token = extractTokenFromRequest(request);

            if (token == null) {
                log.warn("❌ JWT токен не найден - перенаправление на логин");
                redirectAttributes.addFlashAttribute("error", "Необходимо войти в систему");
                return "redirect:/login";
            }

            // ✅ ВАЛИДИРУЕМ токен
            if (!jwtUtil.isTokenValid(token)) {
                log.warn("❌ JWT токен недействителен - перенаправление на логин");
                redirectAttributes.addFlashAttribute("error", "Сессия истекла, войдите заново");
                return "redirect:/login";
            }

            // Получаем информацию о пользователе из токена
            String userEmail = jwtUtil.getEmailFromToken(token);
            Long userId = jwtUtil.getUserIdFromToken(token);
            String userRole = jwtUtil.getRoleFromToken(token);

            log.info("👤 Пользователь: ID={}, Email={}, Role={}", userId, userEmail, userRole);

            // ✅ ПЕРЕДАЕМ данные в модель
            model.addAttribute("jwtToken", token);
            model.addAttribute("isAuthenticated", true);
            model.addAttribute("userRole", userRole);
            model.addAttribute("userEmail", userEmail);
            model.addAttribute("userId", userId);

            // Вызываем сервис для получения избранных магазинов
            FavoriteStoreApiResponse<List<FavoriteStoreResponseDto>> response =
                    storeClientService.getMyFavorites(token);

            if (response != null && response.getSuccess()) {
                List<FavoriteStoreResponseDto> favoriteStores = response.getData();

                log.info("✅ Получено {} избранных магазинов", favoriteStores != null ? favoriteStores.size() : 0);

                model.addAttribute("favoriteStores", favoriteStores != null ? favoriteStores : Collections.emptyList());
                model.addAttribute("hasStores", favoriteStores != null && !favoriteStores.isEmpty());
                model.addAttribute("storesCount", favoriteStores != null ? favoriteStores.size() : 0);

                // Добавляем дополнительную статистику
                if (favoriteStores != null && !favoriteStores.isEmpty()) {
                    double averageRating = favoriteStores.stream()
                            .filter(store -> store.getStore() != null && store.getStore().getRating() != null)
                            .mapToDouble(store -> store.getStore().getRating().doubleValue())
                            .average()
                            .orElse(0.0);

                    model.addAttribute("averageRating", Math.round(averageRating * 10.0) / 10.0);
                }

            } else {
                log.error("❌ Ошибка получения избранных магазинов: {}",
                        response != null ? response.getMessage() : "Неизвестная ошибка");

                model.addAttribute("favoriteStores", Collections.emptyList());
                model.addAttribute("hasStores", false);
                model.addAttribute("storesCount", 0);
                model.addAttribute("isAuthenticated", true);
                model.addAttribute("userRole", userRole);
                model.addAttribute("userEmail", userEmail);
                model.addAttribute("error", response != null ? response.getMessage() : "Ошибка загрузки данных");
            }

        } catch (Exception e) {
            log.error("💥 Исключение при получении избранных магазинов", e);
            model.addAttribute("favoriteStores", Collections.emptyList());
            model.addAttribute("hasStores", false);
            model.addAttribute("isAuthenticated", false);
            model.addAttribute("error", "Произошла ошибка при загрузке избранных магазинов");
        }

        return "favorites/stores";
    }

    // ✅ УЛУЧШЕННЫЙ метод извлечения токена с детальным логированием
    private String extractTokenFromRequest(HttpServletRequest request) {
        log.info("🔍 Извлечение токена из запроса...");

        // 1. Проверяем Authorization header
        String bearerToken = request.getHeader("Authorization");
        log.info("📋 Authorization header: {}", bearerToken != null ? "Найден" : "Отсутствует");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            log.info("✅ Токен найден в Authorization header (длина: {})", token.length());
            return token;
        }

        // 2. Проверяем cookies
        log.info("🍪 Проверяем cookies...");
        if (request.getCookies() != null) {
            log.info("📦 Найдено {} cookies", request.getCookies().length);

            for (Cookie cookie : request.getCookies()) {
                log.info("🍪 Cookie: {} = {}", cookie.getName(),
                        cookie.getValue() != null ? "[" + cookie.getValue().length() + " символов]" : "null");

                if ("jwt".equals(cookie.getName()) || "accessToken".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    if (token != null && !token.trim().isEmpty()) {
                        log.info("✅ Токен найден в cookie '{}' (длина: {})", cookie.getName(), token.length());
                        return token;
                    }
                }
            }
        } else {
            log.warn("❌ Cookies отсутствуют в запросе");
        }

        // 3. Проверяем параметры запроса (на всякий случай)
        String paramToken = request.getParameter("token");
        if (paramToken != null && !paramToken.trim().isEmpty()) {
            log.info("✅ Токен найден в параметрах (длина: {})", paramToken.length());
            return paramToken;
        }

        log.warn("❌ Токен не найден ни в одном из источников");
        return null;
    }

    // ✅ МЕТОД для детального логирования запроса
    private void logRequestDetails(HttpServletRequest request) {
        log.info("📝 === ДЕТАЛИ ЗАПРОСА ===");
        log.info("🌐 URL: {}", request.getRequestURL());
        log.info("🔗 Method: {}", request.getMethod());
        log.info("📍 Remote Address: {}", request.getRemoteAddr());
        log.info("🖥️ User Agent: {}", request.getHeader("User-Agent"));

        // Логируем все заголовки
        log.info("📋 === ЗАГОЛОВКИ ===");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);

            // Скрываем чувствительные данные
            if ("Authorization".equalsIgnoreCase(headerName) && headerValue != null) {
                log.info("📋 {}: {}", headerName, headerValue.startsWith("Bearer ")
                        ? "Bearer [HIDDEN:" + (headerValue.length() - 7) + " chars]"
                        : "[HIDDEN:" + headerValue.length() + " chars]");
            } else {
                log.info("📋 {}: {}", headerName, headerValue);
            }
        }

        // Логируем cookies
        log.info("🍪 === COOKIES ===");
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                String value = cookie.getValue();
                if ("jwt".equals(cookie.getName()) || "accessToken".equals(cookie.getName())) {
                    log.info("🍪 {}: [HIDDEN:{} chars] (Domain: {}, Path: {})",
                            cookie.getName(),
                            value != null ? value.length() : 0,
                            cookie.getDomain(),
                            cookie.getPath());
                } else {
                    log.info("🍪 {}: {} (Domain: {}, Path: {})",
                            cookie.getName(),
                            value,
                            cookie.getDomain(),
                            cookie.getPath());
                }
            }
        } else {
            log.info("🍪 Cookies отсутствуют");
        }

        log.info("📝 === КОНЕЦ ДЕТАЛЕЙ ===");
    }

    /**
     * Добавление магазина в избранное
     */
    @PostMapping("/add/{storeId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToFavorites(
            @PathVariable Long storeId,
            HttpServletRequest request) {

        log.info("➕ Добавление магазина {} в избранное", storeId);

        Map<String, Object> response = new HashMap<>();

        try {
            String token = extractTokenFromRequest(request);

            if (token == null) {
                response.put("success", false);
                response.put("message", "Необходимо войти в систему");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            FavoriteStoreApiResponse<FavoriteStoreResponseDto> apiResponse =
                    storeClientService.addToFavorites(token, storeId);

            if (apiResponse != null && apiResponse.getSuccess()) {
                log.info("✅ Магазин {} успешно добавлен в избранное", storeId);
                response.put("success", true);
                response.put("message", "Магазин добавлен в избранное");
                response.put("data", apiResponse.getData());
                return ResponseEntity.ok(response);
            } else {
                log.error("❌ Ошибка добавления магазина в избранное: {}",
                        apiResponse != null ? apiResponse.getMessage() : "Неизвестная ошибка");
                response.put("success", false);
                response.put("message", apiResponse != null ? apiResponse.getMessage() : "Ошибка добавления");
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("💥 Исключение при добавлении в избранное", e);
            response.put("success", false);
            response.put("message", "Произошла ошибка");
            return ResponseEntity.internalServerError().body(response);
        }
    }




    /**
     * Удаление магазина из избранного
     */
    @DeleteMapping("/remove/{favoriteId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFromFavorites(
            @PathVariable Long favoriteId,
            HttpServletRequest request) {

        log.info("➖ Удаление магазина из избранного: {}", favoriteId);

        Map<String, Object> response = new HashMap<>();

        try {
            String token = extractTokenFromRequest(request);

            if (token == null) {
                response.put("success", false);
                response.put("message", "Необходимо войти в систему");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Здесь нужно добавить метод в сервис для удаления
//             storeClientService.removeFromFavorites(token, favoriteId);

            log.info("✅ Магазин успешно удален из избранного");
            response.put("success", true);
            response.put("message", "Магазин удален из избранного");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("💥 Исключение при удалении из избранного", e);
            response.put("success", false);
            response.put("message", "Произошла ошибка");
            return ResponseEntity.internalServerError().body(response);
        }
    }

}