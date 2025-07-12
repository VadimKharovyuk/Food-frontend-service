package com.example.foodfrontendservice.dto.PRODUCTSERVICE.Product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
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

    // ✅ Дополнительные поля для удобства (автоматически вычисляются)
    private Boolean hasDiscount;
    private BigDecimal finalPrice;

    // ✅ Геттеры с логикой (как в оригинале)
    public Boolean getHasDiscount() {
        return discountPrice != null && discountPrice.compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal getFinalPrice() {
        return getHasDiscount() ? discountPrice : price;
    }
}