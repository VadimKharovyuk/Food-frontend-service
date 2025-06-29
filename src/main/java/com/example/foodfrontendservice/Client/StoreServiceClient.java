package com.example.foodfrontendservice.Client;

import com.example.foodfrontendservice.Client.StoreServiceClientFallback;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.StoreBriefResponseWrapper;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.StoreResponseWrapper;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.StoreUIResponseWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "product-service",
        path = "/api/stores",
        fallback = StoreServiceClientFallback.class
)
public interface StoreServiceClient {

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

    // 🏪 Получить магазины владельца (требует авторизации)
    @GetMapping("/my")
    ResponseEntity<StoreResponseWrapper> getMyStores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    );
}