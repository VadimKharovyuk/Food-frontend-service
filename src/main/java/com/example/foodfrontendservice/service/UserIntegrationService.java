package com.example.foodfrontendservice.service;
import com.example.foodfrontendservice.Client.UserServiceClient;
import com.example.foodfrontendservice.dto.AuthResponseDto;
import com.example.foodfrontendservice.dto.LoginRequestDto;
import com.example.foodfrontendservice.dto.UserRegistrationDto;
import com.example.foodfrontendservice.dto.UserResponseDto;
import com.example.foodfrontendservice.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserIntegrationService {


    private final UserServiceClient userServiceClient;


    public List<UserRole> getAvailableRoles() {
        try {
            log.info("Получаем доступные роли из User Service");
            ResponseEntity<List<UserRole>> response = userServiceClient.getAvailableRoles();
            return response.getBody();
        } catch (Exception e) {
            log.error("Ошибка получения ролей: {}", e.getMessage());
            throw new RuntimeException("Не удалось получить список ролей");
        }
    }


    public UserResponseDto registerUser(UserRegistrationDto registrationDto) {
        try {
            log.info("Регистрируем пользователя: {}", registrationDto.getEmail());
            ResponseEntity<UserResponseDto> response = userServiceClient.register(registrationDto);
            log.info("Пользователь успешно зарегистрирован: {}", registrationDto.getEmail());
            return response.getBody();
        } catch (Exception e) {
            log.error("Ошибка регистрации пользователя {}: {}", registrationDto.getEmail(), e.getMessage());
            throw new RuntimeException("Ошибка регистрации: " + e.getMessage());
        }
    }


    public Boolean checkEmailAvailability(String email) {
        try {
            log.info("Проверяем доступность email: {}", email);
            ResponseEntity<Boolean> response = userServiceClient.checkEmailAvailability(email);
            return response.getBody();
        } catch (Exception e) {
            log.error("Ошибка проверки email {}: {}", email, e.getMessage());
            return true; // По умолчанию считаем доступным
        }
    }


    public AuthResponseDto loginUser(LoginRequestDto loginRequest) {
        try {
            log.info("Авторизуем пользователя: {}", loginRequest.getEmail());
            ResponseEntity<AuthResponseDto> response = userServiceClient.login(loginRequest);
            log.info("Пользователь успешно авторизован: {}", loginRequest.getEmail());
            return response.getBody();
        } catch (Exception e) {
            log.error("Ошибка авторизации пользователя {}: {}", loginRequest.getEmail(), e.getMessage());
            throw new RuntimeException("Ошибка авторизации: " + e.getMessage());
        }
    }


    public UserResponseDto getCurrentUser(String token) {
        try {
            log.info("Получаем профиль пользователя по токену");
            String authHeader = "Bearer " + token;
            ResponseEntity<UserResponseDto> response = userServiceClient.getCurrentUser(authHeader);
            return response.getBody();
        } catch (Exception e) {
            log.error("Ошибка получения профиля: {}", e.getMessage());
            throw new RuntimeException("Ошибка получения профиля пользователя");
        }
    }


    public Boolean validateToken(String token) {
        try {
            log.debug("Валидируем токен");
            String authHeader = "Bearer " + token;
            ResponseEntity<Boolean> response = userServiceClient.validateToken(authHeader);
            return response.getBody();
        } catch (Exception e) {
            log.error("Ошибка валидации токена: {}", e.getMessage());
            return false;
        }
    }


}