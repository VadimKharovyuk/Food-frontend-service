package com.example.foodfrontendservice.service;

import com.example.foodfrontendservice.Client.StoreServiceClient;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.StoreBriefResponseWrapper;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.StoreResponseWrapper;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.StoreStatsDto;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.StoreUIResponseWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreService {

    private final StoreServiceClient storeServiceClient;

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