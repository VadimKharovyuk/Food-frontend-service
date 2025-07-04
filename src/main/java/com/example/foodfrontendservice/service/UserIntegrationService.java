//package com.example.foodfrontendservice.service;
//import com.example.foodfrontendservice.Client.UserServiceClient;
//import com.example.foodfrontendservice.dto.AuthResponseDto;
//import com.example.foodfrontendservice.dto.LoginRequestDto;
//import com.example.foodfrontendservice.dto.UserRegistrationDto;
//import com.example.foodfrontendservice.dto.UserResponseDto;
//import com.example.foodfrontendservice.enums.UserRole;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class UserIntegrationService {
//
//    private final UserServiceClient userServiceClient;
//
//
//    public List<UserRole> getAvailableRoles() {
//        try {
//            log.info("📋 Получаем доступные роли из User Service");
//            ResponseEntity<List<UserRole>> response = userServiceClient.getAvailableRoles();
//
//            if (response.getBody() != null) {
//                log.info("✅ Получено {} ролей", response.getBody().size());
//                return response.getBody();
//            } else {
//                log.warn("⚠️ Пустой ответ при получении ролей");
//                return List.of();
//            }
//        } catch (Exception e) {
//            log.error("❌ Ошибка получения ролей: {}", e.getMessage(), e);
//            throw new RuntimeException("Не удалось получить список ролей");
//        }
//    }
//
//    public UserResponseDto registerUser(UserRegistrationDto registrationDto) {
//        try {
//            log.info("📝 Регистрируем пользователя: {}", registrationDto.getEmail());
//            ResponseEntity<UserResponseDto> response = userServiceClient.register(registrationDto);
//
//            if (response.getBody() != null) {
//                log.info("✅ Пользователь успешно зарегистрирован: {}", registrationDto.getEmail());
//                return response.getBody();
//            } else {
//                log.error("❌ Пустой ответ при регистрации пользователя: {}", registrationDto.getEmail());
//                throw new RuntimeException("Пустой ответ от сервера регистрации");
//            }
//        } catch (Exception e) {
//            log.error("❌ Ошибка регистрации пользователя {}: {}", registrationDto.getEmail(), e.getMessage(), e);
//            throw new RuntimeException("Ошибка регистрации: " + e.getMessage());
//        }
//    }
//
//    public Boolean checkEmailAvailability(String email) {
//        try {
//            log.debug("📧 Проверяем доступность email: {}", email);
//            ResponseEntity<Boolean> response = userServiceClient.checkEmailAvailability(email);
//
//            Boolean result = response.getBody();
//            log.debug("✅ Email {} {}", email, Boolean.TRUE.equals(result) ? "доступен" : "занят");
//            return result != null ? result : true;
//        } catch (Exception e) {
//            log.error("❌ Ошибка проверки email {}: {}", email, e.getMessage());
//            return true; // По умолчанию считаем доступным
//        }
//    }
//
//    public AuthResponseDto loginUser(LoginRequestDto loginRequest) {
//        try {
//            log.info("🔐 Авторизуем пользователя: {}", loginRequest.getEmail());
//            ResponseEntity<AuthResponseDto> response = userServiceClient.login(loginRequest);
//
//            if (response.getBody() != null && response.getBody().getToken() != null) {
//                log.info("✅ Пользователь успешно авторизован: {}", loginRequest.getEmail());
//                return response.getBody();
//            } else {
//                log.error("❌ Пустой ответ или токен при авторизации: {}", loginRequest.getEmail());
//                throw new RuntimeException("Неверные учетные данные");
//            }
//        } catch (Exception e) {
//            log.error("❌ Ошибка авторизации пользователя {}: {}", loginRequest.getEmail(), e.getMessage(), e);
//            throw new RuntimeException("Ошибка авторизации: " + e.getMessage());
//        }
//    }
//
//
//    public UserResponseDto getCurrentUser(String authHeader) {
//        try {
//            log.info("👤 Получаем профиль пользователя по токену");
//
//
//            String finalAuthHeader = authHeader;
//            if (!authHeader.startsWith("Bearer ")) {
//                finalAuthHeader = "Bearer " + authHeader;
//                log.debug("🔧 Добавлен префикс Bearer к токену");
//            }
//
//            ResponseEntity<UserResponseDto> response = userServiceClient.getCurrentUser(finalAuthHeader);
//
//            if (response.getBody() != null) {
//                UserResponseDto user = response.getBody();
//                log.info("✅ Профиль получен: {} (роль: {})", user.getEmail(), user.getUserRole());
//                return user;
//            } else {
//                log.error("❌ Пустой ответ при получении профиля пользователя");
//                throw new RuntimeException("Не удалось получить профиль пользователя");
//            }
//        } catch (Exception e) {
//            log.error("❌ Ошибка получения профиля: {}", e.getMessage(), e);
//            throw new RuntimeException("Ошибка получения профиля пользователя: " + e.getMessage());
//        }
//    }
//
//
//    public Boolean validateToken(String authHeader) {
//        try {
//            log.debug("🔍 Валидируем токен");
//
//            // Убеждаемся, что заголовок имеет правильный формат
//            String finalAuthHeader = authHeader;
//            if (!authHeader.startsWith("Bearer ")) {
//                finalAuthHeader = "Bearer " + authHeader;
//            }
//
//            ResponseEntity<Boolean> response = userServiceClient.validateToken(finalAuthHeader);
//            Boolean isValid = response.getBody();
//
//            log.debug("✅ Результат валидации токена: {}", isValid);
//            return Boolean.TRUE.equals(isValid);
//        } catch (Exception e) {
//            log.error("❌ Ошибка валидации токена: {}", e.getMessage());
//            return false;
//        }
//    }
//
//}

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

    // ✅ ИСПРАВЛЕННЫЙ метод loginUser с null-safe проверкой
    public AuthResponseDto loginUser(LoginRequestDto loginRequest) {
        try {
            log.info("🔐 Авторизуем пользователя: {}", loginRequest.getEmail());

            // ✅ ДОБАВЛЯЕМ ДЕТАЛЬНОЕ ЛОГИРОВАНИЕ
            log.info("🍪 Remember Me в запросе: {} (type: {})",
                    loginRequest.getRememberMe(),
                    loginRequest.getRememberMe() != null ? loginRequest.getRememberMe().getClass().getSimpleName() : "null");

            // ✅ ЛОГИРУЕМ ВСЕ ДАННЫЕ ЗАПРОСА
            log.info("📤 Отправляем данные в User Service:");
            log.info("   📧 Email: {}", loginRequest.getEmail());
            log.info("   🔒 Password: {}", loginRequest.getPassword() != null ? "[PROTECTED]" : "null");
            log.info("   🍪 Remember Me: {}", loginRequest.getRememberMe());

            ResponseEntity<AuthResponseDto> response = userServiceClient.login(loginRequest);

            if (response.getBody() != null && response.getBody().getToken() != null) {
                AuthResponseDto authResponse = response.getBody();

                // ✅ ДЕТАЛЬНОЕ ЛОГИРОВАНИЕ ОТВЕТА
                log.info("📥 Получен ответ от User Service:");
                log.info("   🎫 Token: {}", authResponse.getToken() != null ? "PRESENT" : "NULL");
                log.info("   🏷️ Type: {}", authResponse.getType());
                log.info("   👤 User: {}", authResponse.getUser() != null ? authResponse.getUser().getEmail() : "NULL");
                log.info("   🍪 Remember Me в ответе: {} (type: {})",
                        authResponse.getRememberMe(),
                        authResponse.getRememberMe() != null ? authResponse.getRememberMe().getClass().getSimpleName() : "null");

                // ✅ NULL-SAFE ПРОВЕРКА СООТВЕТСТВИЯ
                Boolean requestRememberMe = loginRequest.getRememberMe() != null ? loginRequest.getRememberMe() : false;
                Boolean responseRememberMe = authResponse.getRememberMe() != null ? authResponse.getRememberMe() : false;

                if (!requestRememberMe.equals(responseRememberMe)) {
                    log.warn("⚠️ НЕСООТВЕТСТВИЕ Remember Me!");
                    log.warn("   Отправили: {} (normalized: {})", loginRequest.getRememberMe(), requestRememberMe);
                    log.warn("   Получили: {} (normalized: {})", authResponse.getRememberMe(), responseRememberMe);
                }

                log.info("✅ Пользователь успешно авторизован: {}", loginRequest.getEmail());
                return authResponse;
            } else {
                log.error("❌ Пустой ответ или токен при авторизации: {}", loginRequest.getEmail());
                log.error("   Response body: {}", response.getBody());
                log.error("   Token: {}", response.getBody() != null ? response.getBody().getToken() : "NULL");
                throw new RuntimeException("Неверные учетные данные");
            }
        } catch (Exception e) {
            log.error("❌ Ошибка авторизации пользователя {}: {}", loginRequest.getEmail(), e.getMessage(), e);

            // ✅ ДОПОЛНИТЕЛЬНАЯ ОТЛАДОЧНАЯ ИНФОРМАЦИЯ
            if (e.getCause() != null) {
                log.error("❌ Причина ошибки: {}", e.getCause().getMessage());
            }

            throw new RuntimeException("Ошибка авторизации: " + e.getMessage());
        }
    }
