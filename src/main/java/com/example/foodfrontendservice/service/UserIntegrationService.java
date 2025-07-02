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
            log.info("📋 Получаем доступные роли из User Service");
            ResponseEntity<List<UserRole>> response = userServiceClient.getAvailableRoles();

            if (response.getBody() != null) {
                log.info("✅ Получено {} ролей", response.getBody().size());
                return response.getBody();
            } else {
                log.warn("⚠️ Пустой ответ при получении ролей");
                return List.of();
            }
        } catch (Exception e) {
            log.error("❌ Ошибка получения ролей: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось получить список ролей");
        }
    }

    public UserResponseDto registerUser(UserRegistrationDto registrationDto) {
        try {
            log.info("📝 Регистрируем пользователя: {}", registrationDto.getEmail());
            ResponseEntity<UserResponseDto> response = userServiceClient.register(registrationDto);

            if (response.getBody() != null) {
                log.info("✅ Пользователь успешно зарегистрирован: {}", registrationDto.getEmail());
                return response.getBody();
            } else {
                log.error("❌ Пустой ответ при регистрации пользователя: {}", registrationDto.getEmail());
                throw new RuntimeException("Пустой ответ от сервера регистрации");
            }
        } catch (Exception e) {
            log.error("❌ Ошибка регистрации пользователя {}: {}", registrationDto.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Ошибка регистрации: " + e.getMessage());
        }
    }

    public Boolean checkEmailAvailability(String email) {
        try {
            log.debug("📧 Проверяем доступность email: {}", email);
            ResponseEntity<Boolean> response = userServiceClient.checkEmailAvailability(email);

            Boolean result = response.getBody();
            log.debug("✅ Email {} {}", email, Boolean.TRUE.equals(result) ? "доступен" : "занят");
            return result != null ? result : true;
        } catch (Exception e) {
            log.error("❌ Ошибка проверки email {}: {}", email, e.getMessage());
            return true; // По умолчанию считаем доступным
        }
    }

    public AuthResponseDto loginUser(LoginRequestDto loginRequest) {
        try {
            log.info("🔐 Авторизуем пользователя: {}", loginRequest.getEmail());
            ResponseEntity<AuthResponseDto> response = userServiceClient.login(loginRequest);

            if (response.getBody() != null && response.getBody().getToken() != null) {
                log.info("✅ Пользователь успешно авторизован: {}", loginRequest.getEmail());
                return response.getBody();
            } else {
                log.error("❌ Пустой ответ или токен при авторизации: {}", loginRequest.getEmail());
                throw new RuntimeException("Неверные учетные данные");
            }
        } catch (Exception e) {
            log.error("❌ Ошибка авторизации пользователя {}: {}", loginRequest.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Ошибка авторизации: " + e.getMessage());
        }
    }


    public UserResponseDto getCurrentUser(String authHeader) {
        try {
            log.info("👤 Получаем профиль пользователя по токену");


            String finalAuthHeader = authHeader;
            if (!authHeader.startsWith("Bearer ")) {
                finalAuthHeader = "Bearer " + authHeader;
                log.debug("🔧 Добавлен префикс Bearer к токену");
            }

            ResponseEntity<UserResponseDto> response = userServiceClient.getCurrentUser(finalAuthHeader);

            if (response.getBody() != null) {
                UserResponseDto user = response.getBody();
                log.info("✅ Профиль получен: {} (роль: {})", user.getEmail(), user.getUserRole());
                return user;
            } else {
                log.error("❌ Пустой ответ при получении профиля пользователя");
                throw new RuntimeException("Не удалось получить профиль пользователя");
            }
        } catch (Exception e) {
            log.error("❌ Ошибка получения профиля: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка получения профиля пользователя: " + e.getMessage());
        }
    }


    public Boolean validateToken(String authHeader) {
        try {
            log.debug("🔍 Валидируем токен");

            // Убеждаемся, что заголовок имеет правильный формат
            String finalAuthHeader = authHeader;
            if (!authHeader.startsWith("Bearer ")) {
                finalAuthHeader = "Bearer " + authHeader;
            }

            ResponseEntity<Boolean> response = userServiceClient.validateToken(finalAuthHeader);
            Boolean isValid = response.getBody();

            log.debug("✅ Результат валидации токена: {}", isValid);
            return Boolean.TRUE.equals(isValid);
        } catch (Exception e) {
            log.error("❌ Ошибка валидации токена: {}", e.getMessage());
            return false;
        }
    }

}