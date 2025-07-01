package com.example.foodfrontendservice.service;

import com.example.foodfrontendservice.Client.StoreServiceClient;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.*;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.category.ApiResponse;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.store.CreateStoreDto;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.store.CreateStoreRequest;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.store.StoreCreationResponse;
import feign.FeignException;
import feign.RetryableException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreService {

    private final StoreServiceClient storeServiceClient;




    public ApiResponse<StoreResponseDto> createStore(CreateStoreRequest createStoreRequest) {
        log.debug("Creating store: {}", createStoreRequest.getName());

        try {
            // Проверяем наличие файла изображения
            MultipartFile imageFile = createStoreRequest.getImageFile();

            if (imageFile == null || imageFile.isEmpty()) {
                log.warn("⚠️ No image file provided for store: {}", createStoreRequest.getName());
                return ApiResponse.error("Изображение магазина обязательно");
            }

            // Получаем userId из контекста
            Long userId = getCurrentUserId();
            if (userId == null) {
                log.error("❌ No user ID found in request context");
                return ApiResponse.error("Не удалось определить пользователя");
            }

            // Конвертируем в CreateStoreDto для backend
            CreateStoreDto storeDto = convertToDto(createStoreRequest);

            log.info("📸 Sending store to backend: {} ({}) for user: {}",
                    createStoreRequest.getName(),
                    imageFile.getOriginalFilename(),
                    userId);

            // Отправляем на product-service
            ApiResponse<StoreResponseDto> response = storeServiceClient.createStore(storeDto, imageFile, userId);

            if (response != null && response.isSuccess()) {
                log.info("✅ Successfully created store via backend: {}", createStoreRequest.getName());
                return response;
            } else {
                log.error("❌ Backend failed to create store: {}", response != null ? response.getMessage() : "null response");
                return response != null ? response : ApiResponse.error("Не удалось создать магазин");
            }

        } catch (FeignException e) {
            log.error("🔥 Feign error creating store: {}", createStoreRequest.getName(), e);
            return ApiResponse.error("Ошибка связи с backend сервисом: " + e.getMessage());
        } catch (Exception e) {
            log.error("💥 Error creating store: {}", createStoreRequest.getName(), e);
            return ApiResponse.error("Ошибка создания магазина: " + e.getMessage());
        }
    }

    private Long getCurrentUserId() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String userIdHeader = request.getHeader("X-User-Id");
            return userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        } catch (Exception e) {
            log.error("Error getting current user ID", e);
            return null;
        }
    }

    private CreateStoreDto convertToDto(CreateStoreRequest request) {
        CreateStoreDto dto = new CreateStoreDto();
        dto.setName(request.getName());
        dto.setDescription(request.getDescription());
        dto.setStreet(request.getStreet());
        dto.setCity(request.getCity());
        dto.setRegion(request.getRegion());
        dto.setPostalCode(request.getPostalCode());
        dto.setCountry(request.getCountry());
        dto.setPhone(request.getPhone());
        dto.setEmail(request.getEmail());
        dto.setDeliveryRadius(request.getDeliveryRadius());
        dto.setDeliveryFee(request.getDeliveryFee());
        dto.setEstimatedDeliveryTime(request.getEstimatedDeliveryTime());
        dto.setIsActive(request.getIsActive());
        // НЕ копируем imageFile - он передается отдельно в Feign
        return dto;
    }

    /**
     * ✅ Валидация основных полей перед отправкой
     */
    private void validateCreateRequest(CreateStoreRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Название магазина обязательно");
        }

        if (request.getStreet() == null || request.getStreet().trim().isEmpty()) {
            throw new IllegalArgumentException("Адрес (улица) обязателен");
        }

        if (request.getCity() == null || request.getCity().trim().isEmpty()) {
            throw new IllegalArgumentException("Город обязателен");
        }

        if (request.getCountry() == null || request.getCountry().trim().isEmpty()) {
            throw new IllegalArgumentException("Страна обязательна");
        }

        if (request.getDeliveryRadius() != null && (request.getDeliveryRadius() < 1 || request.getDeliveryRadius() > 50)) {
            throw new IllegalArgumentException("Радиус доставки должен быть от 1 до 50 км");
        }

        if (request.getEstimatedDeliveryTime() != null &&
                (request.getEstimatedDeliveryTime() < 10 || request.getEstimatedDeliveryTime() > 180)) {
            throw new IllegalArgumentException("Время доставки должно быть от 10 до 180 минут");
        }

        log.debug("✅ Validation passed for store: {}", request.getName());
    }







    /**
     * Получить магазины для UI (главная страница)
     * @return магазины для отображения на главной странице
     */
    public StoreUIResponseWrapper getStoresForUI() {
        log.debug("🏪 Getting stores for UI from product service");

        try {
            ResponseEntity<StoreUIResponseWrapper> response = storeServiceClient.getStoresForUI();

            if (response.getBody() != null) {
                log.debug("✅ Successfully retrieved {} stores for UI",
                        response.getBody().getTotalCount());
                return response.getBody();
            } else {
                log.warn("⚠️ Empty response from product service for UI stores");
                return StoreUIResponseWrapper.error("Не удалось получить данные о магазинах");
            }

        } catch (Exception e) {
            log.error("❌ Error calling product service for UI stores", e);
            return StoreUIResponseWrapper.error("Ошибка связи с сервисом продуктов");
        }
    }

    /**
     * Получить краткие данные магазинов с пагинацией
     * @param page номер страницы
     * @param size размер страницы
     * @return краткие данные магазинов
     */
    public StoreBriefResponseWrapper getStoresBrief(int page, int size) {
        log.debug("🏪 Getting brief stores from product service: page={}, size={}", page, size);

        try {
            ResponseEntity<StoreBriefResponseWrapper> response =
                    storeServiceClient.getStoresBrief(page, size);

            if (response.getBody() != null) {
                log.debug("✅ Successfully retrieved {} brief stores for page {}",
                        response.getBody().getTotalCount(), page);
                return response.getBody();
            } else {
                log.warn("⚠️ Empty response from product service for brief stores");
                return StoreBriefResponseWrapper.error("Не удалось получить данные о магазинах");
            }

        } catch (Exception e) {
            log.error("❌ Error calling product service for brief stores: page={}, size={}",
                    page, size, e);
            return StoreBriefResponseWrapper.error("Ошибка связи с сервисом продуктов");
        }
    }

    /**
     * Получить полные данные активных магазинов
     * @param page номер страницы
     * @param size размер страницы
     * @return полные данные магазинов
     */
    public StoreResponseWrapper getActiveStores(int page, int size) {
        log.debug("🏪 Getting active stores from product service: page={}, size={}", page, size);

        try {
            ResponseEntity<StoreResponseWrapper> response =
                    storeServiceClient.getActiveStores(page, size);

            if (response.getBody() != null) {
                log.debug("✅ Successfully retrieved {} active stores for page {}",
                        response.getBody().getTotalCount(), page);
                return response.getBody();
            } else {
                log.warn("⚠️ Empty response from product service for active stores");
                return StoreResponseWrapper.error("Не удалось получить данные о магазинах");
            }

        } catch (Exception e) {
            log.error("❌ Error calling product service for active stores: page={}, size={}",
                    page, size, e);
            return StoreResponseWrapper.error("Ошибка связи с сервисом продуктов");
        }
    }

    /**
     * Получить магазины текущего пользователя (владельца)
     * userId передается автоматически через заголовок X-User-Id благодаря FeignAuthInterceptor
     * @param userId ID пользователя (используется только для логирования)
     * @param page номер страницы
     * @param size размер страницы
     * @return магазины владельца
     */
    public StoreResponseWrapper getMyStores(Long userId, int page, int size) {
        log.debug("🏪 Getting my stores for user {} from product service: page={}, size={}",
                userId, page, size);

        try {
            // userId передается автоматически через заголовок X-User-Id
            ResponseEntity<StoreResponseWrapper> response =
                    storeServiceClient.getMyStores(page, size);

            if (response.getBody() != null) {
                log.debug("✅ Successfully retrieved {} my stores for user {} on page {}",
                        response.getBody().getTotalCount(), userId, page);
                return response.getBody();
            } else {
                log.warn("⚠️ Empty response from product service for user {} stores", userId);
                return StoreResponseWrapper.error("Не удалось получить ваши магазины");
            }

        } catch (Exception e) {
            log.error("❌ Error calling product service for user {} stores: page={}, size={}",
                    userId, page, size, e);
            return StoreResponseWrapper.error("Ошибка связи с сервисом продуктов");
        }
    }

    /**
     * Получить статистику магазинов для дашборда
     * @return базовая статистика
     */
    public StoreStatsDto getStoreStats() {
        log.debug("📊 Getting store statistics");

        try {
            // Получаем краткие данные первой страницы для подсчета
            StoreBriefResponseWrapper briefStores = getStoresBrief(0, 1);

            if (briefStores.getSuccess()) {
                return StoreStatsDto.builder()
                        .totalActiveStores(briefStores.getTotalCount())
                        .hasStores(briefStores.getTotalCount() > 0)
                        .build();
            } else {
                return StoreStatsDto.builder()
                        .totalActiveStores(0)
                        .hasStores(false)
                        .build();
            }

        } catch (Exception e) {
            log.error("❌ Error getting store statistics", e);
            return StoreStatsDto.builder()
                    .totalActiveStores(0)
                    .hasStores(false)
                    .build();
        }
    }
}