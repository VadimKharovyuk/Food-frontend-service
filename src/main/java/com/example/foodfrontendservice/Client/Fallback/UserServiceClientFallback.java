package com.example.foodfrontendservice.Client.Fallback;//package com.example.foodfrontendservice.Client.Fallback;
//import com.example.foodfrontendservice.Client.UserServiceClient;
//import com.example.foodfrontendservice.dto.AuthResponseDto;
//import com.example.foodfrontendservice.dto.LoginRequestDto;
//import com.example.foodfrontendservice.dto.UserRegistrationDto;
//import com.example.foodfrontendservice.dto.UserResponseDto;
//import com.example.foodfrontendservice.enums.UserRole;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Component;
//
//import java.util.Arrays;
//import java.util.List;
//
//
//@Component("userServiceClientFallback")
//@Slf4j
//public class UserServiceClientFallback implements UserServiceClient {
//
//    @Override
//    public ResponseEntity<List<UserRole>> getAvailableRoles() {
//        log.warn("User Service недоступен - возвращаем fallback роли");
//        return ResponseEntity.ok(Arrays.asList(UserRole.BASE_USER, UserRole.BUSINESS_USER, UserRole.COURIER));
//    }
//
//    @Override
//    public ResponseEntity<UserResponseDto> register(UserRegistrationDto registrationDto) {
//        log.error("User Service недоступен - регистрация невозможна");
//        throw new RuntimeException("Сервис регистрации временно недоступен");
//    }
//
//    @Override
//    public ResponseEntity<Boolean> checkEmailAvailability(String email) {
//        log.warn("User Service недоступен - проверка email невозможна");
//        return ResponseEntity.ok(true);
//    }
//
//    @Override
//    public ResponseEntity<AuthResponseDto> login(LoginRequestDto loginRequest) {
//        log.error("User Service недоступен - авторизация невозможна");
//        throw new RuntimeException("Сервис авторизации временно недоступен");
//    }
//
//    @Override
//    public ResponseEntity<UserResponseDto> getCurrentUser(String authHeader) {
//        log.error("User Service недоступен - получение профиля невозможно");
//        throw new RuntimeException("Сервис пользователей временно недоступен");
//    }
//
//    @Override
//    public ResponseEntity<Boolean> validateToken(String authHeader) {
//        log.warn("User Service недоступен - валидация токена невозможна");
//        return ResponseEntity.ok(false);
//    }
//
//    @Override
//    public ResponseEntity<String> testAuthService() {
//        log.warn("User Service недоступен");
//        return ResponseEntity.ok("❌ User Service недоступен (fallback)");
//    }
//
//    @Override
//    public ResponseEntity<String> testRegistrationService() {
//        log.warn("Registration Service недоступен");
//        return ResponseEntity.ok("❌ Registration Service недоступен (fallback)");
//    }
//}


import com.example.foodfrontendservice.Client.UserServiceClient;
import com.example.foodfrontendservice.dto.*;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.category.ApiResponse;
import com.example.foodfrontendservice.enums.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 🛡️ Fallback для UserServiceClient
 * Обрабатывает ситуации, когда User Service недоступен
 */
@Component
@Slf4j
public class UserServiceClientFallback implements UserServiceClient {

    // ========== МЕЖСЕРВИСНЫЕ ENDPOINTS ==========



    @Override
    public AuthResponseDto login(LoginRequestDto loginRequest) {
        log.error("🔥 FALLBACK: User Service недоступен для login {}", loginRequest.getEmail());
        throw new RuntimeException("User Service недоступен для авторизации");
    }

    @Override
    public UserResponseDto getUserByToken(String authHeader) {
        log.error("🔥 FALLBACK: User Service недоступен для getUserByToken");
        throw new RuntimeException("User Service недоступен для получения пользователя");
    }

    @Override
    public Boolean validateTokenSimple(String authHeader) {
        log.error("🔥 FALLBACK: User Service недоступен для validateTokenSimple");
        return false; // Безопасно считаем токен невалидным
    }

    // ========== КЛИЕНТСКИЕ ENDPOINTS ==========

    @Override
    public ResponseEntity<ApiResponse<UserResponseDto>> getCurrentUser(String authHeader) {
        log.error("🔥 FALLBACK: User Service недоступен для getCurrentUser");

        ApiResponse<UserResponseDto> response = ApiResponse.<UserResponseDto>builder()
                .success(false)
                .message("Сервис пользователей временно недоступен")
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @Override
    public ResponseEntity<ApiResponse<TokenValidationDto>> validateToken(String authHeader) {
        log.error("🔥 FALLBACK: User Service недоступен для validateToken");

        TokenValidationDto validation = TokenValidationDto.builder()
                .valid(false)
                .build();

        ApiResponse<TokenValidationDto> response = ApiResponse.<TokenValidationDto>builder()
                .success(true)
                .message("Сервис недоступен - токен считается невалидным")
                .data(validation)
                .build();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ApiResponse<LogoutResponseDto>> logout(String authHeader) {
        log.error("🔥 FALLBACK: User Service недоступен для logout");

        LogoutResponseDto logoutResponse = LogoutResponseDto.builder()
                .success(true) // Всё равно считаем успешным
                .message("Выход выполнен локально (сервис недоступен)")
                .logoutTime(LocalDateTime.now())
                .build();

        ApiResponse<LogoutResponseDto> response = ApiResponse.<LogoutResponseDto>builder()
                .success(true)
                .message("Выход из системы выполнен")
                .data(logoutResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ApiResponse<ServiceHealthDto>> testAuthService() {
        log.error("🔥 FALLBACK: User Service недоступен для testAuthService");

        ServiceHealthDto health = ServiceHealthDto.builder()
                .serviceName("Auth Service")
                .status("DOWN")
                .message("Сервис недоступен")
                .version("unknown")
                .timestamp(LocalDateTime.now())
                .build();

        ApiResponse<ServiceHealthDto> response = ApiResponse.<ServiceHealthDto>builder()
                .success(false)
                .message("Auth Service недоступен")
                .data(health)
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    // ========== REGISTRATION ENDPOINTS ==========

    @Override
    public ResponseEntity<List<UserRole>> getAvailableRoles() {
        log.error("🔥 FALLBACK: User Service недоступен для getAvailableRoles");

        // Возвращаем базовые роли как fallback
        List<UserRole> defaultRoles = List.of(UserRole.BASE_USER, UserRole.BUSINESS_USER);
        return ResponseEntity.ok(defaultRoles);
    }

    @Override
    public ResponseEntity<UserResponseDto> register(UserRegistrationDto registrationDto) {
        log.error("🔥 FALLBACK: User Service недоступен для register {}", registrationDto.getEmail());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    @Override
    public ResponseEntity<Boolean> checkEmailAvailability(String email) {
        log.error("🔥 FALLBACK: User Service недоступен для checkEmailAvailability {}", email);

        // Безопасно считаем email занятым
        return ResponseEntity.ok(false);
    }

    @Override
    public ResponseEntity<String> testRegistrationService() {
        log.error("🔥 FALLBACK: User Service недоступен для testRegistrationService");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Registration Service недоступен");
    }



    @Override
    public ResponseEntity<UserResponseDto> getUserByTokenWithResponse(String authHeader) {
        log.error("🔥 FALLBACK: User Service недоступен для getUserByTokenWithResponse");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}