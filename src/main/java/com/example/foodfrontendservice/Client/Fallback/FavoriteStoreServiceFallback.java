package com.example.foodfrontendservice.Client.Fallback;

import com.example.foodfrontendservice.Client.FavoriteStoreServiceClient;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.Favorite.FavoriteStoreApiResponse;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.Favorite.FavoriteStoreResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class FavoriteStoreServiceFallback implements FavoriteStoreServiceClient {

    @Override
    public ResponseEntity<FavoriteStoreApiResponse<FavoriteStoreResponseDto>> addToFavorites(
            String authToken, Long storeId) {
        log.warn("Fallback: addToFavorites для storeId: {}", storeId);
        return ResponseEntity.ok(
                FavoriteStoreApiResponse.error("Сервис избранного временно недоступен")
        );
    }

    @Override
    public ResponseEntity<FavoriteStoreApiResponse<String>> removeFromFavorites(
            String authToken, Long storeId) {
        log.warn("Fallback: removeFromFavorites для storeId: {}", storeId);
        return ResponseEntity.ok(
                FavoriteStoreApiResponse.error("Сервис избранного временно недоступен")
        );
    }

    @Override
    public ResponseEntity<FavoriteStoreApiResponse<FavoriteStoreResponseDto>> toggleFavorite(
            String authToken, Long storeId) {
        log.warn("Fallback: toggleFavorite для storeId: {}", storeId);
        return ResponseEntity.ok(
                FavoriteStoreApiResponse.error("Сервис избранного временно недоступен")
        );
    }

    @Override
    public ResponseEntity<FavoriteStoreApiResponse<List<FavoriteStoreResponseDto>>> getMyFavorites(
            String authToken) {
        log.warn("Fallback: getMyFavorites");
        return ResponseEntity.ok(
                FavoriteStoreApiResponse.error("Сервис избранного временно недоступен")
        );
    }

    @Override
    public ResponseEntity<FavoriteStoreApiResponse<List<FavoriteStoreResponseDto>>> getMyActiveFavorites(
            String authToken) {
        log.warn("Fallback: getMyActiveFavorites");
        return ResponseEntity.ok(
                FavoriteStoreApiResponse.error("Сервис избранного временно недоступен")
        );
    }

    @Override
    public ResponseEntity<FavoriteStoreApiResponse<Boolean>> checkFavoriteStatus(
            String authToken, Long storeId) {
        log.warn("Fallback: checkFavoriteStatus для storeId: {}", storeId);
        return ResponseEntity.ok(
                FavoriteStoreApiResponse.success(false, "Статус недоступен")
        );
    }

    @Override
    public ResponseEntity<FavoriteStoreApiResponse<Long>> getFavoritesCount(String authToken) {
        log.warn("Fallback: getFavoritesCount");
        return ResponseEntity.ok(
                FavoriteStoreApiResponse.success(0L, "Количество недоступно")
        );
    }
}