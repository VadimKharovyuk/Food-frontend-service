package com.example.foodfrontendservice.service;

import com.example.foodfrontendservice.Client.ProductServiceClient;
import com.example.foodfrontendservice.dto.CreateProductDto;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.Product.ProductResponseWrapper;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.SingleProductResponseWrapper;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductServiceClient productServiceClient;

    /**
     * ✅ Получить продукты по категории
     */
    public ProductResponseWrapper getProductsByCategory(Long categoryId, int page, int size) {
        log.info("🛒 Получение продуктов категории {}: page={}, size={}", categoryId, page, size);

        try {
            ResponseEntity<ProductResponseWrapper> response =
                    productServiceClient.getProductsByCategory(categoryId, page, size);

            if (response.getBody() != null) {
                ProductResponseWrapper wrapper = response.getBody();

                log.info("✅ Получено {} продуктов для категории {} (page: {}, hasNext: {})",
                        wrapper.getProductCount(), categoryId, page, wrapper.getHasNext());

                return wrapper;
            } else {
                log.warn("⚠️ Пустой ответ от product-service для категории {}", categoryId);
                return createEmptyProductsResponse(page, size, "Пустой ответ от сервиса продуктов");
            }

        } catch (Exception e) {
            log.error("💥 Ошибка получения продуктов категории {}: {}", categoryId, e.getMessage(), e);
            return createEmptyProductsResponse(page, size, "Ошибка связи с сервисом продуктов: " + e.getMessage());
        }
    }

    /**
     * ✅ Получить продукт по ID
     */
    public SingleProductResponseWrapper getProductById(Long productId) {
        log.info("🔍 Получение продукта по ID: {}", productId);

        try {
            ResponseEntity<SingleProductResponseWrapper> response =
                    productServiceClient.getProduct(productId);

            if (response.getBody() != null) {
                SingleProductResponseWrapper wrapper = response.getBody();

                if (wrapper.getSuccess()) {
                    log.info("✅ Продукт {} успешно получен", productId);
                } else {
                    log.warn("⚠️ Продукт {} не найден: {}", productId, wrapper.getMessage());
                }

                return wrapper;
            } else {
                log.warn("⚠️ Пустой ответ от product-service для продукта {}", productId);
                return createErrorProductResponse("Пустой ответ от сервиса продуктов");
            }

        } catch (Exception e) {
            log.error("💥 Ошибка получения продукта {}: {}", productId, e.getMessage(), e);
            return createErrorProductResponse("Ошибка связи с сервисом продуктов: " + e.getMessage());
        }
    }

    /**
     * ✅ Создать новый продукт
     */
    public SingleProductResponseWrapper createProduct(String productJson, MultipartFile imageFile, Long userId) {
        log.info("📝 Создание нового продукта для пользователя {}", userId);

        try {
            // Валидация входных данных
            if (productJson == null || productJson.trim().isEmpty()) {
                log.error("❌ Некорректные данные продукта: productJson пустой");
                return createErrorProductResponse("Некорректные данные продукта");
            }

            if (imageFile == null || imageFile.isEmpty()) {
                log.error("❌ Изображение продукта не предоставлено");
                return createErrorProductResponse("Изображение продукта обязательно");
            }

            // Валидация размера файла (например, максимум 5MB)
            long maxFileSize = 5 * 1024 * 1024; // 5MB
            if (imageFile.getSize() > maxFileSize) {
                log.error("❌ Размер изображения превышает лимит: {} bytes", imageFile.getSize());
                return createErrorProductResponse("Размер изображения не должен превышать 5MB");
            }

            // Валидация типа файла
            String contentType = imageFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                log.error("❌ Некорректный тип файла: {}", contentType);
                return createErrorProductResponse("Файл должен быть изображением");
            }

            ResponseEntity<SingleProductResponseWrapper> response =
                    productServiceClient.createProduct(productJson, imageFile, userId);

            if (response.getBody() != null) {
                SingleProductResponseWrapper wrapper = response.getBody();

                if (wrapper.getSuccess()) {
                    log.info("✅ Продукт успешно создан пользователем {}", userId);
                } else {
                    log.warn("⚠️ Ошибка создания продукта пользователем {}: {}", userId, wrapper.getMessage());
                }

                return wrapper;
            } else {
                log.warn("⚠️ Пустой ответ от product-service при создании продукта");
                return createErrorProductResponse("Пустой ответ от сервиса продуктов");
            }

        } catch (Exception e) {
            log.error("💥 Ошибка создания продукта пользователем {}: {}", userId, e.getMessage(), e);
            return createErrorProductResponse("Ошибка связи с сервисом продуктов: " + e.getMessage());
        }
    }

    /**
     * ✅ Удалить продукт
     */
    public boolean deleteProduct(Long productId, Long userId) {
        log.info("🗑️ Удаление продукта {} пользователем {}", productId, userId);

        try {
            ResponseEntity<Void> response = productServiceClient.deleteProduct(productId, userId);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Продукт {} успешно удален пользователем {}", productId, userId);
                return true;
            } else {
                log.warn("⚠️ Ошибка удаления продукта {}: HTTP {}", productId, response.getStatusCode());
                return false;
            }

        } catch (Exception e) {
            log.error("💥 Ошибка удаления продукта {} пользователем {}: {}", productId, userId, e.getMessage(), e);
            return false;
        }
    }


    /**
     * ✅ Получить количество продуктов в категории
     */
    public int getProductCountByCategory(Long categoryId) {
        log.debug("📊 Получение количества продуктов в категории {}", categoryId);

        try {
            ProductResponseWrapper response = getProductsByCategory(categoryId, 0, 1);

            if (response.getSuccess()) {
                int count = response.getTotalCount() != null ? response.getTotalCount() : 0;
                log.debug("📊 В категории {} найдено {} продуктов", categoryId, count);
                return count;
            } else {
                log.debug("📊 Не удалось получить количество продуктов в категории {}", categoryId);
                return 0;
            }

        } catch (Exception e) {
            log.error("💥 Ошибка получения количества продуктов в категории {}: {}", categoryId, e.getMessage());
            return 0;
        }
    }

    // ✅ Вспомогательные методы

    /**
     * Создать пустой ответ для списка продуктов
     */
    private ProductResponseWrapper createEmptyProductsResponse(int page, int size, String message) {
        return ProductResponseWrapper.builder()
                .products(List.of())
                .totalCount(0)
                .success(false)
                .message(message)
                .hasNext(false)
                .hasPrevious(false)
                .currentPage(page)
                .pageSize(size)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Создать ответ об ошибке для отдельного продукта
     */
    private SingleProductResponseWrapper createErrorProductResponse(String message) {
        return SingleProductResponseWrapper.builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * ✅ Получить статистику по продуктам (дополнительный метод)
     */
    public ProductStatistics getProductStatistics(Long categoryId) {
        log.debug("📊 Получение статистики продуктов для категории {}", categoryId);

        try {
            ProductResponseWrapper response = getProductsByCategory(categoryId, 0, Integer.MAX_VALUE);

            if (response.getSuccess() && response.hasProducts()) {
                return ProductStatistics.builder()
                        .totalProducts(response.getTotalCount())
                        .availableProducts((int) response.getProducts().stream()
                                .filter(p -> p.getIsAvailable() != null && p.getIsAvailable())
                                .count())
                        .productsWithDiscount((int) response.getProducts().stream()
                                .filter(p -> p.getHasDiscount() != null && p.getHasDiscount())
                                .count())
                        .popularProducts((int) response.getProducts().stream()
                                .filter(p -> p.getIsPopular() != null && p.getIsPopular())
                                .count())
                        .categoryId(categoryId)
                        .build();
            } else {
                return ProductStatistics.builder()
                        .totalProducts(0)
                        .availableProducts(0)
                        .productsWithDiscount(0)
                        .popularProducts(0)
                        .categoryId(categoryId)
                        .build();
            }

        } catch (Exception e) {
            log.error("💥 Ошибка получения статистики продуктов для категории {}: {}", categoryId, e.getMessage());
            return ProductStatistics.builder()
                    .totalProducts(0)
                    .availableProducts(0)
                    .productsWithDiscount(0)
                    .popularProducts(0)
                    .categoryId(categoryId)
                    .build();
        }
    }

    // ✅ Вложенный класс для статистики
    @Data
    @Builder
    public static class ProductStatistics {
        private Long categoryId;
        private Integer totalProducts;
        private Integer availableProducts;
        private Integer productsWithDiscount;
        private Integer popularProducts;
    }
}