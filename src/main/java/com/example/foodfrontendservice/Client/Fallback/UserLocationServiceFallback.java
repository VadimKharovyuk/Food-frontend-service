package com.example.foodfrontendservice.Client.Fallback;

import com.example.foodfrontendservice.Client.UserServiceLocationClient;
import com.example.foodfrontendservice.dto.AUTSERVICE.UpdateUserAddressDto;
import com.example.foodfrontendservice.dto.AUTSERVICE.UpdateUserLocationDto;
import com.example.foodfrontendservice.dto.AUTSERVICE.UserLocationDto;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.category.ApiResponse;
import com.example.foodfrontendservice.dto.UserResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserLocationServiceFallback implements UserServiceLocationClient {

    @Override
    public ApiResponse<UserLocationDto> getUserLocation(String authHeader) {
        log.error("🔴 Fallback: Сервис геолокации недоступен при получении локации");
        return ApiResponse.error("Сервис геолокации временно недоступен");
    }

    @Override
    public ApiResponse<UserResponseDto> updateUserLocation(UpdateUserLocationDto locationDto, String authHeader) {
        log.error("🔴 Fallback: Сервис геолокации недоступен при обновлении координат [{}, {}]",
                locationDto.getLatitude(), locationDto.getLongitude());
        return ApiResponse.error("Не удалось обновить геолокацию. Попробуйте позже");
    }

    @Override
    public ApiResponse<UserResponseDto> updateUserAddress(UpdateUserAddressDto addressDto, String authHeader) {
        log.error("🔴 Fallback: Сервис геолокации недоступен при обновлении адреса: {}, {}",
                addressDto.getStreet(), addressDto.getCity());
        return ApiResponse.error("Не удалось обновить адрес. Попробуйте позже");
    }

    @Override
    public ApiResponse<UserResponseDto> clearUserLocation(String authHeader) {
        log.error("🔴 Fallback: Сервис геолокации недоступен при очистке локации");
        return ApiResponse.error("Не удалось очистить геолокацию. Попробуйте позже");
    }
}