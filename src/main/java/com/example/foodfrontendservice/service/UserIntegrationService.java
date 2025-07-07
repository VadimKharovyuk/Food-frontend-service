package com.example.foodfrontendservice.service;
import com.example.foodfrontendservice.Client.UserServiceClient;
import com.example.foodfrontendservice.dto.*;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.category.ApiResponse;
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



    public AuthResponseDto loginUser(LoginRequestDto loginRequest) {
        try {
            log.info("🔗 Вызов User Service для авторизации: {}", loginRequest.getEmail());

            // ✅ Используем межсервисный endpoint
            AuthResponseDto response = userServiceClient.login(loginRequest);

            if (response != null && response.getUser() != null) {
                log.info("✅ Успешная авторизация через User Service: {}", response.getUser().getEmail());
                return response;
            } else {
                throw new RuntimeException("Пустой ответ от User Service");
            }

        } catch (feign.RetryableException e) {
            log.error("🔌 Ошибка подключения к User Service: {}", e.getMessage());
            throw new RuntimeException("User Service недоступен. Проверьте подключение к сервису.");

        } catch (feign.FeignException.Unauthorized e) {
            log.error("🔐 Ошибка авторизации в User Service: {}", e.getMessage());
            throw new RuntimeException("Неверный email или пароль");

        } catch (feign.FeignException.ServiceUnavailable e) {
            log.error("🚫 User Service недоступен: {}", e.getMessage());
            throw new RuntimeException("Сервис авторизации временно недоступен");

        } catch (feign.FeignException e) {
            log.error("🌐 Ошибка Feign при вызове User Service: {} - {}", e.status(), e.getMessage());

            if (e.status() == 401) {
                throw new RuntimeException("Неверный email или пароль");
            } else if (e.status() == 404) {
                throw new RuntimeException("Сервис авторизации не найден");
            } else if (e.status() >= 500) {
                throw new RuntimeException("Внутренняя ошибка сервиса авторизации");
            } else {
                throw new RuntimeException("Ошибка авторизации: " + e.getMessage());
            }

        } catch (Exception e) {
            log.error("❌ Неожиданная ошибка при вызове User Service login: {}", e.getMessage(), e);

            // Детальная обработка различных типов ошибок
            String errorMessage = determineErrorMessage(e);
            throw new RuntimeException(errorMessage);
        }
    }

    /**
     * 🔍 Определение типа ошибки для пользователя
     */
    private String determineErrorMessage(Exception e) {
        String message = e.getMessage().toLowerCase();

        if (message.contains("connect timed out") || message.contains("connection refused")) {
            return "Сервис авторизации недоступен. Попробуйте позже.";
        } else if (message.contains("read timed out")) {
            return "Превышено время ожидания ответа от сервиса авторизации.";
        } else if (message.contains("unknown host") || message.contains("name resolution")) {
            return "Не удается найти сервис авторизации.";
        } else if (message.contains("eureka")) {
            return "Ошибка обнаружения сервисов. Проверьте конфигурацию.";
        } else {
            return "Ошибка авторизации: " + e.getMessage();
        }
    }

    /**
     * 🔗 Получение пользователя по токену - МЕЖСЕРВИСНЫЙ ВЫЗОВ
     * Для внутренней валидации в других endpoints
     */
    public UserResponseDto getUserByToken(String token) {
        try {
            log.debug("🔗 Вызов User Service для получения пользователя по токену");

            // ✅ Используем межсервисный endpoint
            UserResponseDto user = userServiceClient.getUserByToken("Bearer " + token);

            log.debug("✅ Получен пользователь: {}", user.getEmail());
            return user;

        } catch (Exception e) {
            log.error("❌ Ошибка получения пользователя: {}", e.getMessage());
            throw new RuntimeException("Пользователь не найден");
        }
    }

    /**
     * 🔗 Простая валидация токена - МЕЖСЕРВИСНЫЙ ВЫЗОВ
     * Для быстрых проверок без получения полных данных пользователя
     */
    public boolean validateToken(String token) {
        try {
            log.debug("🔗 Вызов User Service для валидации токена");

            // ✅ Используем межсервисный endpoint
            Boolean isValid = userServiceClient.validateTokenSimple("Bearer " + token);

            log.debug("✅ Результат валидации токена: {}", isValid);
            return Boolean.TRUE.equals(isValid);

        } catch (Exception e) {
            log.error("❌ Ошибка валидации токена: {}", e.getMessage());
            return false; // Безопасно считаем невалидным
        }
    }


    // ========== REGISTRATION МЕТОДЫ ==========

    /**
     * 📋 Получение доступных ролей
     */
    public ResponseEntity<List<UserRole>> getAvailableRoles() {
        try {
            log.debug("📋 Получение доступных ролей из User Service");

            ResponseEntity<List<UserRole>> response = userServiceClient.getAvailableRoles();
            List<UserRole> roles = response.getBody();

            log.debug("✅ Получено {} ролей", roles != null ? roles.size() : 0);
            return response;

        } catch (Exception e) {
            log.error("❌ Ошибка получения ролей: {}", e.getMessage());

            // Fallback роли
            List<UserRole> fallbackRoles = List.of(UserRole.BASE_USER, UserRole.BUSINESS_USER);
            return ResponseEntity.ok(fallbackRoles);
        }
    }

    /**
     * 📝 Регистрация пользователя
     */
    public ResponseEntity<UserResponseDto> registerUser(UserRegistrationDto registrationDto) {
        try {
            log.info("📝 Регистрация пользователя через User Service: {}", registrationDto.getEmail());

            ResponseEntity<UserResponseDto> response = userServiceClient.register(registrationDto);
            UserResponseDto user = response.getBody();

            if (user != null) {
                log.info("✅ Пользователь {} успешно зарегистрирован (ID: {})",
                        user.getEmail(), user.getId());
            }

            return response;

        } catch (Exception e) {
            log.error("❌ Ошибка регистрации: {}", e.getMessage());
            throw new RuntimeException("Ошибка регистрации: " + e.getMessage());
        }
    }

    /**
     * ✅ Проверка доступности email
     */
    public ResponseEntity<Boolean> checkEmailAvailability(String email) {
        try {
            log.debug("✅ Проверка доступности email: {}", email);

            ResponseEntity<Boolean> response = userServiceClient.checkEmailAvailability(email);
            Boolean isAvailable = response.getBody();

            log.debug("✅ Email {} - доступен: {}", email, isAvailable);
            return response;

        } catch (Exception e) {
            log.error("❌ Ошибка проверки email: {}", e.getMessage());

            // Безопасно считаем занятым при ошибке
            return ResponseEntity.ok(false);
        }
    }

    /**
     * 🧪 Тест регистрационного сервиса
     */
    public ResponseEntity<String> testRegistrationService() {
        try {
            log.debug("🧪 Тестирование Registration Service");

            ResponseEntity<String> response = userServiceClient.testRegistrationService();
            String result = response.getBody();

            log.debug("✅ Registration Service тест: {}", result);
            return response;

        } catch (Exception e) {
            log.error("❌ Ошибка тестирования: {}", e.getMessage());
            return ResponseEntity.status(503).body("Service Unavailable: " + e.getMessage());
        }
    }

    /**
     * DTO для статистики подключения к сервисам
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ServiceConnectionStats {
        private Boolean authServiceAvailable;
        private Boolean registrationServiceAvailable;
        private Integer totalAvailableRoles;
        private java.time.LocalDateTime lastChecked;
    }
}