//package com.example.foodfrontendservice.controller.rest;
//
//import com.example.foodfrontendservice.dto.UserRegistrationDto;
//import com.example.foodfrontendservice.dto.UserResponseDto;
//import com.example.foodfrontendservice.enums.UserRole;
//import com.example.foodfrontendservice.service.UserIntegrationService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
////
////@RestController
////@RequestMapping("/api/registration")
////@RequiredArgsConstructor
////@Slf4j
////@CrossOrigin(origins = "*")
////public class RegistrationController {
////
////    private final UserIntegrationService userIntegrationService;
////
////
////    @GetMapping("/roles")
////    public ResponseEntity<List<UserRole>> getAvailableRoles() {
////        log.info("Запрос доступных ролей для регистрации");
////        try {
////            List<UserRole> roles = userIntegrationService.getAvailableRoles();
////            return ResponseEntity.ok(roles);
////        } catch (Exception e) {
////            log.error("Ошибка получения ролей: {}", e.getMessage());
////            return ResponseEntity.status(500).body(null);
////        }
////    }
////
////
////    @PostMapping("/register")
////    public ResponseEntity<Map<String, Object>> register(@RequestBody UserRegistrationDto registrationDto) {
////        log.info("Запрос на регистрацию пользователя: {}", registrationDto.getEmail());
////
////        try {
////            UserResponseDto user = userIntegrationService.registerUser(registrationDto);
////
////            log.info("Пользователь {} успешно зарегистрирован", user.getEmail());
////
////            return ResponseEntity.ok(Map.of(
////                    "success", true,
////                    "message", "Пользователь успешно зарегистрирован",
////                    "user", user
////            ));
////
////        } catch (Exception e) {
////            log.error("Ошибка регистрации пользователя {}: {}", registrationDto.getEmail(), e.getMessage());
////
////            return ResponseEntity.badRequest().body(Map.of(
////                    "success", false,
////                    "message", "Ошибка регистрации",
////                    "error", e.getMessage()
////            ));
////        }
////    }
////
////
////    @GetMapping("/check-email")
////    public ResponseEntity<Map<String, Object>> checkEmailAvailability(@RequestParam String email) {
////        log.info("Проверка доступности email: {}", email);
////
////        try {
////            Boolean isAvailable = userIntegrationService.checkEmailAvailability(email);
////
////            return ResponseEntity.ok(Map.of(
////                    "available", isAvailable,
////                    "email", email,
////                    "message", isAvailable ? "Email доступен" : "Email уже используется"
////            ));
////
////        } catch (Exception e) {
////            log.error("Ошибка проверки email {}: {}", email, e.getMessage());
////
////            return ResponseEntity.ok(Map.of(
////                    "available", true,
////                    "email", email,
////                    "message", "Не удалось проверить email"
////            ));
////        }
////    }
////
////
////}
//
///**
// * 🔗 REST API контроллер для регистрации (для AJAX и мобильных приложений)
// */
//@RestController
//@RequestMapping("/api/registration")
//@RequiredArgsConstructor
//@Slf4j
//@CrossOrigin(origins = "*")
//public class RegistrationRestController {
//
//    private final UserIntegrationService userIntegrationService;
//
//    /**
//     * 📋 Получить доступные роли
//     */
//    @GetMapping("/roles")
//    public ResponseEntity<Map<String, Object>> getAvailableRoles() {
//        log.info("📋 API: Запрос доступных ролей");
//
//        try {
//            List<UserRole> roles = userIntegrationService.getAvailableRoles();
//
//            return ResponseEntity.ok(Map.of(
//                    "success", true,
//                    "roles", roles,
//                    "message", "Роли успешно получены"
//            ));
//        } catch (Exception e) {
//            log.error("❌ API: Ошибка получения ролей: {}", e.getMessage(), e);
//
//            return ResponseEntity.status(500).body(Map.of(
//                    "success", false,
//                    "message", "Ошибка получения ролей",
//                    "error", e.getMessage()
//            ));
//        }
//    }
//
//    /**
//     * 📝 Регистрация пользователя через API
//     */
//    @PostMapping("/register")
//    public ResponseEntity<Map<String, Object>> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
//        log.info("📝 API: Попытка регистрации пользователя: {}", registrationDto.getEmail());
//
//        try {
//            // Проверяем доступность email
//            Boolean isEmailAvailable = userIntegrationService.checkEmailAvailability(registrationDto.getEmail());
//            if (!Boolean.TRUE.equals(isEmailAvailable)) {
//                log.warn("📧 API: Email {} уже занят", registrationDto.getEmail());
//
//                return ResponseEntity.badRequest().body(Map.of(
//                        "success", false,
//                        "message", "Пользователь с таким email уже существует",
//                        "field", "email"
//                ));
//            }
//
//            // Регистрируем пользователя
//            UserResponseDto registeredUser = userIntegrationService.registerUser(registrationDto);
//
//            if (registeredUser != null) {
//                log.info("✅ API: Пользователь {} успешно зарегистрирован", registeredUser.getEmail());
//
//                return ResponseEntity.ok(Map.of(
//                        "success", true,
//                        "message", "Регистрация прошла успешно",
//                        "user", registeredUser
//                ));
//            } else {
//                throw new RuntimeException("Пустой ответ от сервера регистрации");
//            }
//
//        } catch (Exception e) {
//            log.error("💥 API: Ошибка регистрации пользователя {}: {}", registrationDto.getEmail(), e.getMessage(), e);
//
//            return ResponseEntity.status(500).body(Map.of(
//                    "success", false,
//                    "message", "Ошибка регистрации",
//                    "error", e.getMessage()
//            ));
//        }
//    }
//
//    /**
//     * ✅ Проверка доступности email
//     */
//    @GetMapping("/check-email")
//    public ResponseEntity<Map<String, Object>> checkEmailAvailability(@RequestParam String email) {
//        log.debug("📧 API: Проверка доступности email: {}", email);
//
//        try {
//            Boolean isAvailable = userIntegrationService.checkEmailAvailability(email);
//
//            return ResponseEntity.ok(Map.of(
//                    "success", true,
//                    "available", isAvailable,
//                    "email", email,
//                    "message", Boolean.TRUE.equals(isAvailable) ? "Email доступен" : "Email уже занят"
//            ));
//        } catch (Exception e) {
//            log.error("❌ API: Ошибка проверки email {}: {}", email, e.getMessage());
//
//            return ResponseEntity.status(500).body(Map.of(
//                    "success", false,
//                    "available", false,
//                    "message", "Ошибка проверки email",
//                    "error", e.getMessage()
//            ));
//        }
//    }
//
//    /**
//     * 🧪 Тест подключения к регистрационному сервису
//     */
//    @GetMapping("/test")
//    public ResponseEntity<Map<String, Object>> testRegistrationService() {
//        log.info("🧪 API: Тест регистрационного сервиса");
//
//        try {
//            // Пытаемся получить роли как тест подключения
//            List<UserRole> roles = userIntegrationService.getAvailableRoles();
//
//            return ResponseEntity.ok(Map.of(
//                    "success", true,
//                    "message", "Регистрационный сервис работает",
//                    "rolesCount", roles.size(),
//                    "timestamp", System.currentTimeMillis()
//            ));
//        } catch (Exception e) {
//            log.error("❌ API: Ошибка тестирования сервиса: {}", e.getMessage());
//
//            return ResponseEntity.status(500).body(Map.of(
//                    "success", false,
//                    "message", "Регистрационный сервис недоступен",
//                    "error", e.getMessage()
//            ));
//        }
//    }
//}

