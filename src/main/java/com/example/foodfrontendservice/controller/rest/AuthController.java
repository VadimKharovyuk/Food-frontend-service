//package com.example.foodfrontendservice.controller.rest;
//
//import com.example.foodfrontendservice.dto.AuthResponseDto;
//import com.example.foodfrontendservice.dto.LoginRequestDto;
//import com.example.foodfrontendservice.dto.UserResponseDto;
//import com.example.foodfrontendservice.service.UserIntegrationService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//
//
//@RestController
//@RequestMapping("/api/auth")
//@RequiredArgsConstructor
//@Slf4j
//@CrossOrigin(origins = "*")
//public class AuthController {
//
//    private final UserIntegrationService userIntegrationService;
//
//
//    @PostMapping("/login")
//    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequestDto loginRequest) {
//        log.info("Попытка авторизации пользователя: {}", loginRequest.getEmail());
//
//        try {
//
//            AuthResponseDto authResponse = userIntegrationService.loginUser(loginRequest);
//
//            if (authResponse != null && authResponse.getToken() != null) {
//                log.info("Пользователь {} успешно авторизован", loginRequest.getEmail());
//
//                return ResponseEntity.ok(Map.of(
//                        "success", true,
//                        "message", "Успешная авторизация",
//                        "token", authResponse.getToken(),
//                        "type", authResponse.getType() != null ? authResponse.getType() : "Bearer",
//                        "user", authResponse.getUser()
//                ));
//            } else {
//                log.warn("AuthResponse пустой для пользователя: {}", loginRequest.getEmail());
//                return ResponseEntity.status(401).body(Map.of(
//                        "success", false,
//                        "message", "Неверный email или пароль"
//                ));
//            }
//
//        } catch (Exception e) {
//            log.error("Ошибка авторизации пользователя {}: {}", loginRequest.getEmail(), e.getMessage(), e);
//
//            return ResponseEntity.status(401).body(Map.of(
//                    "success", false,
//                    "message", "Ошибка авторизации",
//                    "error", e.getMessage()
//            ));
//        }
//    }
//
//
//    @GetMapping("/me")
//    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
//        log.debug("Запрос информации о текущем пользователе");
//
//        try {
//            // Извлекаем токен из заголовка "Bearer TOKEN"
//            String token = authHeader.substring(7);
//            UserResponseDto user = userIntegrationService.getCurrentUser(token);
//
//            return ResponseEntity.ok(Map.of(
//                    "success", true,
//                    "user", user
//            ));
//
//        } catch (Exception e) {
//            log.error("Ошибка получения информации о пользователе: {}", e.getMessage());
//
//            return ResponseEntity.status(401).body(Map.of(
//                    "success", false,
//                    "message", "Ошибка получения информации о пользователе",
//                    "error", e.getMessage()
//            ));
//        }
//    }
//
//
//    @PostMapping("/validate-token")
//    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String authHeader) {
//        log.debug("Валидация JWT токена");
//
//        try {
//            String token = authHeader.substring(7);
//            Boolean isValid = userIntegrationService.validateToken(token);
//
//            return ResponseEntity.ok(Map.of(
//                    "valid", isValid,
//                    "message", isValid ? "Токен валиден" : "Токен невалиден"
//            ));
//
//        } catch (Exception e) {
//            log.error("Ошибка валидации токена: {}", e.getMessage());
//
//            return ResponseEntity.ok(Map.of(
//                    "valid", false,
//                    "message", "Ошибка валидации токена"
//            ));
//        }
//    }
//
//
//    @PostMapping("/logout")
//    public ResponseEntity<Map<String, Object>> logout() {
//        log.info("Пользователь вышел из системы");
//
//        return ResponseEntity.ok(Map.of(
//                "success", true,
//                "message", "Успешный выход из системы"
//        ));
//    }
//
//
//}

package com.example.foodfrontendservice.controller.rest;

