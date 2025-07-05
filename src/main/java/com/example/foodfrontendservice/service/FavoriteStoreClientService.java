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

    /**
     * Добавить магазин в избранное
     */
    public FavoriteStoreApiResponse<FavoriteStoreResponseDto> addToFavorites(String jwt, Long storeId) {
        try {
            log.info("➕ Добавление магазина {} в избранное", storeId);

            String authHeader = "Bearer " + jwt;
            ResponseEntity<FavoriteStoreApiResponse<FavoriteStoreResponseDto>> response =
                    favoriteStoreServiceClient.addToFavorites(authHeader, storeId);

            if (response.getBody() != null) {
                log.info("✅ Магазин {} успешно добавлен в избранное", storeId);
                return response.getBody();
            } else {
                log.error("❌ Пустой ответ при добавлении магазина {} в избранное", storeId);
                return FavoriteStoreApiResponse.error("Пустой ответ от сервиса");
            }

        } catch (Exception e) {
            log.error("❌ Ошибка добавления магазина {} в избранное через Feign: {}", storeId, e.getMessage(), e);
            return FavoriteStoreApiResponse.error("Ошибка связи с сервисом: " + e.getMessage());
        }
    }

    /**
     * Получить список избранных магазинов
     */
    public FavoriteStoreApiResponse<List<FavoriteStoreResponseDto>> getMyFavorites(String jwt) {
        try {
            log.info("📋 Получение списка избранных магазинов");

            String authHeader = "Bearer " + jwt;
            ResponseEntity<FavoriteStoreApiResponse<List<FavoriteStoreResponseDto>>> response =
                    favoriteStoreServiceClient.getMyFavorites(authHeader);

            if (response.getBody() != null) {
                FavoriteStoreApiResponse<List<FavoriteStoreResponseDto>> body = response.getBody();
                if (body.getSuccess() && body.getData() != null) {
                    log.info("✅ Получено {} избранных магазинов", body.getData().size());
                } else {
                    log.warn("⚠️ Получен ответ, но нет данных или ошибка: {}", body.getMessage());
                }
                return body;
            } else {
                log.error("❌ Пустой ответ при получении избранных магазинов");
                return FavoriteStoreApiResponse.error("Пустой ответ от сервиса");
            }

        } catch (Exception e) {
            log.error("❌ Ошибка получения избранного через Feign: {}", e.getMessage(), e);
            return FavoriteStoreApiResponse.error("Ошибка связи с сервисом: " + e.getMessage());
        }
    }

    /**
     * ✅ НОВЫЙ МЕТОД: Удалить магазин из избранного
     */
    public FavoriteStoreApiResponse<String> removeFromFavorites(String jwt, Long storeId) {
        try {
            log.info("➖ Удаление магазина {} из избранного", storeId);

            String authHeader = "Bearer " + jwt;
            ResponseEntity<FavoriteStoreApiResponse<String>> response =
                    favoriteStoreServiceClient.removeFromFavorites(authHeader, storeId);

            if (response.getBody() != null) {
                FavoriteStoreApiResponse<String> body = response.getBody();

                if (body.getSuccess()) {
                    log.info("✅ Магазин {} успешно удален из избранного", storeId);
                } else {
                    log.warn("⚠️ Не удалось удалить магазин {} из избранного: {}", storeId, body.getMessage());
                }

                return body;
            } else {
                log.error("❌ Пустой ответ при удалении магазина {} из избранного", storeId);
                return FavoriteStoreApiResponse.error("Пустой ответ от сервиса");
            }

        } catch (Exception e) {
            log.error("❌ Ошибка удаления магазина {} из избранного через Feign: {}", storeId, e.getMessage(), e);
            return FavoriteStoreApiResponse.error("Ошибка связи с сервисом: " + e.getMessage());
        }
    }



    /**
     * ✅ ВСПОМОГАТЕЛЬНЫЙ МЕТОД: Форматирование заголовка авторизации
     */
    private String formatAuthHeader(String jwt) {
        if (jwt == null || jwt.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT токен не может быть пустым");
        }

        // Если уже есть префикс Bearer, не добавляем
        if (jwt.startsWith("Bearer ")) {
            return jwt;
        }

        return "Bearer " + jwt.trim();
    }

    /**
     * ✅ УЛУЧШЕННАЯ ВЕРСИЯ: Добавить магазин в избранное с лучшей обработкой ошибок
     */
    public FavoriteStoreApiResponse<FavoriteStoreResponseDto> addToFavoritesSecure(String jwt, Long storeId) {
        // Валидация входных параметров
        if (storeId == null || storeId <= 0) {
            log.error("❌ Некорректный ID магазина: {}", storeId);
            return FavoriteStoreApiResponse.error("Некорректный ID магазина");
        }

        try {
            String authHeader = formatAuthHeader(jwt);
            log.info("➕ Добавление магазина {} в избранное (безопасный метод)", storeId);

            ResponseEntity<FavoriteStoreApiResponse<FavoriteStoreResponseDto>> response =
                    favoriteStoreServiceClient.addToFavorites(authHeader, storeId);

            return processResponse(response, "добавления в избранное", storeId);

        } catch (Exception e) {
            log.error("❌ Ошибка добавления магазина {} в избранное: {}", storeId, e.getMessage(), e);
            return FavoriteStoreApiResponse.error("Ошибка связи с сервисом");
        }
    }

    /**
     * ✅ ВСПОМОГАТЕЛЬНЫЙ МЕТОД: Обработка ответа от сервиса
     */
    private <T> FavoriteStoreApiResponse<T> processResponse(
            ResponseEntity<FavoriteStoreApiResponse<T>> response,
            String operation,
            Long entityId) {

        if (response == null) {
            log.error("❌ Нет ответа для операции {} с ID {}", operation, entityId);
            return FavoriteStoreApiResponse.error("Нет ответа от сервиса");
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("❌ HTTP ошибка {} для операции {} с ID {}",
                    response.getStatusCode(), operation, entityId);
            return FavoriteStoreApiResponse.error("Ошибка HTTP: " + response.getStatusCode());
        }

        FavoriteStoreApiResponse<T> body = response.getBody();
        if (body == null) {
            log.error("❌ Пустое тело ответа для операции {} с ID {}", operation, entityId);
            return FavoriteStoreApiResponse.error("Пустой ответ от сервиса");
        }

        if (!body.getSuccess()) {
            log.warn("⚠️ Операция {} с ID {} завершилась с ошибкой: {}",
                    operation, entityId, body.getMessage());
        } else {
            log.info("✅ Операция {} с ID {} выполнена успешно", operation, entityId);
        }

        return body;
    }
}