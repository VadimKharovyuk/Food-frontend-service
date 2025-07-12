package com.example.foodfrontendservice.Client;

import com.example.foodfrontendservice.Client.Fallback.ProductServiceClientFallbackFactory;
import com.example.foodfrontendservice.config.FeignConfig;
import com.example.foodfrontendservice.dto.CreateProductDto;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.Product.ProductResponseWrapper;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.SingleProductResponseWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(
        name = "product-service",
        path = "/api/products",
        contextId = "productServiceClient",
        fallbackFactory = ProductServiceClientFallbackFactory.class,
        configuration = FeignConfig.class
)
public interface ProductServiceClient {

    /**
     * ✅ Получить продукты по категории
     */
    @GetMapping("/category/{categoryId}")
    ResponseEntity<ProductResponseWrapper> getProductsByCategory(
            @PathVariable("categoryId") Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    );

    /**
     * ✅ Создать новый продукт
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<SingleProductResponseWrapper> createProduct(
            @RequestPart(value = "product", required = true) String productJson,
            @RequestPart(value = "image", required = true) MultipartFile imageFile,
            @RequestHeader(value = "X-User-ID", required = false) Long userId
    );

    /**
     * ✅ Получить продукт по ID
     */
    @GetMapping("/{id}")
    ResponseEntity<SingleProductResponseWrapper> getProduct(@PathVariable("id") Long id);

    /**
     * ✅ Удалить продукт
     */
    @DeleteMapping("/{productId}")
    ResponseEntity<Void> deleteProduct(
            @PathVariable Long productId,
            @RequestHeader(value = "X-User-ID", required = false) Long userId
    );
}