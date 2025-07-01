package com.example.foodfrontendservice.dto.PRODUCTSERVICE.store;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateAddressDto {

    @NotBlank(message = "Улица обязательна")
    @Size(max = 200, message = "Адрес улицы не должен превышать 200 символов")
    private String street;

    @NotBlank(message = "Город обязателен")
    @Size(max = 100, message = "Название города не должно превышать 100 символов")
    private String city;

    @Size(max = 100, message = "Название региона не должно превышать 100 символов")
    private String region;

    @Pattern(regexp = "^[0-9]{5,10}$", message = "Неверный формат почтового индекса")
    private String postalCode;

    @NotBlank(message = "Страна обязательна")
    @Size(max = 100, message = "Название страны не должно превышать 100 символов")
    private String country;

    // Географические координаты (заполняются автоматически)
    private BigDecimal latitude;
    private BigDecimal longitude;

    // Флаг для автоматического геокодирования
    private Boolean autoGeocode = true;

    // Дополнительная информация
    private String apartment;
    private String floor;
    private String entrance;
    private String intercom;
    private String notes;

    // Методы для удобства
    public String getFullAddress() {
        StringBuilder fullAddress = new StringBuilder();

        if (street != null) {
            fullAddress.append(street);
        }
        if (apartment != null) {
            fullAddress.append(", кв. ").append(apartment);
        }
        if (city != null) {
            fullAddress.append(", ").append(city);
        }
        if (region != null) {
            fullAddress.append(", ").append(region);
        }
        if (postalCode != null) {
            fullAddress.append(", ").append(postalCode);
        }
        if (country != null) {
            fullAddress.append(", ").append(country);
        }

        return fullAddress.toString();
    }

    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }

    public boolean isValid() {
        return street != null && !street.trim().isEmpty() &&
                city != null && !city.trim().isEmpty() &&
                country != null && !country.trim().isEmpty();
    }

    @Override
    public String toString() {
        return String.format("CreateAddressDto{street='%s', city='%s', country='%s', hasCoordinates=%s}",
                street, city, country, hasCoordinates());
    }
}