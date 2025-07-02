package com.example.foodfrontendservice.controller;

import com.example.foodfrontendservice.dto.PRODUCTSERVICE.StoreBriefDto;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.StoreBriefResponseWrapper;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.Favorite.FavoriteStoreApiResponse;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.Favorite.FavoriteStoreResponseDto;
import com.example.foodfrontendservice.service.FavoriteStoreClientService;
import com.example.foodfrontendservice.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Контроллер для отображения магазинов на фронтенде
 */
@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/stores")
public class StoreClientController {

    private final StoreService storeService;
    private final FavoriteStoreClientService favoriteStoreClientService;

    /**
     * Главная страница со списком магазинов
     * GET /
     * GET /stores
     */
    @GetMapping()
    public String showStoresPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String sortBy,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
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

            // Если пользователь авторизован - получаем его избранное
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                Set<Long> favoriteStoreIds = getUserFavoriteStoreIds(jwt);
                model.addAttribute("favoriteStoreIds", favoriteStoreIds);
                model.addAttribute("isAuthenticated", true);

                log.debug("✅ Пользователь авторизован. Избранных магазинов: {}", favoriteStoreIds.size());
            } else {
                model.addAttribute("favoriteStoreIds", Set.of());
                model.addAttribute("isAuthenticated", false);
                log.debug("👤 Пользователь не авторизован");
            }

            log.info("✅ Загружено {} магазинов для страницы {}",
                    storesResponse.getStores().size(), page);

            return "stores/store-list";

        } catch (Exception e) {
            log.error("💥 Ошибка загрузки страницы магазинов: {}", e.getMessage(), e);
            model.addAttribute("error", "Ошибка загрузки магазинов");
            model.addAttribute("stores", List.of());
            return "stores/store-list";
        }
    }

    /**
     * Страница конкретного магазина
     * GET /stores/{storeId}
     */
    @GetMapping("/stores/{storeId}")
    public String showStorePage(
            @PathVariable Long storeId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            Model model) {

        log.info("🏪 Загрузка страницы магазина: {}", storeId);

        try {
            // TODO: Добавить метод получения детальной информации о магазине
            // StoreDetailDto storeDetail = storeService.getStoreDetail(storeId);

            model.addAttribute("storeId", storeId);

            // Если пользователь авторизован - проверяем избранное
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                boolean isFavorite = isStoreFavorite(jwt, storeId);
                model.addAttribute("isFavorite", isFavorite);
                model.addAttribute("isAuthenticated", true);
            } else {
                model.addAttribute("isFavorite", false);
                model.addAttribute("isAuthenticated", false);
            }

            return "stores/store-detail";

        } catch (Exception e) {
            log.error("💥 Ошибка загрузки страницы магазина {}: {}", storeId, e.getMessage(), e);
            model.addAttribute("error", "Ошибка загрузки магазина");
            return "stores/store-detail";
        }
    }

    /**
     * AJAX: Получить магазины (для динамической загрузки)
     * GET /api/stores/ajax
     */
    @GetMapping("/api/stores/ajax")
    @ResponseBody
    public ResponseEntity<StoreBriefResponseWrapper> getStoresAjax(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String sortBy) {

        log.info("📡 AJAX запрос магазинов: page={}, size={}, search={}, city={}",
                page, size, search, city);

        try {
            StoreBriefResponseWrapper response = storeService.getStoresBrief(page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("💥 Ошибка AJAX загрузки магазинов: {}", e.getMessage(), e);
            return ResponseEntity.ok(StoreBriefResponseWrapper.error("Ошибка загрузки магазинов"));
        }
    }

    /**
     * AJAX: Получить избранные магазины
     * GET /api/stores/favorites
     */
    @GetMapping("/api/stores/favorites")
    @ResponseBody
    public ResponseEntity<FavoriteStoreApiResponse<List<FavoriteStoreResponseDto>>> getFavoritesAjax(
            @RequestHeader("Authorization") String authHeader) {

        log.info("📡 AJAX запрос избранных магазинов");

        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                FavoriteStoreApiResponse<List<FavoriteStoreResponseDto>> response =
                        favoriteStoreClientService.getMyFavorites(jwt);

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest()
                        .body(FavoriteStoreApiResponse.error("Требуется авторизация"));
            }
        } catch (Exception e) {
            log.error("💥 Ошибка AJAX загрузки избранного: {}", e.getMessage(), e);
            return ResponseEntity.ok(FavoriteStoreApiResponse.error("Ошибка загрузки избранного"));
        }
    }

    /**
     * AJAX: Добавить/удалить магазин в/из избранного
     * POST /api/stores/{storeId}/toggle-favorite
     */
    @PostMapping("/api/stores/{storeId}/toggle-favorite")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleFavoriteAjax(
            @PathVariable Long storeId,
            @RequestHeader("Authorization") String authHeader) {

        log.info("📡 AJAX переключение избранного для магазина: {}", storeId);

        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);

                // TODO: Реализовать toggle через FavoriteStoreClientService
                // FavoriteStoreApiResponse<FavoriteStoreResponseDto> response =
                //     favoriteStoreClientService.toggleFavorite(jwt, storeId);

                Map<String, Object> result = Map.of(
                        "success", true,
                        "message", "Статус избранного изменен",
                        "isFavorite", true // TODO: получить реальный статус
                );

                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Требуется авторизация"
                ));
            }
        } catch (Exception e) {
            log.error("💥 Ошибка AJAX переключения избранного: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Ошибка операции"
            ));
        }
    }

    // ================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ================================

    /**
     * Получить ID избранных магазинов пользователя
     */
    private Set<Long> getUserFavoriteStoreIds(String jwt) {
        try {
            FavoriteStoreApiResponse<List<FavoriteStoreResponseDto>> response =
                    favoriteStoreClientService.getMyFavorites(jwt);

            if (response.getSuccess() && response.getData() != null) {
                return response.getData().stream()
                        .map(favorite -> favorite.getStore().getId())
                        .collect(Collectors.toSet());
            }
        } catch (Exception e) {
            log.warn("⚠️ Ошибка получения избранных магазинов: {}", e.getMessage());
        }
        return Set.of();
    }

    /**
     * Проверить, является ли магазин избранным
     */
    private boolean isStoreFavorite(String jwt, Long storeId) {
        Set<Long> favoriteIds = getUserFavoriteStoreIds(jwt);
        return favoriteIds.contains(storeId);
    }

    /**
     * Страница избранных магазинов
     * GET /favorites
     */
    @GetMapping("/favorites")
    public String showFavoritesPage(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            Model model) {

        log.info("❤️ Загрузка страницы избранных магазинов");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return "redirect:/login?error=auth_required";
        }

        try {
            String jwt = authHeader.substring(7);
            FavoriteStoreApiResponse<List<FavoriteStoreResponseDto>> response =
                    favoriteStoreClientService.getMyFavorites(jwt);

            if (response.getSuccess()) {
                model.addAttribute("favorites", response.getData());
                model.addAttribute("favoritesCount", response.getData().size());
            } else {
                model.addAttribute("error", response.getMessage());
                model.addAttribute("favorites", List.of());
                model.addAttribute("favoritesCount", 0);
            }

            return "stores/favorites"; // Шаблон Thymeleaf

        } catch (Exception e) {
            log.error("💥 Ошибка загрузки избранных магазинов: {}", e.getMessage(), e);
            model.addAttribute("error", "Ошибка загрузки избранного");
            return "stores/favorites";
        }
    }

    /**
     * API endpoint для поиска магазинов
     * GET /api/stores/search
     */
    @GetMapping("/api/stores/search")
    @ResponseBody
    public ResponseEntity<StoreBriefResponseWrapper> searchStores(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        log.info("🔍 Поиск магазинов: query={}, page={}, size={}", query, page, size);

        try {
            // TODO: Реализовать поиск в StoreService
            StoreBriefResponseWrapper response = storeService.getStoresBrief(page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("💥 Ошибка поиска магазинов: {}", e.getMessage(), e);
            return ResponseEntity.ok(StoreBriefResponseWrapper.error("Ошибка поиска"));
        }
    }
}