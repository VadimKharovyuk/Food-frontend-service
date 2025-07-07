//
//package com.example.foodfrontendservice.controller;
//
//import com.example.foodfrontendservice.dto.UserRegistrationDto;
//import com.example.foodfrontendservice.dto.UserResponseDto;
//import com.example.foodfrontendservice.enums.UserRole;
//import com.example.foodfrontendservice.service.UserIntegrationService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.validation.Valid;
//import java.util.List;
//
//@Controller
//@RequestMapping("/register")
//@RequiredArgsConstructor
//@Slf4j
//public class RegistrationController {
//
//    private final UserIntegrationService userIntegrationService;
//
//    /**
//     * 📋 Показать форму регистрации
//     */
//    @GetMapping
//    public String showRegistrationForm(Model model) {
//        log.info("📋 Запрос формы регистрации");
//
//        try {
//            // Создаем пустой объект для формы
//            model.addAttribute("registrationDto", new UserRegistrationDto());
//
//            // Получаем доступные роли из User Service
//            List<UserRole> availableRoles = userIntegrationService.getAvailableRoles();
//            model.addAttribute("availableRoles", availableRoles);
//
//            log.info("✅ Форма регистрации загружена с {} ролями", availableRoles.size());
//            return "registration/register";
//
//        } catch (Exception e) {
//            log.error("❌ Ошибка загрузки формы регистрации: {}", e.getMessage(), e);
//            model.addAttribute("error", "Ошибка загрузки формы регистрации");
//            return "error/registration";
//        }
//    }
//
//    /**
//     * 📝 Обработка регистрации пользователя
//     */
//    @PostMapping
//    public String registerUser(@Valid @ModelAttribute UserRegistrationDto registrationDto,
//                               BindingResult bindingResult,
//                               Model model,
//                               RedirectAttributes redirectAttributes) {
//
//        log.info("📝 Попытка регистрации пользователя: {}", registrationDto.getEmail());
//
//        try {
//            // Проверяем ошибки валидации
//            if (bindingResult.hasErrors()) {
//                log.warn("❌ Ошибки валидации при регистрации: {}", bindingResult.getAllErrors());
//
//                // Перезагружаем роли для формы
//                List<UserRole> availableRoles = userIntegrationService.getAvailableRoles();
//                model.addAttribute("availableRoles", availableRoles);
//                model.addAttribute("error", "Пожалуйста, исправьте ошибки в форме");
//
//                return "registration/register";
//            }
//
//            // Проверяем доступность email
//            Boolean isEmailAvailable = userIntegrationService.checkEmailAvailability(registrationDto.getEmail());
//            if (!Boolean.TRUE.equals(isEmailAvailable)) {
//                log.warn("📧 Email {} уже занят", registrationDto.getEmail());
//
//                // Перезагружаем роли для формы
//                List<UserRole> availableRoles = userIntegrationService.getAvailableRoles();
//                model.addAttribute("availableRoles", availableRoles);
//                model.addAttribute("error", "Пользователь с таким email уже существует");
//
//                return "registration/register";
//            }
//
//            // Регистрируем пользователя
//            UserResponseDto registeredUser = userIntegrationService.registerUser(registrationDto);
//
//            if (registeredUser != null) {
//                log.info("✅ Пользователь {} успешно зарегистрирован с ID: {}",
//                        registeredUser.getEmail(), registeredUser.getId());
//
//                // Сохраняем информацию о успешной регистрации
//                redirectAttributes.addFlashAttribute("success",
//                        "Регистрация прошла успешно! Теперь вы можете войти в систему.");
//                redirectAttributes.addFlashAttribute("registeredEmail", registeredUser.getEmail());
//
//                // Перенаправляем на страницу входа
//                return "redirect:/login?registered=true";
//            } else {
//                log.error("❌ Получен пустой ответ при регистрации пользователя: {}", registrationDto.getEmail());
//                throw new RuntimeException("Пустой ответ от сервера регистрации");
//            }
//
//        } catch (Exception e) {
//            log.error("💥 Ошибка регистрации пользователя {}: {}", registrationDto.getEmail(), e.getMessage(), e);
//
//            // Перезагружаем роли для формы
//            try {
//                List<UserRole> availableRoles = userIntegrationService.getAvailableRoles();
//                model.addAttribute("availableRoles", availableRoles);
//            } catch (Exception roleException) {
//                log.error("❌ Ошибка загрузки ролей: {}", roleException.getMessage());
//            }
//
//            model.addAttribute("error", "Ошибка регистрации: " + e.getMessage());
//            return "registration/register";
//        }
//    }
//
//    /**
//     * ✅ AJAX проверка доступности email
//     */
//    @GetMapping("/check-email")
//    @ResponseBody
//    public ResponseEntity<Boolean> checkEmailAvailability(@RequestParam String email) {
//        log.debug("📧 Проверка доступности email: {}", email);
//
//        try {
//            Boolean isAvailable = userIntegrationService.checkEmailAvailability(email);
//            log.debug("✅ Email {} {}", email, Boolean.TRUE.equals(isAvailable) ? "доступен" : "занят");
//
//            return ResponseEntity.ok(isAvailable);
//        } catch (Exception e) {
//            log.error("❌ Ошибка проверки email {}: {}", email, e.getMessage());
//            return ResponseEntity.ok(false); // По умолчанию считаем недоступным при ошибке
//        }
//    }
//
//    /**
//     * 📋 AJAX получение доступных ролей
//     */
//    @GetMapping("/roles")
//    @ResponseBody
//    public ResponseEntity<List<UserRole>> getAvailableRoles() {
//        log.debug("📋 AJAX запрос доступных ролей");
//
//        try {
//            List<UserRole> roles = userIntegrationService.getAvailableRoles();
//            log.debug("✅ Возвращено {} ролей", roles.size());
//
//            return ResponseEntity.ok(roles);
//        } catch (Exception e) {
//            log.error("❌ Ошибка получения ролей: {}", e.getMessage());
//            return ResponseEntity.ok(List.of()); // Возвращаем пустой список при ошибке
//        }
//    }
//
//    /**
//     * 🧪 Страница успешной регистрации
//     */
//    @GetMapping("/success")
//    public String registrationSuccess(Model model,
//                                      @RequestParam(required = false) String email) {
//        log.info("🎉 Страница успешной регистрации для: {}", email);
//
//        model.addAttribute("email", email);
//        return "registration/success";
//    }
//
//    /**
//     * ❌ Обработка ошибок регистрации
//     */
//    @ExceptionHandler(Exception.class)
//    public String handleRegistrationError(Exception e, Model model, HttpServletRequest request) {
//        log.error("💥 Необработанная ошибка в контроллере регистрации: {}", e.getMessage(), e);
//
//        model.addAttribute("error", "Произошла ошибка во время регистрации");
//        model.addAttribute("details", e.getMessage());
//        model.addAttribute("path", request.getRequestURI());
//
//        return "error/registration";
//    }
//}

