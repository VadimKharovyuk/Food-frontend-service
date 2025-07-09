package com.example.foodfrontendservice.Client;//package com.example.foodfrontendservice.Client;
import com.example.foodfrontendservice.Client.Fallback.UserServiceClientFallback;
import com.example.foodfrontendservice.config.FeignConfig;
import com.example.foodfrontendservice.dto.*;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.category.ApiResponse;
import com.example.foodfrontendservice.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@FeignClient(
        name = "user-service",
        url = "http://localhost:8081",
        path = "/api",
        fallback = UserServiceClientFallback.class,
        configuration = FeignConfig.class,
        contextId = "userServiceClient"
)
public interface UserServiceClient {

    @PostMapping("/auth/login")
    AuthResponseDto login(@RequestBody LoginRequestDto loginRequest);

    @GetMapping("/auth/user-by-token")
    UserResponseDto getUserByToken(@RequestHeader("Authorization") String authHeader);

    @PostMapping("/auth/validate-token-simple")
    Boolean validateTokenSimple(@RequestHeader("Authorization") String authHeader);

    @GetMapping("/auth/me")
    ResponseEntity<ApiResponse<UserResponseDto>> getCurrentUser(@RequestHeader("Authorization") String authHeader);

    @PostMapping("/auth/validate-token")
    ResponseEntity<ApiResponse<TokenValidationDto>> validateToken(@RequestHeader("Authorization") String authHeader);

    @PostMapping("/auth/logout")
    ResponseEntity<ApiResponse<LogoutResponseDto>> logout(@RequestHeader("Authorization") String authHeader);

    @GetMapping("/auth/test")
    ResponseEntity<ApiResponse<ServiceHealthDto>> testAuthService();

    @GetMapping("/registration/roles")
    ResponseEntity<List<UserRole>> getAvailableRoles();

    @PostMapping("/registration/register")
    ResponseEntity<UserResponseDto> register(@RequestBody UserRegistrationDto registrationDto);

    @GetMapping("/registration/check-email")
    ResponseEntity<Boolean> checkEmailAvailability(@RequestParam String email);

    @GetMapping("/registration/test")
    ResponseEntity<String> testRegistrationService();


    @GetMapping("/auth/user-by-token-response")
    ResponseEntity<UserResponseDto> getUserByTokenWithResponse(@RequestHeader("Authorization") String authHeader);

    // ========== DTO КЛАССЫ ДЛЯ КЛИЕНТСКИХ ENDPOINTS ==========

    /**
     * DTO для детальной валидации токена
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class TokenValidationDto {
        private Boolean valid;
        private Long userId;
        private String email;
        private String role;
        private Boolean hasLocation;
        private String locationStatus;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class LogoutResponseDto {
        private Boolean success;
        private String userEmail;
        private String message;
        private java.time.LocalDateTime logoutTime;
    }

    /**
     * DTO для health check
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class ServiceHealthDto {
        private String serviceName;
        private String status;
        private String message;
        private String version;
        private java.time.LocalDateTime timestamp;
    }
}