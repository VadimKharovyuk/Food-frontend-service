package com.example.foodfrontendservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)  // ✅ Игнорировать неизвестные поля
public class AuthResponseDto {
    private String token;

    @Builder.Default
    private String type = "Bearer";

    @JsonProperty("user")  // ✅ Явно указать маппинг
    private UserResponseDto user;

    private Boolean rememberMe;
}