package com.example.foodfrontendservice.controller;

import com.example.foodfrontendservice.dto.UserRegistrationDto;
import com.example.foodfrontendservice.dto.UserResponseDto;
import com.example.foodfrontendservice.enums.UserRole;
import com.example.foodfrontendservice.service.UserIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 🌐 RegistrationController - ОБНОВЛЕН для новой архитектуры
 * Контроллер для Thymeleaf форм регистрации
 */
@Controller
@RequestMapping("/register")
@RequiredArgsConstructor
@Slf4j
public class RegistrationController {

    private final UserIntegrationService userIntegrationService;

    /**
     * 📋 Показать форму регистрации
     */
    @GetMapping
    public String showRegistrationForm(Model model) {
        log.info("📋 Запрос формы регистрации");

        try {
            // Создаем пустой объект для формы
            model.addAttribute("registrationDto", new UserRegistrationDto());

            // ✅ Получаем доступные роли через обновленный метод
            ResponseEntity<List<UserRole>> rolesResponse = userIntegrationService.getAvailableRoles();
            List<UserRole> availableRoles = rolesResponse.getBody();

            if (availableRoles != null) {
                model.addAttribute("availableRoles", availableRoles);
                log.info("✅ Форма регистрации загружена с {} ролями", availableRoles.size());
            } else {
                // Fallback роли если сервис недоступен
                List<UserRole> fallbackRoles = List.of(UserRole.BASE_USER, UserRole.BUSINESS_USER);
                model.addAttribute("availableRoles", fallbackRoles);
                log.warn("⚠️ Используются fallback роли, сервис недоступен");
            }

            return "registration/register";

        } catch (Exception e) {
            log.error("❌ Ошибка загрузки формы регистрации: {}", e.getMessage(), e);

            // Добавляем fallback роли при ошибке
            model.addAttribute("availableRoles", List.of(UserRole.BASE_USER));
            model.addAttribute("error", "Ошибка загрузки формы регистрации. Некоторые функции могут быть недоступны.");

            return "registration/register";
        }
    }

