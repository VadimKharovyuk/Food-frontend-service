package com.example.foodfrontendservice.dto.PRODUCTSERVICE.store;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class CreateAddressRequest {
        @NotBlank(message = "Улица обязательна")
        private String street;

        @NotBlank(message = "Город обязателен")
        private String city;


        private String region;


        private String postalCode;

        @NotBlank(message = "Страна обязательна")
        private String country;

        private BigDecimal latitude;
        private BigDecimal longitude;


        private Boolean autoGeocode = true;
    }
