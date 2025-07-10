package com.example.foodfrontendservice.dto.AUTSERVICE;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// 📍 DTO для получения геолокации пользователя
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserLocationDto {

    @JsonProperty("latitude")
    private Double latitude;

    @JsonProperty("longitude")
    private Double longitude;

    @JsonProperty("street")
    private String street;

    @JsonProperty("city")
    private String city;

    @JsonProperty("region")
    private String region;

    @JsonProperty("country")
    private String country;

    @JsonProperty("fullAddress")
    private String fullAddress;

    // ✅ Это поле может отсутствовать в JSON
    @JsonProperty(value = "postalCode", required = false)
    private String postalCode;

    @JsonProperty("locationUpdatedAt")
    private String locationUpdatedAt;   // ← Изменено на String

    @JsonProperty("hasLocation")
    private Boolean hasLocation;

    // 📊 Дополнительная информация
    @JsonProperty("formattedCoordinates")
    private String formattedCoordinates;

    @JsonProperty("shortAddress")
    private String shortAddress;
}