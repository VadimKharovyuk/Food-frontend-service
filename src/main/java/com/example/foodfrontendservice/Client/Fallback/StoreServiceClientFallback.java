package com.example.foodfrontendservice.Client.Fallback;

import com.example.foodfrontendservice.Client.StoreServiceClient;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.category.ApiResponse;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.store.CreateStoreDto;
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


    @Override
    public ApiResponse<StoreResponseDto> createStore(CreateStoreDto createStoreDto, MultipartFile imageFile, Long userId) {
        String storeName = createStoreDto != null ? createStoreDto.getName() : "Неизвестный магазин";
        String imageInfo = imageFile != null ?
                String.format("%s (%s bytes)", imageFile.getOriginalFilename(), imageFile.getSize()) :
                "Нет изображения";

        log.error("🚨 FALLBACK ACTIVATED - Product Service Connection Failed");
        log.error("📊 Request Details:");
        log.error("   🏪 Store: {}", storeName);
        log.error("   📸 Image: {}", imageInfo);
        log.error("   👤 User ID: {}", userId);
        log.error("   ⏰ Timestamp: {}", java.time.LocalDateTime.now());


        return ApiResponse.error(
                String.format("Не удалось создать магазин '%s'. Сервис временно недоступен.", storeName)
        );
    }

    @Override
    public ResponseEntity<StoreResponseWrapper> getMyStores(int page, int size) {
        log.warn("🔥 Fallback: Store service unavailable for user stores - page={}, size={}", page, size);
        return ResponseEntity.ok(StoreResponseWrapper.error("Ваши магазины временно недоступны"));
    }
}