    /**
     * 📝 Обработка регистрации пользователя
     */
    @PostMapping
    public String registerUser(@Valid @ModelAttribute UserRegistrationDto registrationDto,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        log.info("📝 Попытка регистрации пользователя: {}", registrationDto.getEmail());

        try {
            // Проверяем ошибки валидации
            if (bindingResult.hasErrors()) {
                log.warn("❌ Ошибки валидации при регистрации: {}", bindingResult.getAllErrors());

                // Перезагружаем роли для формы
                loadRolesForModel(model);
                model.addAttribute("error", "Пожалуйста, исправьте ошибки в форме");

                return "registration/register";
            }

            // ✅ Проверяем доступность email через обновленный метод
            ResponseEntity<Boolean> emailCheckResponse = userIntegrationService.checkEmailAvailability(registrationDto.getEmail());
            Boolean isEmailAvailable = emailCheckResponse.getBody();

            if (!Boolean.TRUE.equals(isEmailAvailable)) {
                log.warn("📧 Email {} уже занят", registrationDto.getEmail());

                // Перезагружаем роли для формы
                loadRolesForModel(model);
                model.addAttribute("error", "Пользователь с таким email уже существует");

                return "registration/register";
            }

            // ✅ Регистрируем пользователя через обновленный метод
            ResponseEntity<UserResponseDto> registerResponse = userIntegrationService.registerUser(registrationDto);
            UserResponseDto registeredUser = registerResponse.getBody();

            if (registeredUser != null) {
                log.info("✅ Пользователь {} успешно зарегистрирован с ID: {}",
                        registeredUser.getEmail(), registeredUser.getId());

                // Сохраняем информацию о успешной регистрации
                redirectAttributes.addFlashAttribute("success",
                        "Регистрация прошла успешно! Теперь вы можете войти в систему.");
                redirectAttributes.addFlashAttribute("registeredEmail", registeredUser.getEmail());
                redirectAttributes.addFlashAttribute("userRole", registeredUser.getRoleDisplayName());

                // Определяем куда перенаправить в зависимости от роли
                String redirectUrl = determineRedirectAfterRegistration(registeredUser.getUserRole());

                return "redirect:" + redirectUrl;
            } else {
                log.error("❌ Получен пустой ответ при регистрации пользователя: {}", registrationDto.getEmail());
                throw new RuntimeException("Пустой ответ от сервера регистрации");
            }

        } catch (Exception e) {
            log.error("💥 Ошибка регистрации пользователя {}: {}", registrationDto.getEmail(), e.getMessage(), e);

            // Перезагружаем роли для формы
            loadRolesForModel(model);

            // Определяем тип ошибки для более точного сообщения
            String errorMessage = determineErrorMessage(e);
            model.addAttribute("error", errorMessage);

            return "registration/register";
        }
    }

    /**
     * ✅ AJAX проверка доступности email
     */
    @GetMapping("/check-email")
    @ResponseBody
    public ResponseEntity<Boolean> checkEmailAvailability(@RequestParam String email) {
        log.debug("📧 AJAX: Проверка доступности email: {}", email);

        try {
            // ✅ Используем обновленный метод
            ResponseEntity<Boolean> response = userIntegrationService.checkEmailAvailability(email);
            Boolean isAvailable = response.getBody();

            log.debug("✅ AJAX: Email {} {}", email, Boolean.TRUE.equals(isAvailable) ? "доступен" : "занят");

            return ResponseEntity.ok(Boolean.TRUE.equals(isAvailable));

        } catch (Exception e) {
            log.error("❌ AJAX: Ошибка проверки email {}: {}", email, e.getMessage());
            return ResponseEntity.ok(false); // По умолчанию считаем недоступным при ошибке
        }
    }

    /**
     * 📋 AJAX получение доступных ролей
     */
    @GetMapping("/roles")
    @ResponseBody
    public ResponseEntity<List<UserRole>> getAvailableRoles() {
        log.debug("📋 AJAX: Запрос доступных ролей");

        try {
            // ✅ Используем обновленный метод
            ResponseEntity<List<UserRole>> response = userIntegrationService.getAvailableRoles();
            List<UserRole> roles = response.getBody();

            if (roles != null) {
                log.debug("✅ AJAX: Возвращено {} ролей", roles.size());
                return ResponseEntity.ok(roles);
            } else {
                log.warn("⚠️ AJAX: Пустой список ролей, возвращаем fallback");
                return ResponseEntity.ok(List.of(UserRole.BASE_USER));
            }

        } catch (Exception e) {
            log.error("❌ AJAX: Ошибка получения ролей: {}", e.getMessage());
            return ResponseEntity.ok(List.of(UserRole.BASE_USER)); // Возвращаем fallback при ошибке
        }
    }