import com.example.foodfrontendservice.dto.AuthResponseDto;
import com.example.foodfrontendservice.dto.LoginRequestDto;
import com.example.foodfrontendservice.dto.UserResponseDto;
import com.example.foodfrontendservice.service.UserIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserIntegrationService userIntegrationService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequestDto loginRequest) {
        log.info("🔐 Попытка авторизации пользователя: {}", loginRequest.getEmail());

        // ✅ ДОБАВЛЯЕМ ЛОГИРОВАНИЕ REMEMBER ME
        log.info("🍪 Remember Me от клиента: {} (type: {})",
                loginRequest.getRememberMe(),
                loginRequest.getRememberMe() != null ? loginRequest.getRememberMe().getClass().getSimpleName() : "null");

        try {
            AuthResponseDto authResponse = userIntegrationService.loginUser(loginRequest);

            if (authResponse != null && authResponse.getToken() != null) {
                log.info("✅ Пользователь {} успешно авторизован", loginRequest.getEmail());

                // ✅ ДОБАВЛЯЕМ ЛОГИРОВАНИЕ REMEMBER ME ИЗ ОТВЕТА
                log.info("🍪 Remember Me в ответе от user-service: {}", authResponse.getRememberMe());

                // ✅ ИСПРАВЛЯЕМ ОТВЕТ - ДОБАВЛЯЕМ REMEMBER ME
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Успешная авторизация",
                        "token", authResponse.getToken(),
                        "type", authResponse.getType() != null ? authResponse.getType() : "Bearer",
                        "user", authResponse.getUser(),
                        "rememberMe", authResponse.getRememberMe() != null ? authResponse.getRememberMe() : false // ✅ ДОБАВЛЕНО
                ));
            } else {
                log.warn("⚠️ AuthResponse пустой для пользователя: {}", loginRequest.getEmail());
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Неверный email или пароль"
                ));
            }

        } catch (Exception e) {
            log.error("❌ Ошибка авторизации пользователя {}: {}", loginRequest.getEmail(), e.getMessage(), e);

            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Ошибка авторизации",
                    "error", e.getMessage()
            ));
        }
    }

    // ✅ ДОБАВЛЯЕМ DEBUG ENDPOINT
    @PostMapping("/login/debug")
    public ResponseEntity<Map<String, Object>> loginDebug(
            @RequestBody Map<String, Object> rawData,
            HttpServletRequest request) {

        log.info("🐛 DEBUG: Получены RAW данные для авторизации");
        log.info("🐛 RAW данные: {}", rawData);

        // Логируем каждое поле отдельно
        rawData.forEach((key, value) -> {
            log.info("🐛 {}: {} (type: {})",
                    key, value,
                    value != null ? value.getClass().getSimpleName() : "null");
        });

        // Логируем заголовки
        log.info("🐛 Content-Type: {}", request.getContentType());
        log.info("🐛 Content-Length: {}", request.getContentLength());

        // Возвращаем отладочную информацию
        Map<String, Object> response = new HashMap<>();
        response.put("receivedData", rawData);
        response.put("timestamp", new Date());
        response.put("message", "Debug endpoint - данные получены на frontend-service");

        return ResponseEntity.ok(response);
    }

    // ✅ ДОБАВЛЯЕМ TEST DTO ENDPOINT
    @PostMapping("/login/test-dto")
    public ResponseEntity<Map<String, Object>> testLoginDto(@RequestBody LoginRequestDto loginRequest) {

        log.info("🧪 TEST DTO: Получен LoginRequestDto");
        log.info("🧪   email: {}", loginRequest.getEmail());
        log.info("🧪   password: {}", loginRequest.getPassword() != null ? "[PROTECTED]" : "null");
        log.info("🧪   rememberMe: {} (type: {})",
                loginRequest.getRememberMe(),
                loginRequest.getRememberMe() != null ? loginRequest.getRememberMe().getClass().getSimpleName() : "null");

        Map<String, Object> response = new HashMap<>();
        response.put("email", loginRequest.getEmail());
        response.put("rememberMe", loginRequest.getRememberMe());
        response.put("rememberMeType", loginRequest.getRememberMe() != null ?
                loginRequest.getRememberMe().getClass().getSimpleName() : "null");
        response.put("message", "DTO test completed on frontend-service");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        log.debug("👤 Запрос информации о текущем пользователе");

        try {
            // Извлекаем токен из заголовка "Bearer TOKEN"
            String token = authHeader.substring(7);
            UserResponseDto user = userIntegrationService.getCurrentUser(token);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "user", user
            ));

        } catch (Exception e) {
            log.error("❌ Ошибка получения информации о пользователе: {}", e.getMessage());

            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Ошибка получения информации о пользователе",
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String authHeader) {
        log.debug("🔍 Валидация JWT токена");

        try {
            String token = authHeader.substring(7);
            Boolean isValid = userIntegrationService.validateToken(token);

            return ResponseEntity.ok(Map.of(
                    "valid", isValid,
                    "message", isValid ? "Токен валиден" : "Токен невалиден"
            ));

        } catch (Exception e) {
            log.error("❌ Ошибка валидации токена: {}", e.getMessage());

            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "message", "Ошибка валидации токена"
            ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        log.info("🚪 Пользователь вышел из системы");

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Успешный выход из системы"
        ));
    }
}