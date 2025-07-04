package com.example.foodfrontendservice.service;


import com.example.foodfrontendservice.Client.FavoriteStoreServiceClient;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.Favorite.FavoriteStoreApiResponse;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.Favorite.FavoriteStoreResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteStoreClientService {

    private final FavoriteStoreServiceClient favoriteStoreServiceClient;


    public FavoriteStoreApiResponse<FavoriteStoreResponseDto> addToFavorites(String jwt, Long storeId) {
        try {
            String authHeader = "Bearer " + jwt;
            ResponseEntity<FavoriteStoreApiResponse<FavoriteStoreResponseDto>> response =
                    favoriteStoreServiceClient.addToFavorites(authHeader, storeId);

            return response.getBody();

        } catch (Exception e) {
            log.error("Ошибка добавления в избранное через Feign: {}", e.getMessage());
            return FavoriteStoreApiResponse.error("Ошибка связи с сервисом");
        }
    }


    public FavoriteStoreApiResponse<List<FavoriteStoreResponseDto>> getMyFavorites(String jwt) {
        try {
            String authHeader = "Bearer " + jwt;
            ResponseEntity<FavoriteStoreApiResponse<List<FavoriteStoreResponseDto>>> response =
                    favoriteStoreServiceClient.getMyFavorites(authHeader);

            return response.getBody();

        } catch (Exception e) {
            log.error("Ошибка получения избранного через Feign: {}", e.getMessage());
            return FavoriteStoreApiResponse.error("Ошибка связи с сервисом");
        }
    }
//    public FavoriteStoreApiResponse<Void> removeFromFavorites(String jwt, Long favoriteId) {
//        try {
//            String authHeader = "Bearer " + jwt;
//            ResponseEntity<FavoriteStoreApiResponse<Void>> response =
//                    favoriteStoreServiceClient.removeFromFavorites(authHeader, favoriteId);
//
//            return response.getBody();
//        } catch (Exception e) {
//            log.error("Ошибка удаления из избранного через Feign: {}", e.getMessage());
//            return FavoriteStoreApiResponse.error("Ошибка связи с сервисом");
//        }
//    }
}
