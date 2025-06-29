package com.example.foodfrontendservice.dto.PRODUCTSERVICE;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreBriefDto {
    private Long id;
    private String name;
    private Boolean isActive;
    private BigDecimal rating;
    private BigDecimal deliveryFee;
    private Integer estimatedDeliveryTime;
    private String picUrl;
    private String addressCity;
    private String addressStreet;
}