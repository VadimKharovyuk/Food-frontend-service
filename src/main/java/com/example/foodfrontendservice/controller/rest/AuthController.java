package com.example.foodfrontendservice.controller.rest;
import com.example.foodfrontendservice.Exception.BadCredentialsException;
import com.example.foodfrontendservice.dto.AuthResponseDto;
import com.example.foodfrontendservice.dto.LoginRequestDto;
import com.example.foodfrontendservice.dto.UserResponseDto;
import com.example.foodfrontendservice.service.UserIntegrationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
//


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserIntegrationService userIntegrationService;

    @PostMapping("/login")
    public AuthResponseDto login(@RequestBody LoginRequestDto loginRequest) {
        log.info("🔐 API /api/auth/login - Авторизация пользователя: {}", loginRequest.getEmail());

        AuthResponseDto authResponse = userIntegrationService.loginUser(loginRequest);

        log.info("✅ Пользователь {} успешно авторизован", loginRequest.getEmail());
        return authResponse;
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        log.debug("👤 Запрос информации о текущем пользователе");

        try {
            String token = authHeader.substring(7);
            UserResponseDto user = userIntegrationService.getUserByToken(token);

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
    public ResponseEntity<Map<String, Object>> logout(HttpServletResponse response) {
        log.info("🚪 Пользователь вышел из системы");

        // ✅ ОЧИЩАЕМ COOKIES при logout
        clearCookie(response, "jwt");
        clearCookie(response, "userRole");
        clearCookie(response, "userEmail");

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Успешный выход из системы"
        ));
    }

    private void clearCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        log.debug("🗑️ Cookie '{}' очищен", cookieName);
    }

    // ✅ DEBUG ENDPOINT
    @PostMapping("/login/debug")
    public ResponseEntity<Map<String, Object>> loginDebug(@RequestBody Map<String, Object> rawData) {
        log.info("🐛 DEBUG: Получены RAW данные для авторизации");
        log.info("🐛 RAW данные: {}", rawData);

        rawData.forEach((key, value) -> {
            log.info("🐛 {}: {} (type: {})",
                    key, value,
                    value != null ? value.getClass().getSimpleName() : "null");
        });

        return ResponseEntity.ok(Map.of(
                "receivedData", rawData,
                "timestamp", new java.util.Date(),
                "message", "Debug endpoint - данные получены"
        ));
    }
}