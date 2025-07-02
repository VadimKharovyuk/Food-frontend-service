package com.example.foodfrontendservice.Client;
import com.example.foodfrontendservice.Client.Fallback.FavoriteStoreServiceFallback;
import com.example.foodfrontendservice.config.feignConfig;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.Favorite.FavoriteStoreApiResponse;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.Favorite.FavoriteStoreResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "product-service",
        path = "/api/favorites",
        fallback = FavoriteStoreServiceFallback.class,
        contextId = "favoriteStoreServiceClient",
        configuration = feignConfig.class
)
public interface FavoriteStoreServiceClient {


    @PostMapping("/stores/{storeId}")
    ResponseEntity<FavoriteStoreApiResponse<FavoriteStoreResponseDto>> addToFavorites(
            @RequestHeader("Authorization") String authToken,  // ← JWT токен
            @PathVariable Long storeId
    );

    /**
     * Удалить ресторан из избранного
     */
    @DeleteMapping("/stores/{storeId}")
    ResponseEntity<FavoriteStoreApiResponse<String>> removeFromFavorites(
            @RequestHeader("Authorization") String authToken,
            @PathVariable Long storeId
    );

    /**
     * Переключить статус избранного
     */
    @PutMapping("/stores/{storeId}/toggle")
    ResponseEntity<FavoriteStoreApiResponse<FavoriteStoreResponseDto>> toggleFavorite(
            @RequestHeader("Authorization") String authToken,
            @PathVariable Long storeId
    );

    /**
     * Получить избранные рестораны
     */
    @GetMapping
    ResponseEntity<FavoriteStoreApiResponse<List<FavoriteStoreResponseDto>>> getMyFavorites(
            @RequestHeader("Authorization") String authToken
    );

    /**
     * Получить активные избранные рестораны
     */
    @GetMapping("/active")
    ResponseEntity<FavoriteStoreApiResponse<List<FavoriteStoreResponseDto>>> getMyActiveFavorites(
            @RequestHeader("Authorization") String authToken
    );

    /**
     * Проверить статус избранного
     */
    @GetMapping("/stores/{storeId}/status")
    ResponseEntity<FavoriteStoreApiResponse<Boolean>> checkFavoriteStatus(
            @RequestHeader("Authorization") String authToken,
            @PathVariable Long storeId
    );

    /**
     * Получить количество избранных
     */
    @GetMapping("/count")
    ResponseEntity<FavoriteStoreApiResponse<Long>> getFavoritesCount(
            @RequestHeader("Authorization") String authToken
    );
}