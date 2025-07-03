package com.example.foodfrontendservice.dto.PRODUCTSERVICE;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


// Для одного продукта
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SingleProductResponseWrapper {
    private ProductResponseDto product;
    private Boolean success;
    private String message;
    private LocalDateTime timestamp;

    public static SingleProductResponseWrapper success(ProductResponseDto product) {
        return SingleProductResponseWrapper.builder()
                .product(product)
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static SingleProductResponseWrapper notFound(Long id) {
        return SingleProductResponseWrapper.builder()
                .product(null)
                .success(false)
                .message("Product not found with ID: " + id)
                .timestamp(LocalDateTime.now())
                .build();
    }
}