package com.example.foodfrontendservice.dto.PRODUCTSERVICE;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class StoreResponseDto {
    private Long id;
    private Long ownerId;
    private String name;
    private String description;
    private AddressDto address;
    private String phone;
    private String email;
    private Boolean isActive;
    private BigDecimal rating;
    private Integer deliveryRadius;
    private BigDecimal deliveryFee;
    private Integer estimatedDeliveryTime;
    @JsonIgnore
    private MultipartFile imageFile;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
