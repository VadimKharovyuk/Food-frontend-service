package com.example.foodfrontendservice.dto.PRODUCTSERVICE;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AddressDto {
    private Long id;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
