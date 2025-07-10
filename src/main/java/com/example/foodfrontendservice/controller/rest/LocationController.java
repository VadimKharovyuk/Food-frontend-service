package com.example.foodfrontendservice.controller.rest;

import com.example.foodfrontendservice.config.CurrentUser;
import com.example.foodfrontendservice.dto.AUTSERVICE.UpdateUserAddressDto;
import com.example.foodfrontendservice.dto.AUTSERVICE.UpdateUserLocationDto;
import com.example.foodfrontendservice.dto.AUTSERVICE.UserLocationDto;
import com.example.foodfrontendservice.dto.AUTSERVICE.UserTokenInfo;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.category.ApiResponse;
import com.example.foodfrontendservice.dto.UserResponseDto;
import com.example.foodfrontendservice.service.UserLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
@Slf4j
public class LocationController {

    private final UserLocationService userLocationService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserLocationDto>> getUserLocation(
            @CurrentUser UserTokenInfo userInfo) {

        ApiResponse<UserLocationDto> response = userLocationService.getUserLocation(userInfo);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUserLocation(
            @Valid @RequestBody UpdateUserLocationDto locationDto,
            @CurrentUser UserTokenInfo userInfo) {

        ApiResponse<UserResponseDto> response = userLocationService.updateUserLocation(locationDto, userInfo);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/address")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUserAddress(
            @Valid @RequestBody UpdateUserAddressDto addressDto,
            @CurrentUser UserTokenInfo userInfo) {

        ApiResponse<UserResponseDto> response = userLocationService.updateUserAddress(addressDto, userInfo);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> clearUserLocation(
            @CurrentUser UserTokenInfo userInfo) {

        ApiResponse<UserResponseDto> response = userLocationService.clearUserLocation(userInfo);
        return ResponseEntity.ok(response);
    }
}