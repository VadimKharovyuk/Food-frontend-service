package com.example.foodfrontendservice.Client.Fallback;

import com.example.foodfrontendservice.Client.ProductServiceClient;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.Product.ProductResponseWrapper;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.SingleProductResponseWrapper;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * ✅ FallbackFactory для ProductServiceClient
 * Предоставляет более детальную обработку ошибок с информацией о причине сбоя
 */
@Component
@Slf4j
public class ProductServiceClientFallbackFactory implements FallbackFactory<ProductServiceClient> {

    @Override
    public ProductServiceClient create(Throwable cause) {
        return new ProductServiceClient() {

            @Override
            public ResponseEntity<ProductResponseWrapper> getProductsByCategory(Long categoryId, int page, int size) {
                String errorMessage = getErrorMessage(cause, "получения продуктов категории");
                String userMessage = getUserFriendlyMessage(cause);

                log.error("💥 Fallback для getProductsByCategory(categoryId: {}, page: {}, size: {}): {}",
                        categoryId, page, size, errorMessage, cause);

                ProductResponseWrapper fallbackResponse = ProductResponseWrapper.builder()
                        .products(Collections.emptyList())
                        .totalCount(0)
                        .hasNext(false)
                        .hasPrevious(false)
                        .currentPage(page)
                        .pageSize(size)
                        .success(false)
                        .message(userMessage)
                        .timestamp(LocalDateTime.now())
                        .build();

                return ResponseEntity.ok(fallbackResponse);
            }

            @Override
            public ResponseEntity<SingleProductResponseWrapper> createProduct(String productJson, MultipartFile imageFile, Long userId) {
                String errorMessage = getErrorMessage(cause, "создания продукта");
                String userMessage = getUserFriendlyMessage(cause);

                log.error("💥 Fallback для createProduct(userId: {}): {}", userId, errorMessage, cause);

                SingleProductResponseWrapper fallbackResponse = SingleProductResponseWrapper.builder()
                        .success(false)
                        .message(userMessage)
                        .timestamp(LocalDateTime.now())
                        .build();

                return ResponseEntity.ok(fallbackResponse);
            }

            @Override
            public ResponseEntity<SingleProductResponseWrapper> getProduct(Long id) {
                String errorMessage = getErrorMessage(cause, "получения продукта");
                String userMessage = getUserFriendlyMessage(cause);

                log.error("💥 Fallback для getProduct(id: {}): {}", id, errorMessage, cause);

                SingleProductResponseWrapper fallbackResponse = SingleProductResponseWrapper.builder()
                        .success(false)
                        .message(userMessage)
                        .timestamp(LocalDateTime.now())
                        .build();

                return ResponseEntity.ok(fallbackResponse);
            }

            @Override
            public ResponseEntity<Void> deleteProduct(Long productId, Long userId) {
                String errorMessage = getErrorMessage(cause, "удаления продукта");

                log.error("💥 Fallback для deleteProduct(productId: {}, userId: {}): {}",
                        productId, userId, errorMessage, cause);

                // Возвращаем соответствующий HTTP статус в зависимости от ошибки
                if (cause instanceof FeignException.ServiceUnavailable) {
                    return ResponseEntity.status(503).build(); // Service Unavailable
                } else if (cause instanceof FeignException.InternalServerError) {
                    return ResponseEntity.status(500).build(); // Internal Server Error
                } else {
                    return ResponseEntity.status(502).build(); // Bad Gateway
                }
            }

            /**
             * ✅ Определение типа ошибки и соответствующего сообщения
             */
            private String getErrorMessage(Throwable cause, String operation) {
                if (cause instanceof FeignException) {
                    FeignException feignException = (FeignException) cause;
                    return String.format("Ошибка %s: HTTP %d - %s",
                            operation, feignException.status(), feignException.getMessage());
                } else if (cause instanceof java.net.ConnectException) {
                    return String.format("Ошибка подключения при %s: сервис недоступен", operation);
                } else if (cause instanceof java.net.SocketTimeoutException) {
                    return String.format("Таймаут при %s: сервис не отвечает", operation);
                } else {
                    return String.format("Неизвестная ошибка при %s: %s", operation, cause.getMessage());
                }
            }

            /**
             * ✅ Пользовательские сообщения об ошибках
             */
            private String getUserFriendlyMessage(Throwable cause) {
                if (cause instanceof FeignException) {
                    FeignException feignException = (FeignException) cause;

                    switch (feignException.status()) {
                        case 404:
                            return "Запрашиваемый ресурс не найден";
                        case 500:
                            return "Внутренняя ошибка сервера. Попробуйте позже";
                        case 503:
                            return "Сервис временно недоступен. Попробуйте через несколько минут";
                        case 400:
                            return "Некорректный запрос";
                        case 401:
                            return "Необходима авторизация";
                        case 403:
                            return "Доступ запрещен";
                        default:
                            return "Сервис продуктов временно недоступен";
                    }
                } else if (cause instanceof java.net.ConnectException) {
                    return "Сервис продуктов недоступен. Проверьте интернет-соединение";
                } else if (cause instanceof java.net.SocketTimeoutException) {
                    return "Сервис продуктов не отвечает. Попробуйте позже";
                } else {
                    return "Временные проблемы с загрузкой. Попробуйте обновить страницу";
                }
            }
        };
    }
}