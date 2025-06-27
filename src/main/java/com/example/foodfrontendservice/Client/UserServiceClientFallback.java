package com.example.foodfrontendservice.config;

import com.example.foodfrontendservice.Client.UserServiceClient;
import com.example.foodfrontendservice.dto.UserRegistrationDto;
import com.example.foodfrontendservice.dto.UserResponseDto;
import com.example.foodfrontendservice.enums.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 🛡️ Fallback для UserServiceClient при недоступности User Service
 */
@Component
@Slf4j
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public ResponseEntity<List<UserRole>> getAvailableRoles() {
        log.warn("User Service недоступен - возвращаем fallback роли");
        return ResponseEntity.ok(Arrays.asList(UserRole.BASE_USER, UserRole.BUSINESS_USER, UserRole.COURIER));
    }

    @Override
    public ResponseEntity<UserResponseDto> register(UserRegistrationDto registrationDto) {
        log.error("User Service недоступен - регистрация невозможна");
        throw new RuntimeException("Сервис регистрации временно недоступен");
    }

    @Override
    public ResponseEntity<Boolean> checkEmailAvailability(String email) {
        log.warn("User Service недоступен - проверка email невозможна");
        return ResponseEntity.ok(true); // Предполагаем что email доступен
    }

    @Override
    public ResponseEntity<AuthResponseDto> login(LoginRequestDto loginRequest) {
        log.error("User Service недоступен - авторизация невозможна");
        throw new RuntimeException("Сервис авторизации временно недоступен");
    }

    @Override
    public ResponseEntity<UserResponseDto> getCurrentUser(String authHeader) {
        log.error("User Service недоступен - получение профиля невозможно");
        throw new RuntimeException("Сервис пользователей временно недоступен");
    }

    @Override
    public ResponseEntity<Boolean> validateToken(String authHeader) {
        log.warn("User Service недоступен - валидация токена невозможна");
        return ResponseEntity.ok(false);
    }

    @Override
    public ResponseEntity<String> testAuthService() {
        log.warn("User Service недоступен");
        return ResponseEntity.ok("❌ User Service недоступен (fallback)");
    }

    @Override
    public ResponseEntity<String> testRegistrationService() {
        log.warn("Registration Service недоступен");
        return ResponseEntity.ok("❌ Registration Service недоступен (fallback)");
    }
}