//    // ✅ ОБНОВЛЕННЫЙ МЕТОД С ЛОГИРОВАНИЕМ REMEMBER ME
//    public AuthResponseDto loginUser(LoginRequestDto loginRequest) {
//        try {
//            log.info("🔐 Авторизуем пользователя: {}", loginRequest.getEmail());
//
//            // ✅ ДОБАВЛЯЕМ ДЕТАЛЬНОЕ ЛОГИРОВАНИЕ
//            log.info("🍪 Remember Me в запросе: {} (type: {})",
//                    loginRequest.getRememberMe(),
//                    loginRequest.getRememberMe() != null ? loginRequest.getRememberMe().getClass().getSimpleName() : "null");
//
//            // ✅ ЛОГИРУЕМ ВСЕ ДАННЫЕ ЗАПРОСА
//            log.info("📤 Отправляем данные в User Service:");
//            log.info("   📧 Email: {}", loginRequest.getEmail());
//            log.info("   🔒 Password: {}", loginRequest.getPassword() != null ? "[PROTECTED]" : "null");
//            log.info("   🍪 Remember Me: {}", loginRequest.getRememberMe());
//
//            ResponseEntity<AuthResponseDto> response = userServiceClient.login(loginRequest);
//
//            if (response.getBody() != null && response.getBody().getToken() != null) {
//                AuthResponseDto authResponse = response.getBody();
//
//                // ✅ ДЕТАЛЬНОЕ ЛОГИРОВАНИЕ ОТВЕТА
//                log.info("📥 Получен ответ от User Service:");
//                log.info("   🎫 Token: {}", authResponse.getToken() != null ? "PRESENT" : "NULL");
//                log.info("   🏷️ Type: {}", authResponse.getType());
//                log.info("   👤 User: {}", authResponse.getUser() != null ? authResponse.getUser().getEmail() : "NULL");
//                log.info("   🍪 Remember Me в ответе: {} (type: {})",
//                        authResponse.getRememberMe(),
//                        authResponse.getRememberMe() != null ? authResponse.getRememberMe().getClass().getSimpleName() : "null");
//
//                // ✅ ПРОВЕРЯЕМ СООТВЕТСТВИЕ
//                if (!loginRequest.getRememberMe().equals(authResponse.getRememberMe())) {
//                    log.warn("⚠️ НЕСООТВЕТСТВИЕ Remember Me!");
//                    log.warn("   Отправили: {}", loginRequest.getRememberMe());
//                    log.warn("   Получили: {}", authResponse.getRememberMe());
//                }
//
//                log.info("✅ Пользователь успешно авторизован: {}", loginRequest.getEmail());
//                return authResponse;
//            } else {
//                log.error("❌ Пустой ответ или токен при авторизации: {}", loginRequest.getEmail());
//                log.error("   Response body: {}", response.getBody());
//                log.error("   Token: {}", response.getBody() != null ? response.getBody().getToken() : "NULL");
//                throw new RuntimeException("Неверные учетные данные");
//            }
//        } catch (Exception e) {
//            log.error("❌ Ошибка авторизации пользователя {}: {}", loginRequest.getEmail(), e.getMessage(), e);
//
//            // ✅ ДОПОЛНИТЕЛЬНАЯ ОТЛАДОЧНАЯ ИНФОРМАЦИЯ
//            if (e.getCause() != null) {
//                log.error("❌ Причина ошибки: {}", e.getCause().getMessage());
//            }
//
//            throw new RuntimeException("Ошибка авторизации: " + e.getMessage());
//        }
//    }

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