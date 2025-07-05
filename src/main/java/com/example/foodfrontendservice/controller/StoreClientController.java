package com.example.foodfrontendservice.controller;

import com.example.foodfrontendservice.config.JwtUtil;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.StoreBriefDto;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.StoreBriefResponseWrapper;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.Favorite.FavoriteStoreApiResponse;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.Favorite.FavoriteStoreResponseDto;
import com.example.foodfrontendservice.service.FavoriteStoreClientService;
import com.example.foodfrontendservice.service.StoreService;
import jakarta.servlet.http.Cookie;
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
    private final JwtUtil jwtUtil;

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
            // Получаем список магазинов
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

            // ✅ УЛУЧШЕННАЯ обработка авторизации
            String token = extractTokenFromRequest(request);

            if (token != null && jwtUtil.isTokenValid(token)) {
                Set<Long> favoriteStoreIds = getUserFavoriteStoreIds(token);

                model.addAttribute("favoriteStoreIds", favoriteStoreIds);
                model.addAttribute("isAuthenticated", true);
                model.addAttribute("jwtToken", token); // ✅ Добавляем токен для JavaScript
                model.addAttribute("authToken", token);
                model.addAttribute("authHeader", "Bearer " + token);

                log.debug("✅ Пользователь авторизован. Избранных магазинов: {}", favoriteStoreIds.size());
            } else {
                model.addAttribute("favoriteStoreIds", Set.of());
                model.addAttribute("isAuthenticated", false);
                model.addAttribute("jwtToken", "");
                model.addAttribute("authToken", "");
                model.addAttribute("authHeader", "");
                log.debug("👤 Пользователь не авторизован");
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
     * ✅ НОВЫЙ API ENDPOINT: Добавить/удалить магазин из избранного
     */
    @PostMapping("/api/{storeId}/toggle-favorite")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleFavorite(
            @PathVariable Long storeId,
            HttpServletRequest request) {

        log.info("🔄 Toggle favorite для магазина {}", storeId);

        Map<String, Object> response = new HashMap<>();

        try {
            // Получаем и валидируем токен
            String token = extractTokenFromRequest(request);

            if (token == null) {
                log.warn("❌ JWT токен не найден для toggle favorite");
                response.put("success", false);
                response.put("message", "Необходимо войти в систему");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            if (!jwtUtil.isTokenValid(token)) {
                log.warn("❌ JWT токен недействителен для toggle favorite");
                response.put("success", false);
                response.put("message", "Сессия истекла, войдите заново");
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
            Set<Long> favoriteStoreIds = getUserFavoriteStoreIds(token);
            boolean isFavorite = favoriteStoreIds.contains(storeId);

            log.info("👤 Пользователь {} магазин {} (текущий статус: {})",
                    jwtUtil.getEmailFromToken(token), storeId, isFavorite ? "в избранном" : "не в избранном");

            FavoriteStoreApiResponse<?> apiResponse;

            if (isFavorite) {
                // Удаляем из избранного
                apiResponse = favoriteStoreClientService.removeFromFavorites(token, storeId);

                if (apiResponse != null && apiResponse.getSuccess()) {
                    log.info("✅ Магазин {} удален из избранного", storeId);
                    response.put("success", true);
                    response.put("message", "Магазин удален из избранного");
                    response.put("isFavorite", false);
                    response.put("action", "removed");
                } else {
                    log.error("❌ Ошибка удаления магазина {} из избранного", storeId);
                    response.put("success", false);
                    response.put("message", "Ошибка удаления из избранного");
                }
            } else {
                // Добавляем в избранное
                FavoriteStoreApiResponse<FavoriteStoreResponseDto> addResponse =
                        favoriteStoreClientService.addToFavorites(token, storeId);

                if (addResponse != null && addResponse.getSuccess()) {
                    log.info("✅ Магазин {} добавлен в избранное", storeId);
                    response.put("success", true);
                    response.put("message", "Магазин добавлен в избранное");
                    response.put("isFavorite", true);
                    response.put("action", "added");
                    response.put("data", addResponse.getData());
                } else {
                    log.error("❌ Ошибка добавления магазина {} в избранное", storeId);
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
     * ✅ ДОПОЛНИТЕЛЬНЫЙ API: Только добавление в избранное
     */
    @PostMapping("/api/{storeId}/add-favorite")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToFavorites(
            @PathVariable Long storeId,
            HttpServletRequest request) {

        log.info("➕ Добавление магазина {} в избранное", storeId);

        Map<String, Object> response = new HashMap<>();

        try {
            String token = extractTokenFromRequest(request);

            if (token == null || !jwtUtil.isTokenValid(token)) {
                response.put("success", false);
                response.put("message", "Необходимо войти в систему");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            FavoriteStoreApiResponse<FavoriteStoreResponseDto> apiResponse =
                    favoriteStoreClientService.addToFavorites(token, storeId);

            if (apiResponse != null && apiResponse.getSuccess()) {
                log.info("✅ Магазин {} успешно добавлен в избранное", storeId);
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
     * ✅ ДОПОЛНИТЕЛЬНЫЙ API: Только удаление из избранного
     */
    @DeleteMapping("/api/{storeId}/remove-favorite")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFromFavorites(
            @PathVariable Long storeId,
            HttpServletRequest request) {

        log.info("➖ Удаление магазина {} из избранного", storeId);

        Map<String, Object> response = new HashMap<>();

        try {
            String token = extractTokenFromRequest(request);

            if (token == null || !jwtUtil.isTokenValid(token)) {
                response.put("success", false);
                response.put("message", "Необходимо войти в систему");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            FavoriteStoreApiResponse<String> apiResponse =
                    favoriteStoreClientService.removeFromFavorites(token, storeId);

            if (apiResponse != null && apiResponse.getSuccess()) {
                log.info("✅ Магазин {} успешно удален из избранного", storeId);
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
     * ✅ ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        // 1. Проверяем Authorization header
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 2. Проверяем cookies
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName()) || "accessToken".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    if (token != null && !token.trim().isEmpty()) {
                        return token;
                    }
                }
            }
        }

        return null;
    }

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