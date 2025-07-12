package com.example.foodfrontendservice.dto.PRODUCTSERVICE.Product;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductResponseWrapper {
    private List<ProductResponseDto> products;
    private Integer totalCount;
    private Boolean hasNext;
    private Boolean hasPrevious;
    private Integer currentPage;
    private Integer pageSize;
    private Boolean success;
    private String message;
    private LocalDateTime timestamp;

    // ✅ Вспомогательные методы для проверки
    public boolean isSuccessful() {
        return success != null && success;
    }

    public boolean hasProducts() {
        return products != null && !products.isEmpty();
    }

    public int getProductCount() {
        return products != null ? products.size() : 0;
    }
}