package com.example.foodfrontendservice.controller.rest;

import com.example.foodfrontendservice.dto.UserRegistrationDto;
import com.example.foodfrontendservice.dto.UserResponseDto;
import com.example.foodfrontendservice.enums.UserRole;
import com.example.foodfrontendservice.service.UserIntegrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 🔗 REST API контроллер для регистрации (для AJAX и мобильных приложений)
 * ОБНОВЛЕН для работы с новой архитектурой UserIntegrationService
 */
@RestController
@RequestMapping("/api/registration")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RegistrationRestController {

    private final UserIntegrationService userIntegrationService;

    /**
     * 📋 Получить доступные роли
     */
    @GetMapping("/roles")
    public ResponseEntity<Map<String, Object>> getAvailableRoles() {
        log.info("📋 API: Запрос доступных ролей");

        try {
            // ✅ Используем обновленный метод
            ResponseEntity<List<UserRole>> response = userIntegrationService.getAvailableRoles();
            List<UserRole> roles = response.getBody();

            if (roles != null) {
                log.info("✅ API: Получено {} ролей", roles.size());

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "roles", roles,
                        "count", roles.size(),
                        "message", "Роли успешно получены"
                ));
            } else {
                log.warn("⚠️ API: Пустой список ролей");

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "roles", List.of(),
                        "count", 0,
                        "message", "Роли недоступны"
                ));
            }

        } catch (Exception e) {
            log.error("❌ API: Ошибка получения ролей: {}", e.getMessage(), e);

            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Ошибка получения ролей",
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * 📝 Регистрация пользователя через API
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        log.info("📝 API: Попытка регистрации пользователя: {}", registrationDto.getEmail());

        try {
            // ✅ Проверяем доступность email через обновленный метод
            ResponseEntity<Boolean> emailCheckResponse = userIntegrationService.checkEmailAvailability(registrationDto.getEmail());
            Boolean isEmailAvailable = emailCheckResponse.getBody();

            if (!Boolean.TRUE.equals(isEmailAvailable)) {
                log.warn("📧 API: Email {} уже занят", registrationDto.getEmail());

                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Пользователь с таким email уже существует",
                        "field", "email",
                        "email", registrationDto.getEmail()
                ));
            }

            // ✅ Регистрируем пользователя через обновленный метод
            ResponseEntity<UserResponseDto> registerResponse = userIntegrationService.registerUser(registrationDto);
            UserResponseDto registeredUser = registerResponse.getBody();

            if (registeredUser != null) {
                log.info("✅ API: Пользователь {} успешно зарегистрирован (ID: {})",
                        registeredUser.getEmail(), registeredUser.getId());

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Регистрация прошла успешно",
                        "user", registeredUser,
                        "redirectUrl", determineRedirectUrl(registeredUser.getUserRole())
                ));
            } else {
                throw new RuntimeException("Пустой ответ от сервера регистрации");
            }

        } catch (Exception e) {
            log.error("💥 API: Ошибка регистрации пользователя {}: {}", registrationDto.getEmail(), e.getMessage(), e);

            // Определяем тип ошибки для более точного ответа
            String errorMessage = "Ошибка регистрации";
            String errorField = null;

            if (e.getMessage().contains("email") || e.getMessage().contains("Email")) {
                errorMessage = "Пользователь с таким email уже существует";
                errorField = "email";
            } else if (e.getMessage().contains("password") || e.getMessage().contains("пароль")) {
                errorMessage = "Некорректный пароль";
                errorField = "password";
            } else if (e.getMessage().contains("validation") || e.getMessage().contains("валидация")) {
                errorMessage = "Ошибка валидации данных";
            }

            Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "message", errorMessage,
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            );

            // Добавляем поле с ошибкой, если определено
            if (errorField != null) {
                errorResponse = new java.util.HashMap<>(errorResponse);
                errorResponse.put("field", errorField);
            }

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * ✅ Проверка доступности email
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmailAvailability(@RequestParam String email) {
        log.debug("📧 API: Проверка доступности email: {}", email);

        try {
            // ✅ Используем обновленный метод
            ResponseEntity<Boolean> response = userIntegrationService.checkEmailAvailability(email);
            Boolean isAvailable = response.getBody();

            log.debug("✅ API: Email {} - доступен: {}", email, isAvailable);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "available", Boolean.TRUE.equals(isAvailable),
                    "email", email,
                    "message", Boolean.TRUE.equals(isAvailable) ? "Email доступен" : "Email уже занят"
            ));

        } catch (Exception e) {
            log.error("❌ API: Ошибка проверки email {}: {}", email, e.getMessage());

            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "available", false,
                    "email", email,
                    "message", "Ошибка проверки email",
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * 🧪 Тест подключения к регистрационному сервису
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testRegistrationService() {
        log.info("🧪 API: Тест регистрационного сервиса");

        try {
            // ✅ Используем обновленный метод
            ResponseEntity<String> testResponse = userIntegrationService.testRegistrationService();
            String testResult = testResponse.getBody();

            // Также тестируем получение ролей
            ResponseEntity<List<UserRole>> rolesResponse = userIntegrationService.getAvailableRoles();
            List<UserRole> roles = rolesResponse.getBody();

            log.info("✅ API: Регистрационный сервис доступен");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Регистрационный сервис работает",
                    "testResult", testResult != null ? testResult : "OK",
                    "rolesCount", roles != null ? roles.size() : 0,
                    "availableRoles", roles != null ? roles : List.of(),
                    "timestamp", LocalDateTime.now(),
                    "serviceStatus", "UP"
            ));

        } catch (Exception e) {
            log.error("❌ API: Ошибка тестирования сервиса: {}", e.getMessage());

            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Регистрационный сервис недоступен",
                    "error", e.getMessage(),
                    "serviceStatus", "DOWN",
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    // ========== UTILITY МЕТОДЫ ==========

    /**
     * 🎯 Определение URL для редиректа после регистрации
     */
    private String determineRedirectUrl(UserRole userRole) {
        return switch (userRole) {
            case ADMIN -> "/admin/dashboard";
            case BUSINESS_USER -> "/business/dashboard";
            case COURIER -> "/courier/dashboard";
            case BASE_USER -> "/user/dashboard";
            default -> "/dashboard";
        };
    }

    // ========== ОБРАБОТЧИКИ ОШИБОК ==========

    /**
     * ❌ Обработчик ошибок валидации
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException e) {

        log.error("❌ API: Ошибка валидации: {}", e.getMessage());

        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(java.util.stream.Collectors.joining(", "));

        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Ошибка валидации данных",
                "validationErrors", errorMessage,
                "timestamp", LocalDateTime.now()
        ));
    }

    /**
     * ❌ Обработчик общих исключений
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        log.error("❌ API: Общая ошибка в RegistrationController: {}", e.getMessage());

        return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Внутренняя ошибка сервера",
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now()
        ));
    }
}