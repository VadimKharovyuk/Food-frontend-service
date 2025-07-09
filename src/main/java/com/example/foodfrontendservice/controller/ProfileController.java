package com.example.foodfrontendservice.controller;

import com.example.foodfrontendservice.config.TokenExtractor;
import com.example.foodfrontendservice.dto.AUTSERVICE.UserTokenInfo;
import com.example.foodfrontendservice.dto.UserResponseDto;
import com.example.foodfrontendservice.service.UserIntegrationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final TokenExtractor tokenExtractor;
    private final UserIntegrationService userIntegrationService;

    @GetMapping
    public String profile(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        try {
            log.info("🏠 Запрос страницы профиля пользователя");

            // 1. ✅ Быстрая проверка авторизации через TokenExtractor
            UserTokenInfo tokenInfo = tokenExtractor.getCurrentUserInfo(request);

            if (tokenInfo == null) {
                log.warn("❌ Пользователь не авторизован - перенаправление на логин");
                redirectAttributes.addFlashAttribute("error", "Необходимо войти в систему");
                return "redirect:/login";
            }

            log.info("👤 Загрузка профиля для пользователя: {} (ID: {}, Role: {})",
                    tokenInfo.getEmail(), tokenInfo.getUserId(), tokenInfo.getRole());

            // 2. ✅ Получаем полные данные пользователя из User Service
            UserResponseDto fullUserData = null;
            try {
                fullUserData = userIntegrationService.getUserByToken(tokenInfo.getToken());
                log.info("✅ Получены полные данные пользователя из User Service");
            } catch (Exception e) {
                log.error("❌ Ошибка получения данных пользователя из User Service: {}", e.getMessage());
                // Используем данные из токена как fallback
                fullUserData = createFallbackUserData(tokenInfo);
                model.addAttribute("warning", "Не удалось загрузить полную информацию профиля");
            }

            // 3. ✅ Добавляем данные в модель для Thymeleaf

            // Базовые данные авторизации
            model.addAttribute("isAuthenticated", true);
            model.addAttribute("tokenInfo", tokenInfo);
            model.addAttribute("userName", getDisplayName(fullUserData));

            // Полные данные пользователя
            model.addAttribute("user", fullUserData);

            // Статистика пользователя (можно получать из других микросервисов)
            addUserStatistics(model, tokenInfo);

            // Настройки геолокации
            addLocationInfo(model, fullUserData);

            log.info("✅ Страница профиля успешно подготовлена для пользователя: {}", tokenInfo.getEmail());

            return "profile/main";

        } catch (Exception e) {
            log.error("💥 Неожиданная ошибка при загрузке профиля", e);
            model.addAttribute("error", "Произошла ошибка при загрузке профиля");
            return "error/500";
        }
    }

    /**
     * 📊 Добавление статистики пользователя
     * В будущем можно получать из разных микросервисов
     */
    private void addUserStatistics(Model model, UserTokenInfo tokenInfo) {
        try {
            // TODO: Получать реальную статистику из микросервисов
            // Пока используем заглушки

            model.addAttribute("orderCount", 15);      // Из Order Service
            model.addAttribute("favoriteCount", 8);    // Из Product Service (Favorites)
            model.addAttribute("reviewCount", 12);     // Из Review Service

            log.debug("📊 Добавлена статистика для пользователя {}", tokenInfo.getUserId());

        } catch (Exception e) {
            log.warn("⚠️ Ошибка получения статистики пользователя: {}", e.getMessage());

            // Fallback значения
            model.addAttribute("orderCount", 0);
            model.addAttribute("favoriteCount", 0);
            model.addAttribute("reviewCount", 0);
        }
    }

    /**
     * 📍 Добавление информации о геолокации
     */
    private void addLocationInfo(Model model, UserResponseDto userData) {
        try {
            if (userData != null) {
                model.addAttribute("hasLocation", userData.getHasLocation());
                model.addAttribute("locationStatus", userData.getLocationStatus());
                model.addAttribute("formattedCoordinates", userData.getFormattedCoordinates());
                model.addAttribute("shortAddress", userData.getShortAddress());

                if (userData.getLocationUpdatedAt() != null) {
                    model.addAttribute("locationLastUpdated", userData.getLocationUpdatedAt());
                }

                log.debug("📍 Добавлена информация о геолокации. Has location: {}", userData.getHasLocation());
            } else {
                model.addAttribute("hasLocation", false);
                model.addAttribute("locationStatus", "Не установлена");
            }

        } catch (Exception e) {
            log.warn("⚠️ Ошибка обработки геолокации: {}", e.getMessage());
            model.addAttribute("hasLocation", false);
            model.addAttribute("locationStatus", "Ошибка загрузки");
        }
    }

    /**
     * 👤 Получение отображаемого имени пользователя
     */
    private String getDisplayName(UserResponseDto userData) {
        if (userData == null) {
            return "Пользователь";
        }

        String firstName = userData.getFirstName();
        String lastName = userData.getLastName();

        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        } else {
            return userData.getEmail();
        }
    }

    /**
     * 🔄 Создание fallback данных пользователя из токена
     * Используется когда User Service недоступен
     */
    private UserResponseDto createFallbackUserData(UserTokenInfo tokenInfo) {
        log.warn("🔄 Создание fallback данных для пользователя {}", tokenInfo.getEmail());

        return UserResponseDto.builder()
                .id(tokenInfo.getUserId())
                .email(tokenInfo.getEmail())
                .firstName("Пользователь")
                .lastName("")
                .userRole(parseUserRole(tokenInfo.getRole()))
                .roleDisplayName(tokenInfo.getRoleDisplayName())
                .createdAt(LocalDateTime.now().minusMonths(1)) // Примерная дата
                .updatedAt(LocalDateTime.now())
                .hasLocation(false)
                .locationStatus("Не установлена")
                .build();
    }

    /**
     * 🔧 Парсинг роли пользователя из строки
     */
    private com.example.foodfrontendservice.enums.UserRole parseUserRole(String roleString) {
        try {
            if (roleString == null) {
                return com.example.foodfrontendservice.enums.UserRole.BASE_USER;
            }

            // Убираем префикс ROLE_ если есть
            String cleanRole = roleString.replace("ROLE_", "");

            return com.example.foodfrontendservice.enums.UserRole.valueOf(cleanRole);

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Неизвестная роль: {}. Используем BASE_USER", roleString);
            return com.example.foodfrontendservice.enums.UserRole.BASE_USER;
        }
    }
}