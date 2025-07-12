package com.example.foodfrontendservice.controller;
import com.example.foodfrontendservice.config.JwtUtil;
import com.example.foodfrontendservice.config.TokenExtractor;
import com.example.foodfrontendservice.dto.AUTSERVICE.UserTokenInfo;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.StoreBriefDto;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.StoreBriefResponseWrapper;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.Favorite.FavoriteStoreApiResponse;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.Favorite.FavoriteStoreResponseDto;
import com.example.foodfrontendservice.service.FavoriteStoreClientService;
import com.example.foodfrontendservice.service.StoreService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/stores")
public class StoreClientController {

    private final StoreService storeService;
    private final FavoriteStoreClientService favoriteStoreClientService;
    private final TokenExtractor tokenExtractor;

    @GetMapping()
    public String showStoresPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String sortBy,
            HttpServletRequest request,
            Model model) {

        log.info("🏪 Загрузка страницы магазинов: page={}, size={}, search={}, city={}, sortBy={}",
                page, size, search, city, sortBy);

        try {

            StoreBriefResponseWrapper storesResponse = storeService.getStoresBrief(page, size);

            // Добавляем данные о магазинах в модель
            model.addAttribute("stores", storesResponse.getStores());
            model.addAttribute("totalCount", storesResponse.getTotalCount());
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("hasNext", storesResponse.getHasNext());
            model.addAttribute("hasPrevious", storesResponse.getHasPrevious());

            // Параметры поиска и фильтрации
            model.addAttribute("search", search);
            model.addAttribute("city", city);
            model.addAttribute("sortBy", sortBy);

            // Пагинация
            int totalPages = (int) Math.ceil((double) storesResponse.getTotalCount() / size);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("nextPage", page + 1);
            model.addAttribute("previousPage", page - 1);

            // ✅ НОВАЯ УЛУЧШЕННАЯ обработка авторизации с TokenExtractor
            UserTokenInfo userInfo = tokenExtractor.getCurrentUserInfo(request);
            boolean isAuthenticated = tokenExtractor.isAuthenticated(request);
            String token = tokenExtractor.extractToken(request);


            if (isAuthenticated && userInfo != null) {
                // Получаем избранные магазины
                Set<Long> favoriteStoreIds = getUserFavoriteStoreIds(userInfo.getToken());

                // Добавляем данные авторизованного пользователя
                model.addAttribute("favoriteStoreIds", favoriteStoreIds);
                model.addAttribute("isAuthenticated", true);
                model.addAttribute("jwtToken", userInfo.getToken());
                model.addAttribute("authToken", userInfo.getToken());
                model.addAttribute("authHeader", "Bearer " + userInfo.getToken());
                model.addAttribute("currentUserEmail", userInfo.getEmail());
                model.addAttribute("currentUserId", userInfo.getUserId());

                log.info("✅ Пользователь авторизован: {} (ID: {}). Избранных магазинов: {}",
                        userInfo.getEmail(), userInfo.getUserId(), favoriteStoreIds.size());
                log.debug("📋 Список избранных ID: {}", favoriteStoreIds);
            } else {
                // Неавторизованный пользователь
                model.addAttribute("favoriteStoreIds", Set.of());
                model.addAttribute("isAuthenticated", false);
                model.addAttribute("jwtToken", "");
                model.addAttribute("authToken", "");
                model.addAttribute("authHeader", "");
                model.addAttribute("currentUserEmail", "");
                model.addAttribute("currentUserId", null);

                log.info("👤 Пользователь НЕ авторизован");
            }

            log.info("✅ Загружено {} магазинов для страницы {}",
                    storesResponse.getStores().size(), page);

            return "stores/store-list";

        } catch (Exception e) {
            log.error("💥 Ошибка загрузки страницы магазинов: {}", e.getMessage(), e);
            model.addAttribute("error", "Ошибка загрузки магазинов");
            model.addAttribute("stores", List.of());
            model.addAttribute("isAuthenticated", false);
            model.addAttribute("jwtToken", "");
            model.addAttribute("authToken", "");
            model.addAttribute("authHeader", "");
            return "stores/store-list";
        }
    }

    /**
     * ✅ ОБНОВЛЕННЫЙ API ENDPOINT: Добавить/удалить магазин из избранного
     */
    @PostMapping("/api/{storeId}/toggle-favorite")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleFavorite(
            @PathVariable Long storeId,
            HttpServletRequest request) {

        log.info("🔄 Toggle favorite для магазина {}", storeId);

        Map<String, Object> response = new HashMap<>();

        try {
            // ✅ Используем TokenExtractor для получения информации о пользователе
            UserTokenInfo userInfo = tokenExtractor.getCurrentUserInfo(request);

            if (userInfo == null) {
                log.warn("❌ Пользователь не авторизован для toggle favorite");
                response.put("success", false);
                response.put("message", "Необходимо войти в систему");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Валидируем storeId
            if (storeId == null || storeId <= 0) {
                log.error("❌ Некорректный ID магазина: {}", storeId);
                response.put("success", false);
                response.put("message", "Некорректный ID магазина");
                return ResponseEntity.badRequest().body(response);
            }

            // Получаем текущий список избранного
            Set<Long> favoriteStoreIds = getUserFavoriteStoreIds(userInfo.getToken());
            boolean isFavorite = favoriteStoreIds.contains(storeId);

            log.info("👤 Пользователь {} (ID: {}) {} магазин {} (текущий статус: {})",
                    userInfo.getEmail(), userInfo.getUserId(),
                    isFavorite ? "удаляет" : "добавляет", storeId,
                    isFavorite ? "в избранном" : "не в избранном");

            FavoriteStoreApiResponse<?> apiResponse;

            if (isFavorite) {
                // Удаляем из избранного
                apiResponse = favoriteStoreClientService.removeFromFavorites(userInfo.getToken(), storeId);

                if (apiResponse != null && apiResponse.getSuccess()) {
                    log.info("✅ Магазин {} удален из избранного пользователем {}", storeId, userInfo.getEmail());
                    response.put("success", true);
                    response.put("message", "Магазин удален из избранного");
                    response.put("isFavorite", false);
                    response.put("action", "removed");
                } else {
                    log.error("❌ Ошибка удаления магазина {} из избранного: {}",
                            storeId, apiResponse != null ? apiResponse.getMessage() : "Unknown error");
                    response.put("success", false);
                    response.put("message", "Ошибка удаления из избранного");
                }
            } else {
                // Добавляем в избранное
                FavoriteStoreApiResponse<FavoriteStoreResponseDto> addResponse =
                        favoriteStoreClientService.addToFavorites(userInfo.getToken(), storeId);

                if (addResponse != null && addResponse.getSuccess()) {
                    log.info("✅ Магазин {} добавлен в избранное пользователем {}", storeId, userInfo.getEmail());
                    response.put("success", true);
                    response.put("message", "Магазин добавлен в избранное");
                    response.put("isFavorite", true);
                    response.put("action", "added");
                    response.put("data", addResponse.getData());
                } else {
                    log.error("❌ Ошибка добавления магазина {} в избранное: {}",
                            storeId, addResponse != null ? addResponse.getMessage() : "Unknown error");
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

    /**
     * ✅ ОБНОВЛЕННЫЙ API: Только добавление в избранное
     */
    @PostMapping("/api/{storeId}/add-favorite")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToFavorites(
            @PathVariable Long storeId,
            HttpServletRequest request) {

        log.info("➕ Добавление магазина {} в избранное", storeId);

        Map<String, Object> response = new HashMap<>();

        try {
            UserTokenInfo userInfo = tokenExtractor.getCurrentUserInfo(request);

            if (userInfo == null) {
                response.put("success", false);
                response.put("message", "Необходимо войти в систему");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            FavoriteStoreApiResponse<FavoriteStoreResponseDto> apiResponse =
                    favoriteStoreClientService.addToFavorites(userInfo.getToken(), storeId);

            if (apiResponse != null && apiResponse.getSuccess()) {
                log.info("✅ Магазин {} успешно добавлен в избранное пользователем {}",
                        storeId, userInfo.getEmail());
                response.put("success", true);
                response.put("message", "Магазин добавлен в избранное");
                response.put("data", apiResponse.getData());
                response.put("storeId", storeId);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", apiResponse != null ? apiResponse.getMessage() : "Ошибка добавления");
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
     * ✅ ОБНОВЛЕННЫЙ API: Только удаление из избранного
     */
    @DeleteMapping("/api/{storeId}/remove-favorite")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFromFavorites(
            @PathVariable Long storeId,
            HttpServletRequest request) {

        log.info("➖ Удаление магазина {} из избранного", storeId);

        Map<String, Object> response = new HashMap<>();

        try {
            UserTokenInfo userInfo = tokenExtractor.getCurrentUserInfo(request);

            if (userInfo == null) {
                response.put("success", false);
                response.put("message", "Необходимо войти в систему");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            FavoriteStoreApiResponse<String> apiResponse =
                    favoriteStoreClientService.removeFromFavorites(userInfo.getToken(), storeId);

            if (apiResponse != null && apiResponse.getSuccess()) {
                log.info("✅ Магазин {} успешно удален из избранного пользователем {}",
                        storeId, userInfo.getEmail());
                response.put("success", true);
                response.put("message", "Магазин удален из избранного");
                response.put("data", apiResponse.getData());
                response.put("storeId", storeId);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", apiResponse != null ? apiResponse.getMessage() : "Ошибка удаления");
                response.put("storeId", storeId);
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("💥 Исключение при удалении из избранного", e);
            response.put("success", false);
            response.put("message", "Произошла ошибка");
            response.put("storeId", storeId);
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * ✅ ВСПОМОГАТЕЛЬНЫЙ МЕТОД: Получение избранных магазинов
     * Теперь используется только в одном месте
     */
    private Set<Long> getUserFavoriteStoreIds(String jwt) {
        try {
            FavoriteStoreApiResponse<List<FavoriteStoreResponseDto>> response =
                    favoriteStoreClientService.getMyFavorites(jwt);

            if (response != null && response.getSuccess() && response.getData() != null) {
                return response.getData().stream()
                        .map(favorite -> favorite.getStore().getId())
                        .collect(Collectors.toSet());
            }
        } catch (Exception e) {
            log.warn("⚠️ Ошибка получения избранных магазинов: {}", e.getMessage());
        }

        return Set.of();
    }
}