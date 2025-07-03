package com.example.foodfrontendservice.dto.PRODUCTSERVICE;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductResponseDto {
    private Long id;
    private Long storeId;
    private Long categoryId;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private String picUrl;
    private Boolean isAvailable;
    private BigDecimal rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isPopular;

    // Дополнительные поля для удобства
    private Boolean hasDiscount;
    private BigDecimal finalPrice; // Цена с учетом скидки

    public Boolean getHasDiscount() {
        return discountPrice != null && discountPrice.compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal getFinalPrice() {
        return getHasDiscount() ? discountPrice : price;
    }
}