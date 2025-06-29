package com.example.foodfrontendservice.controller;
import com.example.foodfrontendservice.config.CurrentUser;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.*;
import com.example.foodfrontendservice.service.StoreService;

@RestController
@RequestMapping("/api/frontend/stores")
@RequiredArgsConstructor
@Slf4j
public class StoreController {

    private final StoreService storeService;
    /**
     * 🏪 Получить магазины текущего пользователя
     */
    @GetMapping("/my")
    public ResponseEntity<StoreResponseWrapper> getMyStores(
            @CurrentUser Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("👤 GET /api/frontend/stores/my - userId={}, page={}, size={}",
                userId, page, size);
        StoreResponseWrapper response = storeService.getMyStores(userId, page, size);

        if (userId == null) {
            log.warn("❌ User not authenticated for /my stores");
            return ResponseEntity.status(401)
                    .body(StoreResponseWrapper.error("Требуется авторизация"));
        }

        if (response.getSuccess()) {
            log.info("✅ Retrieved {} my stores for user {}, hasNext: {}",
                    response.getTotalCount(), userId, response.getHasNext());
            return ResponseEntity.ok(response);
        } else {
            log.warn("❌ Failed to get my stores for user {}: {}",
                    userId, response.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 🏪 Получить магазины для главной страницы
     */
    @GetMapping("/ui")
    public ResponseEntity<StoreUIResponseWrapper> getStoresForUI() {
        log.info("🏠 GET /api/frontend/stores/ui - Getting stores for main page");

        StoreUIResponseWrapper response = storeService.getStoresForUI();

        if (response.getSuccess()) {
            log.info("✅ Successfully retrieved {} stores for UI", response.getTotalCount());
            return ResponseEntity.ok(response);
        } else {
            log.warn("❌ Failed to get stores for UI: {}", response.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 🏪 Получить список магазинов с пагинацией (краткие данные)
     */
    @GetMapping("/brief")
    public ResponseEntity<StoreBriefResponseWrapper> getStoresBrief(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page must be non-negative")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Size must be positive")
            @Max(value = 50, message = "Size must not exceed 50")
            int size) {

        log.info("📋 GET /api/frontend/stores/brief - page={}, size={}", page, size);

        StoreBriefResponseWrapper response = storeService.getStoresBrief(page, size);

        if (response.getSuccess()) {
            log.info("✅ Retrieved {} brief stores, hasNext: {}",
                    response.getTotalCount(), response.getHasNext());
            return ResponseEntity.ok(response);
        } else {
            log.warn("❌ Failed to get brief stores: {}", response.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 🏪 Получить полный список магазинов
     */
    @GetMapping("/full")
    public ResponseEntity<StoreResponseWrapper> getActiveStores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("📋 GET /api/frontend/stores/full - page={}, size={}", page, size);

        StoreResponseWrapper response = storeService.getActiveStores(page, size);

        if (response.getSuccess()) {
            log.info("✅ Retrieved {} full stores, hasNext: {}",
                    response.getTotalCount(), response.getHasNext());
            return ResponseEntity.ok(response);
        } else {
            log.warn("❌ Failed to get full stores: {}", response.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }



    /**
     * 📊 Получить статистику магазинов
     */
    @GetMapping("/stats")
    public ResponseEntity<StoreStatsDto> getStoreStats() {
        log.info("📊 GET /api/frontend/stores/stats - Getting store statistics");

        StoreStatsDto stats = storeService.getStoreStats();

        log.info("✅ Store stats: {} active stores", stats.getTotalActiveStores());
        return ResponseEntity.ok(stats);
    }
}

