package com.example.foodfrontendservice.service;

import com.example.foodfrontendservice.Client.UserServiceLocationClient;
import com.example.foodfrontendservice.dto.AUTSERVICE.UpdateUserAddressDto;
import com.example.foodfrontendservice.dto.AUTSERVICE.UpdateUserLocationDto;
import com.example.foodfrontendservice.dto.AUTSERVICE.UserLocationDto;
import com.example.foodfrontendservice.dto.AUTSERVICE.UserTokenInfo;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.category.ApiResponse;
import com.example.foodfrontendservice.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
@Slf4j
public class UserLocationService {

    private final UserServiceLocationClient userServiceLocationClient;

    /**
     * 📍 Получение геолокации пользователя
     */
    public ApiResponse<UserLocationDto> getUserLocation(UserTokenInfo userInfo) {
        log.debug("📍 Запрос геолокации пользователя {} через микросервис", userInfo.getUserId());

        String authHeader = "Bearer " + userInfo.getToken();
        return userServiceLocationClient.getUserLocation(authHeader);
    }

    /**
     * 📍 Обновление координат пользователя
     */
    public ApiResponse<UserResponseDto> updateUserLocation(UpdateUserLocationDto locationDto, UserTokenInfo userInfo) {
        log.info("🌍 Обновление геолокации пользователя {}: [{}, {}]",
                userInfo.getUserId(), locationDto.getLatitude(), locationDto.getLongitude());

        String authHeader = "Bearer " + userInfo.getToken();
        return userServiceLocationClient.updateUserLocation(locationDto, authHeader);
    }

    /**
     * 🏠 Обновление адреса пользователя
     */
    public ApiResponse<UserResponseDto> updateUserAddress(UpdateUserAddressDto addressDto, UserTokenInfo userInfo) {
        log.info("🏠 Обновление адреса пользователя {}: {}, {}",
                userInfo.getUserId(), addressDto.getStreet(), addressDto.getCity());

        String authHeader = "Bearer " + userInfo.getToken();
        return userServiceLocationClient.updateUserAddress(addressDto, authHeader);
    }

    /**
     * 🧹 Очистка геолокации пользователя
     */
    public ApiResponse<UserResponseDto> clearUserLocation(UserTokenInfo userInfo) {
        log.info("🧹 Очистка геолокации пользователя {}", userInfo.getUserId());

        String authHeader = "Bearer " + userInfo.getToken();
        return userServiceLocationClient.clearUserLocation(authHeader);
    }
}