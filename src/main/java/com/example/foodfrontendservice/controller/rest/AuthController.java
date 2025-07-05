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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
//

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserIntegrationService userIntegrationService;

    // ✅ ОСНОВНОЙ LOGIN ENDPOINT для JSON запросов
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody LoginRequestDto loginRequest,
            HttpServletResponse response) {

        log.info("🔐 API /api/auth/login - Авторизация пользователя: {}", loginRequest.getEmail());

        try {
            AuthResponseDto authResponse = userIntegrationService.loginUser(loginRequest);

            if (authResponse != null && authResponse.getToken() != null) {
                log.info("✅ Пользователь {} успешно авторизован", loginRequest.getEmail());

                // ✅ УСТАНАВЛИВАЕМ JWT ТОКЕН В COOKIE
                String token = authResponse.getToken();

                Cookie jwtCookie = new Cookie("jwt", token);
                jwtCookie.setHttpOnly(true);
                jwtCookie.setPath("/");
                jwtCookie.setSecure(false); // для localhost

                if (Boolean.TRUE.equals(authResponse.getRememberMe())) {
                    jwtCookie.setMaxAge(30 * 24 * 60 * 60); // 30 дней
                    log.info("🍪 JWT cookie установлен на 30 дней (Remember Me)");
                } else {
                    jwtCookie.setMaxAge(24 * 60 * 60); // 24 часа
                    log.info("🍪 JWT cookie установлен на 24 часа");
                }

                response.addCookie(jwtCookie);
                log.info("🍪 JWT cookie установлен (длина: {})", token.length());

                // ✅ ДОПОЛНИТЕЛЬНЫЕ COOKIES
                if (authResponse.getUser() != null) {
                    Cookie roleCookie = new Cookie("userRole", authResponse.getUser().getUserRole().toString());
                    roleCookie.setPath("/");
                    roleCookie.setMaxAge(jwtCookie.getMaxAge());
                    response.addCookie(roleCookie);

                    Cookie emailCookie = new Cookie("userEmail", authResponse.getUser().getEmail());
                    emailCookie.setPath("/");
                    emailCookie.setMaxAge(jwtCookie.getMaxAge());
                    response.addCookie(emailCookie);

                    log.info("🍪 Дополнительные cookies установлены");
                }

                // ✅ ВОЗВРАЩАЕМ JSON ОТВЕТ
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Успешная авторизация",
                        "token", authResponse.getToken(),
                        "type", authResponse.getType() != null ? authResponse.getType() : "Bearer",
                        "user", authResponse.getUser(),
                        "rememberMe", authResponse.getRememberMe() != null ? authResponse.getRememberMe() : false,
                        "redirectUrl", determineRedirectPath(authResponse.getUser())
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

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        log.debug("👤 Запрос информации о текущем пользователе");

        try {
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

    // ✅ HELPER METHODS
    private String determineRedirectPath(UserResponseDto user) {
        if (user != null && user.getUserRole() != null) {
            return "/dashboard/" + user.getUserRole().toString();
        }
        return "/dashboard";
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