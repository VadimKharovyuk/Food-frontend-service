
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

            // Получаем доступные роли из User Service
            List<UserRole> availableRoles = userIntegrationService.getAvailableRoles();
            model.addAttribute("availableRoles", availableRoles);

            log.info("✅ Форма регистрации загружена с {} ролями", availableRoles.size());
            return "registration/register";

        } catch (Exception e) {
            log.error("❌ Ошибка загрузки формы регистрации: {}", e.getMessage(), e);
            model.addAttribute("error", "Ошибка загрузки формы регистрации");
            return "error/registration";
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
                List<UserRole> availableRoles = userIntegrationService.getAvailableRoles();
                model.addAttribute("availableRoles", availableRoles);
                model.addAttribute("error", "Пожалуйста, исправьте ошибки в форме");

                return "registration/register";
            }

            // Проверяем доступность email
            Boolean isEmailAvailable = userIntegrationService.checkEmailAvailability(registrationDto.getEmail());
            if (!Boolean.TRUE.equals(isEmailAvailable)) {
                log.warn("📧 Email {} уже занят", registrationDto.getEmail());

                // Перезагружаем роли для формы
                List<UserRole> availableRoles = userIntegrationService.getAvailableRoles();
                model.addAttribute("availableRoles", availableRoles);
                model.addAttribute("error", "Пользователь с таким email уже существует");

                return "registration/register";
            }

            // Регистрируем пользователя
            UserResponseDto registeredUser = userIntegrationService.registerUser(registrationDto);

            if (registeredUser != null) {
                log.info("✅ Пользователь {} успешно зарегистрирован с ID: {}",
                        registeredUser.getEmail(), registeredUser.getId());

                // Сохраняем информацию о успешной регистрации
                redirectAttributes.addFlashAttribute("success",
                        "Регистрация прошла успешно! Теперь вы можете войти в систему.");
                redirectAttributes.addFlashAttribute("registeredEmail", registeredUser.getEmail());

                // Перенаправляем на страницу входа
                return "redirect:/login?registered=true";
            } else {
                log.error("❌ Получен пустой ответ при регистрации пользователя: {}", registrationDto.getEmail());
                throw new RuntimeException("Пустой ответ от сервера регистрации");
            }

        } catch (Exception e) {
            log.error("💥 Ошибка регистрации пользователя {}: {}", registrationDto.getEmail(), e.getMessage(), e);

            // Перезагружаем роли для формы
            try {
                List<UserRole> availableRoles = userIntegrationService.getAvailableRoles();
                model.addAttribute("availableRoles", availableRoles);
            } catch (Exception roleException) {
                log.error("❌ Ошибка загрузки ролей: {}", roleException.getMessage());
            }

            model.addAttribute("error", "Ошибка регистрации: " + e.getMessage());
            return "registration/register";
        }
    }

    /**
     * ✅ AJAX проверка доступности email
     */
    @GetMapping("/check-email")
    @ResponseBody
    public ResponseEntity<Boolean> checkEmailAvailability(@RequestParam String email) {
        log.debug("📧 Проверка доступности email: {}", email);

        try {
            Boolean isAvailable = userIntegrationService.checkEmailAvailability(email);
            log.debug("✅ Email {} {}", email, Boolean.TRUE.equals(isAvailable) ? "доступен" : "занят");

            return ResponseEntity.ok(isAvailable);
        } catch (Exception e) {
            log.error("❌ Ошибка проверки email {}: {}", email, e.getMessage());
            return ResponseEntity.ok(false); // По умолчанию считаем недоступным при ошибке
        }
    }

    /**
     * 📋 AJAX получение доступных ролей
     */
    @GetMapping("/roles")
    @ResponseBody
    public ResponseEntity<List<UserRole>> getAvailableRoles() {
        log.debug("📋 AJAX запрос доступных ролей");

        try {
            List<UserRole> roles = userIntegrationService.getAvailableRoles();
            log.debug("✅ Возвращено {} ролей", roles.size());

            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            log.error("❌ Ошибка получения ролей: {}", e.getMessage());
            return ResponseEntity.ok(List.of()); // Возвращаем пустой список при ошибке
        }
    }

    /**
     * 🧪 Страница успешной регистрации
     */
    @GetMapping("/success")
    public String registrationSuccess(Model model,
                                      @RequestParam(required = false) String email) {
        log.info("🎉 Страница успешной регистрации для: {}", email);

        model.addAttribute("email", email);
        return "registration/success";
    }

    /**
     * ❌ Обработка ошибок регистрации
     */
    @ExceptionHandler(Exception.class)
    public String handleRegistrationError(Exception e, Model model, HttpServletRequest request) {
        log.error("💥 Необработанная ошибка в контроллере регистрации: {}", e.getMessage(), e);

        model.addAttribute("error", "Произошла ошибка во время регистрации");
        model.addAttribute("details", e.getMessage());
        model.addAttribute("path", request.getRequestURI());

        return "error/registration";
    }
}