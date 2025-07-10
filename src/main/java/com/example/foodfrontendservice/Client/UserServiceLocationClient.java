package com.example.foodfrontendservice.Client;

import com.example.foodfrontendservice.Client.Fallback.UserLocationServiceFallback;
import com.example.foodfrontendservice.Client.Fallback.UserServiceClientFallback;
import com.example.foodfrontendservice.config.FeignConfig;
import com.example.foodfrontendservice.dto.AUTSERVICE.UpdateUserAddressDto;
import com.example.foodfrontendservice.dto.AUTSERVICE.UpdateUserLocationDto;
import com.example.foodfrontendservice.dto.AUTSERVICE.UserLocationDto;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.category.ApiResponse;
import com.example.foodfrontendservice.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "user-service",
        url = "http://localhost:8081",
        path = "/api",
        fallback = UserLocationServiceFallback.class,
        configuration = FeignConfig.class,
        contextId = "userServiceLocationClient"
)
public interface UserServiceLocationClient {

    /**
     * 📍 Получение геолокации пользователя
     */
    @GetMapping("/users/me/location")
    ApiResponse<UserLocationDto> getUserLocation(@RequestHeader("Authorization") String authHeader);

    /**
     * 📍 Обновление координат пользователя
     */
    @PutMapping("/users/me/location")
    ApiResponse<UserResponseDto> updateUserLocation(
            @RequestBody UpdateUserLocationDto locationDto,
            @RequestHeader("Authorization") String authHeader
    );

    /**
     * 🏠 Обновление адреса пользователя
     */
    @PutMapping("/users/me/address")
    ApiResponse<UserResponseDto> updateUserAddress(
            @RequestBody UpdateUserAddressDto addressDto,
            @RequestHeader("Authorization") String authHeader
    );

    /**
     * 🧹 Очистка геолокации пользователя
     */
    @DeleteMapping("/users/me/location")
    ApiResponse<UserResponseDto> clearUserLocation(@RequestHeader("Authorization") String authHeader);
}
