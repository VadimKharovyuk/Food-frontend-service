package com.example.foodfrontendservice.Client.Fallback;

import com.example.foodfrontendservice.Client.StoreServiceClient;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.store.CreateStoreRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.example.foodfrontendservice.dto.PRODUCTSERVICE.*;
import org.springframework.web.multipart.MultipartFile;


@Component
@Slf4j
public class StoreServiceClientFallback implements StoreServiceClient {




    @Override
    public ResponseEntity<StoreUIResponseWrapper> getStoresForUI() {
        log.warn("🔥 Fallback: Store service unavailable for UI stores");
        return ResponseEntity.ok(StoreUIResponseWrapper.error("Сервис магазинов временно недоступен"));
    }

    @Override
    public ResponseEntity<StoreBriefResponseWrapper> getStoresBrief(int page, int size) {
        log.warn("🔥 Fallback: Store service unavailable for brief stores - page={}, size={}", page, size);
        return ResponseEntity.ok(StoreBriefResponseWrapper.error("Сервис магазинов временно недоступен"));
    }

    @Override
    public ResponseEntity<StoreResponseWrapper> getActiveStores(int page, int size) {
        log.warn("🔥 Fallback: Store service unavailable for active stores - page={}, size={}", page, size);
        return ResponseEntity.ok(StoreResponseWrapper.error("Сервис магазинов временно недоступен"));
    }


    /**
     * ✅ ИСПРАВЛЕНО: Fallback для @ModelAttribute метода
     */
    @Override
    public StoreResponseDto createStore(CreateStoreRequest createStoreRequest) {
        log.error("❌ Fallback: Product Service недоступен для создания магазина: {}",
                createStoreRequest != null ? createStoreRequest.getName() : "unknown");

        // Возвращаем объект с ID = -1 как индикатор ошибки fallback
        StoreResponseDto fallbackResponse = new StoreResponseDto();
        fallbackResponse.setId(-1L); // Индикатор fallback
        fallbackResponse.setName(createStoreRequest != null ? createStoreRequest.getName() : "Error");
        // Заполните другие обязательные поля по необходимости

        return fallbackResponse;
    }

    @Override
    public ResponseEntity<StoreResponseWrapper> getMyStores(int page, int size) {
        log.warn("🔥 Fallback: Store service unavailable for user stores - page={}, size={}", page, size);
        return ResponseEntity.ok(StoreResponseWrapper.error("Ваши магазины временно недоступны"));
    }
}