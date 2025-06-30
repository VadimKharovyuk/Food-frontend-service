package com.example.foodfrontendservice.Client.Fallback;

import com.example.foodfrontendservice.Client.StoreServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.example.foodfrontendservice.dto.PRODUCTSERVICE.*;

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

    @Override
    public ResponseEntity<StoreResponseWrapper> getMyStores(int page, int size) {
        log.warn("🔥 Fallback: Store service unavailable for user stores - page={}, size={}", page, size);
        return ResponseEntity.ok(StoreResponseWrapper.error("Ваши магазины временно недоступны"));
    }
}