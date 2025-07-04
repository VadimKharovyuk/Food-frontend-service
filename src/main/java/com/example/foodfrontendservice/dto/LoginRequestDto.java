package com.example.foodfrontendservice.dto;//package com.example.foodfrontendservice.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//
//import lombok.Data;
//
//@Data
//public class LoginRequestDto {
//    private String email;
//    private String password;
//}
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class LoginRequestDto {

    private String email;
    private String password;

    // ✅ УСТАНАВЛИВАЕМ DEFAULT ЗНАЧЕНИЕ
    @Builder.Default
    private Boolean rememberMe = false;

    // ✅ КОНСТРУКТОР БЕЗ rememberMe
    public LoginRequestDto(String email, String password) {
        this.email = email;
        this.password = password;
        this.rememberMe = false;
    }

    // ✅ УЛУЧШЕННЫЙ SETTER с null-safe логикой
    @JsonSetter("rememberMe")
    public void setRememberMe(Object rememberMe) {
        log.info("🐛 setRememberMe вызван с: {} (type: {})",
                rememberMe,
                rememberMe != null ? rememberMe.getClass().getSimpleName() : "null");

        if (rememberMe instanceof Boolean) {
            this.rememberMe = (Boolean) rememberMe;
        } else if (rememberMe instanceof String) {
            this.rememberMe = Boolean.parseBoolean((String) rememberMe);
        } else if (rememberMe == null) {
            this.rememberMe = false;
        } else {
            log.warn("🐛 Неожиданный тип для rememberMe: {}", rememberMe.getClass());
            this.rememberMe = false;
        }

        log.info("🐛 rememberMe установлен в: {}", this.rememberMe);
    }

    // ✅ АЛЬТЕРНАТИВНЫЙ SETTER для Boolean
    public void setRememberMe(Boolean rememberMe) {
        log.info("🐛 setRememberMe(Boolean) вызван с: {}", rememberMe);
        this.rememberMe = rememberMe != null ? rememberMe : false;
    }

    // ✅ NULL-SAFE GETTER
    public Boolean getRememberMe() {
        return this.rememberMe != null ? this.rememberMe : false;
    }

    // ✅ toString для отладки
    @Override
    public String toString() {
        return String.format("LoginRequestDto{email='%s', password='%s', rememberMe=%s}",
                email,
                password != null ? "[PROTECTED]" : "null",
                rememberMe);
    }
}