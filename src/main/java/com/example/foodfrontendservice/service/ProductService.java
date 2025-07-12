package com.example.foodfrontendservice.service;

import com.example.foodfrontendservice.Client.ProductServiceClient;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.Product.ProductResponseWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
                return ProductResponseWrapper.builder()
                        .products(List.of())
                        .totalCount(0)
                        .success(false)
                        .message("Пустой ответ от сервиса продуктов")
                        .hasNext(false)
                        .hasPrevious(false)
                        .currentPage(page)
                        .pageSize(size)
                        .build();
            }

        } catch (Exception e) {
            log.error("💥 Ошибка получения продуктов категории {}: {}", categoryId, e.getMessage(), e);
            return ProductResponseWrapper.builder()
                    .products(List.of())
                    .totalCount(0)
                    .success(false)
                    .message("Ошибка связи с сервисом продуктов: " + e.getMessage())
                    .hasNext(false)
                    .hasPrevious(false)
                    .currentPage(page)
                    .pageSize(size)
                    .build();
        }
    }
}