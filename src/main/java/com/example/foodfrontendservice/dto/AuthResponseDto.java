package com.example.foodfrontendservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {
    private String token;
    @Builder.Default
    private String type = "Bearer";
    private UserResponseDto user;
    private Boolean rememberMe;
}