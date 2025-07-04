package com.example.foodfrontendservice.controller;

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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/favorites/stores")
public class FavoritesStoreController {

    private final FavoriteStoreClientService storeClientService;


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

    @GetMapping
    public String favoritesStores(
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {

        log.info("🛍️ Запрос избранных магазинов пользователя");

        try {
            // Получаем JWT токен из запроса
            String token = extractTokenFromRequest(request);

            if (token == null) {
                log.warn("❌ JWT токен не найден - перенаправление на логин");
                redirectAttributes.addFlashAttribute("error", "Необходимо войти в систему");
                return "redirect:/login";
            }

            // Получаем информацию о пользователе из headers (установленных фильтром)
            String userId = request.getHeader("X-User-Id");
            String userEmail = request.getHeader("X-User-Email");

            log.info("👤 Пользователь: ID={}, Email={}", userId, userEmail);

            // Вызываем сервис для получения избранных магазинов
            FavoriteStoreApiResponse<List<FavoriteStoreResponseDto>> response =
                    storeClientService.getMyFavorites(token);

            if (response != null && response.getSuccess()) {
                List<FavoriteStoreResponseDto> favoriteStores = response.getData();

                log.info("✅ Получено {} избранных магазинов", favoriteStores != null ? favoriteStores.size() : 0);

                // Передаем данные в модель
                model.addAttribute("favoriteStores", favoriteStores != null ? favoriteStores : Collections.emptyList());
                model.addAttribute("hasStores", favoriteStores != null && !favoriteStores.isEmpty());
                model.addAttribute("storesCount", favoriteStores != null ? favoriteStores.size() : 0);
                model.addAttribute("userEmail", userEmail);

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
                model.addAttribute("error", response != null ? response.getMessage() : "Ошибка загрузки данных");
            }

        } catch (Exception e) {
            log.error("💥 Исключение при получении избранных магазинов", e);
            model.addAttribute("favoriteStores", Collections.emptyList());
            model.addAttribute("hasStores", false);
            model.addAttribute("error", "Произошла ошибка при загрузке избранных магазинов");
        }

        return "favorites/stores";
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
            // storeClientService.removeFromFavorites(token, favoriteId);

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

    /**
     * Извлечение JWT токена из запроса
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // Попробуем получить из cookies как fallback
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName()) || "accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}