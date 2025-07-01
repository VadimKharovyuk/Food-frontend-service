package com.example.foodfrontendservice.controller;

import com.example.foodfrontendservice.dto.PRODUCTSERVICE.category.ApiResponse;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.store.CreateStoreRequest;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.store.StoreCreationResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.*;
import com.example.foodfrontendservice.service.StoreService;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Collectors;

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
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "Authorization", required = false) String authToken,
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page must be non-negative") int page,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "Size must be positive")
            @Max(value = 100, message = "Size must not exceed 100") int size) {

        log.info("👤 GET /api/frontend/stores/my - userIdHeader={}, page={}, size={}",
                userIdHeader, page, size);

        // Проверяем авторизацию
        if (authToken == null || authToken.trim().isEmpty()) {
            log.warn("❌ No Authorization header provided");
            return ResponseEntity.status(401)
                    .body(StoreResponseWrapper.error("Требуется авторизация"));
        }

        if (userIdHeader == null || userIdHeader.trim().isEmpty()) {
            log.warn("❌ No X-User-Id header provided");
            return ResponseEntity.status(401)
                    .body(StoreResponseWrapper.error("Не удалось определить пользователя"));
        }

        Long userId;
        try {
            userId = Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            log.warn("❌ Invalid X-User-Id header format: {}", userIdHeader);
            return ResponseEntity.status(400)
                    .body(StoreResponseWrapper.error("Некорректный формат ID пользователя"));
        }

        try {
            // Получаем магазины пользователя
            StoreResponseWrapper response = storeService.getMyStores(userId, page, size);

            if (response.getSuccess()) {
                log.info("✅ Retrieved {} my stores for user {}, hasNext: {}",
                        response.getTotalCount(), userId, response.getHasNext());
                return ResponseEntity.ok(response);
            } else {
                log.warn("❌ Failed to get my stores for user {}: {}",
                        userId, response.getMessage());
                return ResponseEntity.status(500).body(response);
            }

        } catch (Exception e) {
            log.error("💥 Unexpected error while getting stores for user {}", userId, e);
            return ResponseEntity.status(500)
                    .body(StoreResponseWrapper.error("Внутренняя ошибка сервера"));
        }
    }

    /**
     * 🏪 Получить магазины для главной страницы
     */
    @GetMapping("/ui")
    public ResponseEntity<StoreUIResponseWrapper> getStoresForUI() {
        log.info("🏠 GET /api/frontend/stores/ui - Getting stores for main page");

        try {
            StoreUIResponseWrapper response = storeService.getStoresForUI();

            if (response.getSuccess()) {
                log.info("✅ Successfully retrieved {} stores for UI", response.getTotalCount());
                return ResponseEntity.ok(response);
            } else {
                log.warn("❌ Failed to get stores for UI: {}", response.getMessage());
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            log.error("💥 Unexpected error while getting stores for UI", e);
            return ResponseEntity.status(500)
                    .body(StoreUIResponseWrapper.error("Внутренняя ошибка сервера"));
        }
    }

    /**
     * 🏪 Получить список магазинов с пагинацией (краткие данные)
     */
    @GetMapping("/brief")
    public ResponseEntity<StoreBriefResponseWrapper> getStoresBrief(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page must be non-negative") int page,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Size must be positive")
            @Max(value = 50, message = "Size must not exceed 50") int size) {

        log.info("📋 GET /api/frontend/stores/brief - page={}, size={}", page, size);

        try {
            StoreBriefResponseWrapper response = storeService.getStoresBrief(page, size);

            if (response.getSuccess()) {
                log.info("✅ Retrieved {} brief stores, hasNext: {}",
                        response.getTotalCount(), response.getHasNext());
                return ResponseEntity.ok(response);
            } else {
                log.warn("❌ Failed to get brief stores: {}", response.getMessage());
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            log.error("💥 Unexpected error while getting brief stores", e);
            return ResponseEntity.status(500)
                    .body(StoreBriefResponseWrapper.error("Внутренняя ошибка сервера"));
        }
    }

    /**
     * 🏪 Получить полный список магазинов
     */
    @GetMapping("/full")
    public ResponseEntity<StoreResponseWrapper> getActiveStores(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page must be non-negative") int page,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "Size must be positive")
            @Max(value = 100, message = "Size must not exceed 100") int size) {

        log.info("📋 GET /api/frontend/stores/full - page={}, size={}", page, size);

        try {
            StoreResponseWrapper response = storeService.getActiveStores(page, size);

            if (response.getSuccess()) {
                log.info("✅ Retrieved {} full stores, hasNext: {}",
                        response.getTotalCount(), response.getHasNext());
                return ResponseEntity.ok(response);
            } else {
                log.warn("❌ Failed to get full stores: {}", response.getMessage());
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            log.error("💥 Unexpected error while getting full stores", e);
            return ResponseEntity.status(500)
                    .body(StoreResponseWrapper.error("Внутренняя ошибка сервера"));
        }
    }

    /**
     * 📊 Получить статистику магазинов
     */
    @GetMapping("/stats")
    public ResponseEntity<StoreStatsDto> getStoreStats() {
        log.info("📊 GET /api/frontend/stores/stats - Getting store statistics");

        try {
            StoreStatsDto stats = storeService.getStoreStats();
            log.info("✅ Store stats: {} active stores", stats.getTotalActiveStores());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("💥 Unexpected error while getting store statistics", e);
            return ResponseEntity.status(500)
                    .body(StoreStatsDto.builder()
                            .totalActiveStores(0)
                            .hasStores(false)
                            .build());
        }
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StoreResponseDto> createStore(
            @ModelAttribute CreateStoreRequest createStoreRequest,
            HttpServletRequest request) {

        log.info("📥 POST /api/v1/stores - Creating store: {} from IP: {}",
                createStoreRequest.getName(),
                getClientIpAddress(request));


        try {
            // ✅ Вызываем сервис для создания магазина
            StoreResponseDto createdStore = storeService.createStore(createStoreRequest);

            log.info("✅ Store created successfully with ID: {} and name: {}",
                    createdStore.getId(), createdStore.getName());

            return ResponseEntity.status(HttpStatus.CREATED).body(createdStore);

        } catch (IllegalArgumentException e) {
            log.warn("❌ Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .header("Error-Message", e.getMessage())
                    .build();

        } catch (SecurityException e) {
            log.warn("❌ Security error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .header("Error-Message", e.getMessage())
                    .build();

        } catch (RuntimeException e) {
            log.error("❌ Runtime error while creating store: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Error-Message", "Внутренняя ошибка сервера")
                    .build();

        } catch (Exception e) {
            log.error("❌ Unexpected error while creating store: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Error-Message", "Произошла непредвиденная ошибка")
                    .build();
        }
    }
    /**
     * ✅ Получение IP адреса клиента для логирования
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

}