    /**
     * 🎉 Страница успешной регистрации
     */
    @GetMapping("/success")
    public String registrationSuccess(Model model,
                                      @RequestParam(required = false) String email,
                                      @RequestParam(required = false) String role) {
        log.info("🎉 Страница успешной регистрации для: {} ({})", email, role);

        model.addAttribute("email", email);
        model.addAttribute("role", role);
        model.addAttribute("loginUrl", "/login");

        return "registration/success";
    }

    /**
     * 🧪 Тест регистрационного сервиса
     */
    @GetMapping("/test")
    @ResponseBody
    public ResponseEntity<String> testRegistrationService() {
        log.info("🧪 Тест регистрационного сервиса");

        try {
            // ✅ Используем обновленный метод
            ResponseEntity<String> testResponse = userIntegrationService.testRegistrationService();
            String result = testResponse.getBody();

            // Также тестируем получение ролей
            ResponseEntity<List<UserRole>> rolesResponse = userIntegrationService.getAvailableRoles();
            List<UserRole> roles = rolesResponse.getBody();

            String message = String.format("Registration Service OK. Test: %s, Roles: %d",
                    result != null ? result : "OK",
                    roles != null ? roles.size() : 0);

            return ResponseEntity.ok(message);

        } catch (Exception e) {
            log.error("❌ Ошибка тестирования: {}", e.getMessage());
            return ResponseEntity.status(500).body("Registration Service ERROR: " + e.getMessage());
        }
    }

    // ========== UTILITY МЕТОДЫ ==========

    /**
     * 🔧 Загрузка ролей для модели с fallback
     */
    private void loadRolesForModel(Model model) {
        try {
            ResponseEntity<List<UserRole>> response = userIntegrationService.getAvailableRoles();
            List<UserRole> roles = response.getBody();

            if (roles != null) {
                model.addAttribute("availableRoles", roles);
            } else {
                model.addAttribute("availableRoles", List.of(UserRole.BASE_USER));
            }
        } catch (Exception e) {
            log.error("❌ Ошибка загрузки ролей для модели: {}", e.getMessage());
            model.addAttribute("availableRoles", List.of(UserRole.BASE_USER));
        }
    }

    /**
     * 🎯 Определение редиректа после регистрации
     */
    private String determineRedirectAfterRegistration(UserRole userRole) {
        // Всегда перенаправляем на страницу входа с параметром
        return "/login?registered=true";

        // Альтернативно можно сразу перенаправлять в dashboard:
        /*
        return switch (userRole) {
            case ADMIN -> "/admin/dashboard?newUser=true";
            case BUSINESS_USER -> "/business/dashboard?newUser=true";
            case COURIER -> "/courier/dashboard?newUser=true";
            case BASE_USER -> "/user/dashboard?newUser=true";
            default -> "/login?registered=true";
        };
        */
    }

    /**
     * 💬 Определение сообщения об ошибке
     */
    private String determineErrorMessage(Exception e) {
        String message = e.getMessage().toLowerCase();

        if (message.contains("email") && message.contains("exists")) {
            return "Пользователь с таким email уже существует";
        } else if (message.contains("password")) {
            return "Некорректный пароль. Пароль должен содержать минимум 6 символов";
        } else if (message.contains("validation")) {
            return "Ошибка валидации данных. Проверьте правильность заполнения полей";
        } else if (message.contains("service") && message.contains("unavailable")) {
            return "Сервис регистрации временно недоступен. Попробуйте позже";
        } else {
            return "Ошибка регистрации: " + e.getMessage();
        }
    }

    // ========== ОБРАБОТЧИКИ ОШИБОК ==========

    /**
     * ❌ Обработка ошибок регистрации
     */
    @ExceptionHandler(Exception.class)
    public String handleRegistrationError(Exception e, Model model, HttpServletRequest request) {
        log.error("💥 Необработанная ошибка в контроллере регистрации: {}", e.getMessage(), e);

        model.addAttribute("error", "Произошла ошибка во время регистрации");
        model.addAttribute("details", e.getMessage());
        model.addAttribute("path", request.getRequestURI());
        model.addAttribute("timestamp", java.time.LocalDateTime.now());

        // Пытаемся загрузить роли для формы
        loadRolesForModel(model);

        return "error/registration";
    }
}