package com.example.foodfrontendservice.service;

import com.example.foodfrontendservice.Client.StoreServiceClient;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.*;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.store.CreateStoreRequest;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.store.StoreCreationResponse;
import feign.FeignException;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreService {

    private final StoreServiceClient storeServiceClient;


    /**
     * ✅ ОБНОВЛЕНО: Основной метод с @ModelAttribute подходом
     */
    public StoreResponseDto createStore(CreateStoreRequest createRequest) {
        log.info("🚀 UI Service: Creating store: {} for user with image: {}",
                createRequest.getName(), createRequest.getImageUrl());

        try {
            // ✅ Валидация входных данных
            validateCreateRequest(createRequest);

            // ✅ ИСПРАВЛЕНО: Вызываем Feign клиент с @ModelAttribute
            StoreResponseDto response = storeServiceClient.createStore(createRequest);

            log.info("✅ Store created successfully with ID: {}", response.getId());
            return response;

        } catch (FeignException.InternalServerError e) {
            log.error("❌ Product Service Error (500): {}", e.contentUTF8());
            throw new RuntimeException("Внутренняя ошибка Product Service: " + e.contentUTF8());

        } catch (FeignException.BadRequest e) {
            log.warn("❌ Validation error (400): {}", e.contentUTF8());
            throw new IllegalArgumentException("Ошибка валидации: " + e.contentUTF8());

        } catch (FeignException.Unauthorized e) {
            log.warn("❌ Unauthorized (401): {}", e.getMessage());
            throw new SecurityException("Недостаточно прав для создания магазина");

        } catch (FeignException.Forbidden e) {
            log.warn("❌ Forbidden (403): {}", e.getMessage());
            throw new SecurityException("Доступ запрещен. Требуется роль BUSINESS");

        } catch (RetryableException e) {
            log.warn("❌ Service unavailable: {}", e.getMessage());
            throw new RuntimeException("Product Service временно недоступен");

        } catch (FeignException e) {
            log.error("❌ Feign error: HTTP {}, body: {}", e.status(), e.contentUTF8());
            throw new RuntimeException(String.format("Ошибка связи с Product Service (HTTP %d)", e.status()));

        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage(), e);
            throw new RuntimeException("Внутренняя ошибка UI Service: " + e.getMessage());
        }
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