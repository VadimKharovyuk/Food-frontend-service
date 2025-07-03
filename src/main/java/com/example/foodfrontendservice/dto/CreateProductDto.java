package com.example.foodfrontendservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductDto {

//    @NotNull(message = "Store ID is required")
    private Long storeId;

//    @NotNull(message = "Category ID is required")
    private Long categoryId;

//    @NotBlank(message = "Product name is required")
//    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String name;

//    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

//    @NotNull(message = "Price is required")
//    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
//    @Digits(integer = 8, fraction = 2, message = "Price format is invalid")
    private BigDecimal price;

//    @DecimalMin(value = "0.01", message = "Discount price must be greater than 0")
//    @Digits(integer = 8, fraction = 2, message = "Discount price format is invalid")
    private BigDecimal discountPrice;

    @Builder.Default
    private Boolean isPopular = false;

    @Builder.Default
    private Boolean isAvailable = true;

    // Валидация скидочной цены
    @AssertTrue(message = "Discount price must be less than regular price")
    public boolean isDiscountPriceValid() {
        if (discountPrice == null) {
            return true;
        }
        return price != null && discountPrice.compareTo(price) < 0;
    }
}