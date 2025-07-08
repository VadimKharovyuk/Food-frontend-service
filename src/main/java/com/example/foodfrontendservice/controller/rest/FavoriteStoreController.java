package com.example.foodfrontendservice.controller.rest;

import com.example.foodfrontendservice.dto.PRODUCTSERVICE.Favorite.FavoriteStoreApiResponse;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.Favorite.FavoriteStoreResponseDto;
import com.example.foodfrontendservice.service.FavoriteStoreClientService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test/favorites")
@RequiredArgsConstructor
@Slf4j
public class FavoriteStoreController {

    private final FavoriteStoreClientService favoriteStoreClientService;


    /**
     * Добавить ресторан в избранное (тестовый endpoint)
     * POST /api/test/favorites/stores/{storeId}
     */
    @PostMapping("/stores/{storeId}")
    public ResponseEntity<FavoriteStoreApiResponse<FavoriteStoreResponseDto>> testAddToFavorites(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long storeId) {

        log.info("🧪 TEST: Добавление ресторана {} в избранное", storeId);

        try {
            // Извлекаем JWT из заголовка Authorization
            String jwt = extractJwtFromHeader(authHeader);

            if (jwt == null) {
                log.warn("❌ TEST: Некорректный Authorization header");
                return ResponseEntity.badRequest()
                        .body(FavoriteStoreApiResponse.error("Некорректный Authorization header"));
            }

            // Вызываем сервис
            FavoriteStoreApiResponse<FavoriteStoreResponseDto> response =
                    favoriteStoreClientService.addToFavorites(jwt, storeId);

            log.info("✅ TEST: Ответ от product-service: success={}, message={}",
                    response.getSuccess(), response.getMessage());

            HttpStatus status = Boolean.TRUE.equals(response.getSuccess()) ?
                    HttpStatus.CREATED : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status).body(response);

        } catch (Exception e) {
            log.error("💥 TEST: Ошибка в тестовом контроллере: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(FavoriteStoreApiResponse.error("Внутренняя ошибка сервера"));
        }
    }

    /**
     * Получить избранные рестораны (тестовый endpoint)
     * GET /api/test/favorites
     */
    @GetMapping
    public ResponseEntity<FavoriteStoreApiResponse<List<FavoriteStoreResponseDto>>> testGetMyFavorites(
            @RequestHeader("Authorization") String authHeader) {

        log.info("🧪 TEST: Получение избранных ресторанов");

        try {
            // Извлекаем JWT из заголовка Authorization
            String jwt = extractJwtFromHeader(authHeader);

            if (jwt == null) {
                log.warn("❌ TEST: Некорректный Authorization header");
                return ResponseEntity.badRequest()
                        .body(FavoriteStoreApiResponse.error("Некорректный Authorization header"));
            }

            // Вызываем сервис
            FavoriteStoreApiResponse<List<FavoriteStoreResponseDto>> response =
                    favoriteStoreClientService.getMyFavorites(jwt);

            log.info("✅ TEST: Получено избранных ресторанов: {}",
                    response.getData() != null ? response.getData().size() : 0);

            HttpStatus status = Boolean.TRUE.equals(response.getSuccess()) ?
                    HttpStatus.OK : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status).body(response);

        } catch (Exception e) {
            log.error("💥 TEST: Ошибка в тестовом контроллере: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(FavoriteStoreApiResponse.error("Внутренняя ошибка сервера"));
        }
    }

    // ================================
    // ДОПОЛНИТЕЛЬНЫЕ ТЕСТОВЫЕ МЕТОДЫ
    // ================================

    /**
     * Тестовый endpoint для проверки передачи JWT токена
     * GET /api/test/favorites/jwt-info
     */
    @GetMapping("/jwt-info")
    public ResponseEntity<Object> testJwtInfo(@RequestHeader("Authorization") String authHeader) {

        log.info("🔍 TEST: Анализ JWT токена");

        try {
            String jwt = extractJwtFromHeader(authHeader);

            if (jwt == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Некорректный Authorization header",
                        "received", authHeader
                ));
            }

            // Базовая информация о токене
            Map<String, Object> jwtInfo = Map.of(
                    "tokenLength", jwt.length(),
                    "tokenStart", jwt.substring(0, Math.min(20, jwt.length())) + "...",
                    "authHeader", authHeader,
                    "extractedJwt", jwt.substring(0, Math.min(50, jwt.length())) + "..."
            );

            log.info("🔍 TEST: JWT Info: {}", jwtInfo);

            return ResponseEntity.ok(jwtInfo);

        } catch (Exception e) {
            log.error("💥 TEST: Ошибка анализа JWT: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Ошибка анализа токена",
                    "message", e.getMessage()
            ));
        }
    }


    /**
     * Извлечь JWT токен из заголовка Authorization
     */
    private String extractJwtFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

}
