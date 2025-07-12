package com.example.foodfrontendservice.controller;

import com.example.foodfrontendservice.config.TokenExtractor;
import com.example.foodfrontendservice.dto.AUTSERVICE.UserTokenInfo;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.category.CategoryDto;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.category.ListApiResponse;
import com.example.foodfrontendservice.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/categories") // ✅ ИСПРАВЛЕНИЕ: изменил на множественное число для консистентности
public class CategoryClientController {

    private final CategoryService categoryService;
    private final TokenExtractor tokenExtractor;

    /**
     * ✅ ОСНОВНОЙ метод: Показать все активные категории
     */
    @GetMapping
    public String getAllActiveCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request,
            Model model) {

        log.info("🗂️ Загрузка страницы категорий: page={}, size={}", page, size);

        try {
            // ✅ Получаем информацию о пользователе (опционально)
            UserTokenInfo userInfo = tokenExtractor.getCurrentUserInfo(request);
            boolean isAuthenticated = tokenExtractor.isAuthenticated(request);

            // ✅ Добавляем данные авторизации в модель
            addAuthDataToModel(model, userInfo, isAuthenticated);

            // ✅ Получаем список активных категорий
            ListApiResponse<CategoryDto> categoriesResponse = categoryService.getActiveCategoriesBrief();

            if (categoriesResponse != null && categoriesResponse.getSuccess() && categoriesResponse.getData() != null) {
                List<CategoryDto> activeCategories = categoriesResponse.getData();

                // ✅ Сортируем категории по sortOrder, затем по имени
                activeCategories = activeCategories.stream()
                        .sorted((c1, c2) -> {
                            // Сортируем по sortOrder, затем по имени
                            if (c1.getSortOrder() != null && c2.getSortOrder() != null) {
                                return c1.getSortOrder().compareTo(c2.getSortOrder());
                            }
                            if (c1.getSortOrder() != null) return -1;
                            if (c2.getSortOrder() != null) return 1;
                            return c1.getName().compareTo(c2.getName());
                        })
                        .toList();

                log.info("✅ Загружено {} активных категорий", activeCategories.size());

                // ✅ Добавляем данные в модель
                model.addAttribute("categories", activeCategories);
                model.addAttribute("totalCount", activeCategories.size());
                model.addAttribute("hasCategories", !activeCategories.isEmpty());
                model.addAttribute("currentPage", page);
                model.addAttribute("pageSize", size);

                // ✅ Дополнительная статистика
                long categoriesWithImages = activeCategories.stream()
                        .filter(category -> category.getImageUrl() != null && !category.getImageUrl().trim().isEmpty())
                        .count();
                model.addAttribute("categoriesWithImages", categoriesWithImages);

            } else {
                log.error("❌ Ошибка получения категорий: {}",
                        categoriesResponse != null ? categoriesResponse.getMessage() : "Неизвестная ошибка");

                model.addAttribute("categories", Collections.emptyList());
                model.addAttribute("totalCount", 0);
                model.addAttribute("hasCategories", false);
                model.addAttribute("error", "Ошибка загрузки категорий");
            }

        } catch (Exception e) {
            log.error("💥 Исключение при загрузке категорий", e);
            model.addAttribute("categories", Collections.emptyList());
            model.addAttribute("totalCount", 0);
            model.addAttribute("hasCategories", false);
            model.addAttribute("error", "Произошла ошибка при загрузке категорий");
        }

        return "category/categories-list";
    }

    /**
     * ✅ ДОПОЛНИТЕЛЬНЫЙ метод: Показать категорию по ID (опционально)
     */
    @GetMapping("/{categoryId}")
    public String getCategoryById(
            @PathVariable Long categoryId,
            HttpServletRequest request,
            Model model) {

        log.info("🗂️ Загрузка категории с ID: {}", categoryId);

        try {
            // ✅ Получаем информацию о пользователе
            UserTokenInfo userInfo = tokenExtractor.getCurrentUserInfo(request);
            boolean isAuthenticated = tokenExtractor.isAuthenticated(request);
            addAuthDataToModel(model, userInfo, isAuthenticated);

            // ✅ Получаем все категории для поиска нужной (используем активные)
            ListApiResponse<CategoryDto> categoriesResponse = categoryService.getActiveCategoriesBrief();

            if (categoriesResponse != null && categoriesResponse.getSuccess() && categoriesResponse.getData() != null) {
                CategoryDto category = categoriesResponse.getData().stream()
                        .filter(c -> c.getId().equals(categoryId))
                        .findFirst()
                        .orElse(null);

                if (category != null) {
                    log.info("✅ Найдена категория: {} (ID: {})", category.getName(), categoryId);
                    model.addAttribute("category", category);
                    model.addAttribute("categoryFound", true);
                } else {
                    log.warn("❌ Категория с ID {} не найдена или неактивна", categoryId);
                    model.addAttribute("categoryFound", false);
                    model.addAttribute("error", "Категория не найдена");
                }
            } else {
                log.error("❌ Ошибка получения категорий при поиске ID {}", categoryId);
                model.addAttribute("categoryFound", false);
                model.addAttribute("error", "Ошибка загрузки категории");
            }

        } catch (Exception e) {
            log.error("💥 Исключение при загрузке категории с ID {}", categoryId, e);
            model.addAttribute("categoryFound", false);
            model.addAttribute("error", "Произошла ошибка при загрузке категории");
        }

        return "category/category-detail"; // ✅ Отдельный шаблон для детальной страницы
    }

    /**
     * ✅ ВСПОМОГАТЕЛЬНЫЙ метод: Добавление данных авторизации в модель
     */
    private void addAuthDataToModel(Model model, UserTokenInfo userInfo, boolean isAuthenticated) {
        if (isAuthenticated && userInfo != null) {
            model.addAttribute("isAuthenticated", true);
            model.addAttribute("userEmail", userInfo.getEmail());
            model.addAttribute("userId", userInfo.getUserId());
            model.addAttribute("userRole", userInfo.getRole());
            model.addAttribute("jwtToken", userInfo.getToken());
            model.addAttribute("authToken", userInfo.getToken());
            model.addAttribute("authHeader", "Bearer " + userInfo.getToken());

            log.debug("👤 Пользователь авторизован: {} (ID: {})", userInfo.getEmail(), userInfo.getUserId());
        } else {
            model.addAttribute("isAuthenticated", false);
            model.addAttribute("userEmail", "");
            model.addAttribute("userId", null);
            model.addAttribute("userRole", "");
            model.addAttribute("jwtToken", "");
            model.addAttribute("authToken", "");
            model.addAttribute("authHeader", "");

            log.debug("👤 Пользователь не авторизован");
        }
    }
}