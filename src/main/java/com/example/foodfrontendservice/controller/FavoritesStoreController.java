package com.example.foodfrontendservice.controller;

import com.example.foodfrontendservice.config.JwtUtil;
import com.example.foodfrontendservice.config.TokenExtractor;
import com.example.foodfrontendservice.dto.AUTSERVICE.UserTokenInfo;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.Favorite.FavoriteStoreApiResponse;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.Favorite.FavoriteStoreResponseDto;
import com.example.foodfrontendservice.service.FavoriteStoreClientService;
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
    private final TokenExtractor tokenExtractor; // ✅ Используем TokenExtractor

    /**
     * ✅ ОБНОВЛЕННЫЙ: Показать страницу избранных магазинов
     */
    @GetMapping
    public String favoritesStores(
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {

        log.info("🛍️ Запрос избранных магазинов пользователя");

        try {
            // ✅ НОВОЕ: Используем TokenExtractor для получения информации о пользователе
            UserTokenInfo userInfo = tokenExtractor.getCurrentUserInfo(request);
            boolean isAuthenticated = tokenExtractor.isAuthenticated(request);

            // 🔍 Логируем информацию о пользователе
            log.debug("🔍 Авторизация пользователя:");
            log.debug("- Is Authenticated: {}", isAuthenticated);
            log.debug("- UserInfo: {}", userInfo != null ? userInfo.toString() : "NULL");

            if (!isAuthenticated || userInfo == null) {
                log.warn("❌ Пользователь не авторизован - перенаправление на логин");
                redirectAttributes.addFlashAttribute("error", "Необходимо войти в систему");
                return "redirect:/login";
            }

            log.info("👤 Пользователь: ID={}, Email={}, Role={}",
                    userInfo.getUserId(), userInfo.getEmail(), userInfo.getRole());

            // ✅ Добавляем данные пользователя в модель
            model.addAttribute("isAuthenticated", true);
            model.addAttribute("userRole", userInfo.getRole());
            model.addAttribute("userEmail", userInfo.getEmail());
            model.addAttribute("userId", userInfo.getUserId());
            model.addAttribute("jwtToken", userInfo.getToken()); // Для JavaScript
            model.addAttribute("authToken", userInfo.getToken());
            model.addAttribute("authHeader", "Bearer " + userInfo.getToken());

            // ✅ Получаем избранные магазины
            FavoriteStoreApiResponse<List<FavoriteStoreResponseDto>> response =
                    storeClientService.getMyFavorites(userInfo.getToken());

            if (response != null && response.getSuccess()) {
                List<FavoriteStoreResponseDto> favoriteStores = response.getData();

                log.info("✅ Получено {} избранных магазинов для пользователя {}",
                        favoriteStores != null ? favoriteStores.size() : 0, userInfo.getEmail());

                model.addAttribute("favoriteStores", favoriteStores != null ? favoriteStores : Collections.emptyList());
                model.addAttribute("hasStores", favoriteStores != null && !favoriteStores.isEmpty());
                model.addAttribute("storesCount", favoriteStores != null ? favoriteStores.size() : 0);

                // ✅ Вычисляем средний рейтинг
                if (favoriteStores != null && !favoriteStores.isEmpty()) {
                    double averageRating = favoriteStores.stream()
                            .filter(store -> store.getStore() != null && store.getStore().getRating() != null)
                            .mapToDouble(store -> store.getStore().getRating().doubleValue())
                            .average()
                            .orElse(0.0);

                    model.addAttribute("averageRating", Math.round(averageRating * 10.0) / 10.0);
                    log.debug("📊 Средний рейтинг избранных магазинов: {}", averageRating);
                }

            } else {
                String errorMessage = response != null ? response.getMessage() : "Неизвестная ошибка";
                log.error("❌ Ошибка получения избранных магазинов для {}: {}",
                        userInfo.getEmail(), errorMessage);

                model.addAttribute("favoriteStores", Collections.emptyList());
                model.addAttribute("hasStores", false);
                model.addAttribute("storesCount", 0);
                model.addAttribute("error", errorMessage);
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

    /**
     * ✅ ОБНОВЛЕННЫЙ: Добавление магазина в избранное
     */
    @PostMapping("/add/{storeId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToFavorites(
            @PathVariable Long storeId,
            HttpServletRequest request) {

        log.info("➕ Добавление магазина {} в избранное", storeId);

        Map<String, Object> response = new HashMap<>();

        try {
            // ✅ НОВОЕ: Используем TokenExtractor
            UserTokenInfo userInfo = tokenExtractor.getCurrentUserInfo(request);

            if (userInfo == null) {
                log.warn("❌ Пользователь не авторизован для добавления в избранное");
                response.put("success", false);
                response.put("message", "Необходимо войти в систему");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // ✅ Валидация storeId
            if (storeId == null || storeId <= 0) {
                log.error("❌ Некорректный ID магазина: {}", storeId);
                response.put("success", false);
                response.put("message", "Некорректный ID магазина");
                return ResponseEntity.badRequest().body(response);
            }

            log.info("👤 Пользователь {} (ID: {}) добавляет магазин {} в избранное",
                    userInfo.getEmail(), userInfo.getUserId(), storeId);

            FavoriteStoreApiResponse<FavoriteStoreResponseDto> apiResponse =
                    storeClientService.addToFavorites(userInfo.getToken(), storeId);

            if (apiResponse != null && apiResponse.getSuccess()) {
                log.info("✅ Магазин {} успешно добавлен в избранное пользователем {}",
                        storeId, userInfo.getEmail());
                response.put("success", true);
                response.put("message", "Магазин добавлен в избранное");
                response.put("data", apiResponse.getData());
                response.put("storeId", storeId);
                return ResponseEntity.ok(response);
            } else {
                String errorMessage = apiResponse != null ? apiResponse.getMessage() : "Неизвестная ошибка";
                log.error("❌ Ошибка добавления магазина {} в избранное для {}: {}",
                        storeId, userInfo.getEmail(), errorMessage);
                response.put("success", false);
                response.put("message", errorMessage);
                response.put("storeId", storeId);
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("💥 Исключение при добавлении в избранное", e);
            response.put("success", false);
            response.put("message", "Произошла ошибка");
            response.put("storeId", storeId);
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * ✅ ОБНОВЛЕННЫЙ: Удаление магазина из избранного
     */
    @DeleteMapping("/remove/{storeId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFromFavorites(
            @PathVariable Long storeId,
            HttpServletRequest request) {

        log.info("➖ Удаление магазина {} из избранного", storeId);

        Map<String, Object> response = new HashMap<>();

        try {
            // ✅ НОВОЕ: Используем TokenExtractor
            UserTokenInfo userInfo = tokenExtractor.getCurrentUserInfo(request);

            if (userInfo == null) {
                log.warn("❌ Пользователь не авторизован для удаления из избранного");
                response.put("success", false);
                response.put("message", "Необходимо войти в систему");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // ✅ Валидация storeId
            if (storeId == null || storeId <= 0) {
                log.error("❌ Некорректный ID магазина для удаления: {}", storeId);
                response.put("success", false);
                response.put("message", "Некорректный ID магазина");
                return ResponseEntity.badRequest().body(response);
            }

            log.info("👤 Пользователь {} (ID: {}) удаляет магазин {} из избранного",
                    userInfo.getEmail(), userInfo.getUserId(), storeId);

            FavoriteStoreApiResponse<String> apiResponse =
                    storeClientService.removeFromFavorites(userInfo.getToken(), storeId);

            if (apiResponse != null && apiResponse.getSuccess()) {
                log.info("✅ Магазин {} успешно удален из избранного пользователем {}",
                        storeId, userInfo.getEmail());
                response.put("success", true);
                response.put("message", "Магазин удален из избранного");
                response.put("data", apiResponse.getData());
                response.put("storeId", storeId);
                return ResponseEntity.ok(response);
            } else {
                String errorMessage = apiResponse != null ? apiResponse.getMessage() : "Неизвестная ошибка";
                log.error("❌ Ошибка удаления магазина {} из избранного для {}: {}",
                        storeId, userInfo.getEmail(), errorMessage);

                response.put("success", false);
                response.put("message", errorMessage);
                response.put("storeId", storeId);

                // Определяем HTTP статус на основе ошибки
                if (errorMessage.contains("не найден") || errorMessage.contains("not found")) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                } else {
                    return ResponseEntity.badRequest().body(response);
                }
            }

        } catch (Exception e) {
            log.error("💥 Исключение при удалении магазина {} из избранного", storeId, e);
            response.put("success", false);
            response.put("message", "Произошла ошибка при удалении");
            response.put("storeId", storeId);
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * ✅ НОВЫЙ API: Toggle favorite (для консистентности с другими контроллерами)
     */
    @PostMapping("/api/{storeId}/toggle-favorite")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleFavorite(
            @PathVariable Long storeId,
            HttpServletRequest request) {

        log.info("🔄 Toggle favorite для магазина {} (из контроллера избранного)", storeId);

        Map<String, Object> response = new HashMap<>();

        try {
            UserTokenInfo userInfo = tokenExtractor.getCurrentUserInfo(request);

            if (userInfo == null) {
                response.put("success", false);
                response.put("message", "Необходимо войти в систему");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            if (storeId == null || storeId <= 0) {
                response.put("success", false);
                response.put("message", "Некорректный ID магазина");
                return ResponseEntity.badRequest().body(response);
            }

            // Получаем текущий список избранного для определения действия
            FavoriteStoreApiResponse<List<FavoriteStoreResponseDto>> favoritesResponse =
                    storeClientService.getMyFavorites(userInfo.getToken());

            boolean isFavorite = false;
            if (favoritesResponse != null && favoritesResponse.getSuccess() && favoritesResponse.getData() != null) {
                isFavorite = favoritesResponse.getData().stream()
                        .anyMatch(favorite -> favorite.getStore().getId().equals(storeId));
            }

            log.info("👤 Пользователь {} {} магазин {} (текущий статус: {})",
                    userInfo.getEmail(), isFavorite ? "удаляет" : "добавляет",
                    storeId, isFavorite ? "в избранном" : "не в избранном");

            if (isFavorite) {
                // Удаляем из избранного
                FavoriteStoreApiResponse<String> removeResponse =
                        storeClientService.removeFromFavorites(userInfo.getToken(), storeId);

                if (removeResponse != null && removeResponse.getSuccess()) {
                    response.put("success", true);
                    response.put("message", "Магазин удален из избранного");
                    response.put("isFavorite", false);
                    response.put("action", "removed");
                } else {
                    response.put("success", false);
                    response.put("message", "Ошибка удаления из избранного");
                }
            } else {
                // Добавляем в избранное
                FavoriteStoreApiResponse<FavoriteStoreResponseDto> addResponse =
                        storeClientService.addToFavorites(userInfo.getToken(), storeId);

                if (addResponse != null && addResponse.getSuccess()) {
                    response.put("success", true);
                    response.put("message", "Магазин добавлен в избранное");
                    response.put("isFavorite", true);
                    response.put("action", "added");
                    response.put("data", addResponse.getData());
                } else {
                    response.put("success", false);
                    response.put("message", "Ошибка добавления в избранное");
                }
            }

            response.put("storeId", storeId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("💥 Исключение при toggle favorite для магазина {}", storeId, e);
            response.put("success", false);
            response.put("message", "Произошла ошибка");
            response.put("storeId", storeId);
            return ResponseEntity.internalServerError().body(response);
        }
    }


}