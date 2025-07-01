package com.example.foodfrontendservice.Client;
import com.example.foodfrontendservice.Client.Fallback.UserServiceClientFallback;
import com.example.foodfrontendservice.config.FeignConfig;
import com.example.foodfrontendservice.dto.AuthResponseDto;
import com.example.foodfrontendservice.dto.LoginRequestDto;
import com.example.foodfrontendservice.dto.UserRegistrationDto;
import com.example.foodfrontendservice.dto.UserResponseDto;
import com.example.foodfrontendservice.enums.UserRole;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@FeignClient(
        name = "user-service",
        fallback = UserServiceClientFallback.class,
        configuration = FeignConfig.class
)
public interface UserServiceClient {

    // 📋 Получить доступные роли
    @GetMapping("/api/registration/roles")
    ResponseEntity<List<UserRole>> getAvailableRoles();

    // 📝 Регистрация пользователя
    @PostMapping("/api/registration/register")
    ResponseEntity<UserResponseDto> register(@RequestBody UserRegistrationDto registrationDto);

    // ✅ Проверка доступности email
    @GetMapping("/api/registration/check-email")
    ResponseEntity<Boolean> checkEmailAvailability(@RequestParam String email);

    // 🔐 Авторизация пользователя
    @PostMapping("/api/auth/login")
    ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequestDto loginRequest);

    // 👤 Получить профиль текущего пользователя
    @GetMapping("/api/auth/me")
    ResponseEntity<UserResponseDto> getCurrentUser(@RequestHeader("Authorization") String authHeader);

    // ✅ Валидация токена
    @PostMapping("/api/auth/validate-token")
    ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String authHeader);

    // 🧪 Тестовые endpoints
    @GetMapping("/api/auth/test")
    ResponseEntity<String> testAuthService();

    @GetMapping("/api/registration/test")
    ResponseEntity<String> testRegistrationService();
}