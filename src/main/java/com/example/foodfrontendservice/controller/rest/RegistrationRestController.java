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

import java.util.List;
import java.util.Map;
//
//@RestController
//@RequestMapping("/api/registration")
//@RequiredArgsConstructor
//@Slf4j
//@CrossOrigin(origins = "*")
//public class RegistrationController {
//
//    private final UserIntegrationService userIntegrationService;
//
//
//    @GetMapping("/roles")
//    public ResponseEntity<List<UserRole>> getAvailableRoles() {
//        log.info("Запрос доступных ролей для регистрации");
//        try {
//            List<UserRole> roles = userIntegrationService.getAvailableRoles();
//            return ResponseEntity.ok(roles);
//        } catch (Exception e) {
//            log.error("Ошибка получения ролей: {}", e.getMessage());
//            return ResponseEntity.status(500).body(null);
//        }
//    }
//
//
//    @PostMapping("/register")
//    public ResponseEntity<Map<String, Object>> register(@RequestBody UserRegistrationDto registrationDto) {
//        log.info("Запрос на регистрацию пользователя: {}", registrationDto.getEmail());
//
//        try {
//            UserResponseDto user = userIntegrationService.registerUser(registrationDto);
//
//            log.info("Пользователь {} успешно зарегистрирован", user.getEmail());
//
//            return ResponseEntity.ok(Map.of(
//                    "success", true,
//                    "message", "Пользователь успешно зарегистрирован",
//                    "user", user
//            ));
//
//        } catch (Exception e) {
//            log.error("Ошибка регистрации пользователя {}: {}", registrationDto.getEmail(), e.getMessage());
//
//            return ResponseEntity.badRequest().body(Map.of(
//                    "success", false,
//                    "message", "Ошибка регистрации",
//                    "error", e.getMessage()
//            ));
//        }
//    }
//
//
//    @GetMapping("/check-email")
//    public ResponseEntity<Map<String, Object>> checkEmailAvailability(@RequestParam String email) {
//        log.info("Проверка доступности email: {}", email);
//
//        try {
//            Boolean isAvailable = userIntegrationService.checkEmailAvailability(email);
//
//            return ResponseEntity.ok(Map.of(
//                    "available", isAvailable,
//                    "email", email,
//                    "message", isAvailable ? "Email доступен" : "Email уже используется"
//            ));
//
//        } catch (Exception e) {
//            log.error("Ошибка проверки email {}: {}", email, e.getMessage());
//
//            return ResponseEntity.ok(Map.of(
//                    "available", true,
//                    "email", email,
//                    "message", "Не удалось проверить email"
//            ));
//        }
//    }
//
//
//}

/**
 * 🔗 REST API контроллер для регистрации (для AJAX и мобильных приложений)
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
            List<UserRole> roles = userIntegrationService.getAvailableRoles();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "roles", roles,
                    "message", "Роли успешно получены"
            ));
        } catch (Exception e) {
            log.error("❌ API: Ошибка получения ролей: {}", e.getMessage(), e);

            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Ошибка получения ролей",
                    "error", e.getMessage()
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
            // Проверяем доступность email
            Boolean isEmailAvailable = userIntegrationService.checkEmailAvailability(registrationDto.getEmail());
            if (!Boolean.TRUE.equals(isEmailAvailable)) {
                log.warn("📧 API: Email {} уже занят", registrationDto.getEmail());

                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Пользователь с таким email уже существует",
                        "field", "email"
                ));
            }

            // Регистрируем пользователя
            UserResponseDto registeredUser = userIntegrationService.registerUser(registrationDto);

            if (registeredUser != null) {
                log.info("✅ API: Пользователь {} успешно зарегистрирован", registeredUser.getEmail());

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Регистрация прошла успешно",
                        "user", registeredUser
                ));
            } else {
                throw new RuntimeException("Пустой ответ от сервера регистрации");
            }

        } catch (Exception e) {
            log.error("💥 API: Ошибка регистрации пользователя {}: {}", registrationDto.getEmail(), e.getMessage(), e);

            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Ошибка регистрации",
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * ✅ Проверка доступности email
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmailAvailability(@RequestParam String email) {
        log.debug("📧 API: Проверка доступности email: {}", email);

        try {
            Boolean isAvailable = userIntegrationService.checkEmailAvailability(email);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "available", isAvailable,
                    "email", email,
                    "message", Boolean.TRUE.equals(isAvailable) ? "Email доступен" : "Email уже занят"
            ));
        } catch (Exception e) {
            log.error("❌ API: Ошибка проверки email {}: {}", email, e.getMessage());

            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "available", false,
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
            // Пытаемся получить роли как тест подключения
            List<UserRole> roles = userIntegrationService.getAvailableRoles();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Регистрационный сервис работает",
                    "rolesCount", roles.size(),
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            log.error("❌ API: Ошибка тестирования сервиса: {}", e.getMessage());

            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Регистрационный сервис недоступен",
                    "error", e.getMessage()
            ));
        }
    }
}