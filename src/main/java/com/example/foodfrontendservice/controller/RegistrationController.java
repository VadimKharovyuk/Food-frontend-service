package com.example.foodfrontendservice.controller;

import com.example.foodfrontendservice.dto.UserRegistrationDto;
import com.example.foodfrontendservice.dto.UserResponseDto;
import com.example.foodfrontendservice.enums.UserRole;
import com.example.foodfrontendservice.service.UserIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/registration")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RegistrationController {

    private final UserIntegrationService userIntegrationService;


    @GetMapping("/roles")
    public ResponseEntity<List<UserRole>> getAvailableRoles() {
        log.info("Запрос доступных ролей для регистрации");
        try {
            List<UserRole> roles = userIntegrationService.getAvailableRoles();
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            log.error("Ошибка получения ролей: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }


    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody UserRegistrationDto registrationDto) {
        log.info("Запрос на регистрацию пользователя: {}", registrationDto.getEmail());

        try {
            UserResponseDto user = userIntegrationService.registerUser(registrationDto);

            log.info("Пользователь {} успешно зарегистрирован", user.getEmail());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Пользователь успешно зарегистрирован",
                    "user", user
            ));

        } catch (Exception e) {
            log.error("Ошибка регистрации пользователя {}: {}", registrationDto.getEmail(), e.getMessage());

            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Ошибка регистрации",
                    "error", e.getMessage()
            ));
        }
    }


    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmailAvailability(@RequestParam String email) {
        log.info("Проверка доступности email: {}", email);

        try {
            Boolean isAvailable = userIntegrationService.checkEmailAvailability(email);

            return ResponseEntity.ok(Map.of(
                    "available", isAvailable,
                    "email", email,
                    "message", isAvailable ? "Email доступен" : "Email уже используется"
            ));

        } catch (Exception e) {
            log.error("Ошибка проверки email {}: {}", email, e.getMessage());

            return ResponseEntity.ok(Map.of(
                    "available", true,
                    "email", email,
                    "message", "Не удалось проверить email"
            ));
        }
    }


}