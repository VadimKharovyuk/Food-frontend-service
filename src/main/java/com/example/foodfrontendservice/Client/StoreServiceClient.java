package com.example.foodfrontendservice.Client;

import com.example.foodfrontendservice.Client.Fallback.StoreServiceClientFallback;
import com.example.foodfrontendservice.config.FeignConfig;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.StoreBriefResponseWrapper;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.StoreResponseDto;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.StoreResponseWrapper;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.StoreUIResponseWrapper;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.category.ApiResponse;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.store.CreateStoreRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * ✅ ИСПРАВЛЕНО: Используем FeignConfig вместо FeignAuthInterceptor
 */

@FeignClient(
        name = "product-service",
        path = "/api/stores",
        fallback = StoreServiceClientFallback.class,
        contextId = "storeServiceClient",
        configuration = FeignConfig.class
)
public interface StoreServiceClient {




    @PostMapping(value = "/api/stores", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<StoreResponseDto> createStore(
            @RequestPart("store") CreateStoreRequest createStoreRequest,
            @RequestPart("imageFile") MultipartFile imageFile,
            @RequestHeader("X-User-Id") Long userId
    );


    // 🏪 Получить магазины текущего пользователя
    @GetMapping("/my")
    ResponseEntity<StoreResponseWrapper> getMyStores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    );

    // 🏪 Получить магазины для UI (лимит 6)
    @GetMapping("/ui")
    ResponseEntity<StoreUIResponseWrapper> getStoresForUI();

    // 🏪 Получить краткие данные магазинов с пагинацией
    @GetMapping("/brief")
    ResponseEntity<StoreBriefResponseWrapper> getStoresBrief(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );

    // 🏪 Получить полные данные магазинов с пагинацией
    @GetMapping
    ResponseEntity<StoreResponseWrapper> getActiveStores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    );
}