package com.example.foodfrontendservice.controller;

import com.example.foodfrontendservice.config.TokenExtractor;
import com.example.foodfrontendservice.dto.AUTSERVICE.UserTokenInfo;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.Product.ProductResponseDto;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.Product.ProductResponseWrapper;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.category.CategoryDto;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.category.ListApiResponse;
import com.example.foodfrontendservice.service.CategoryService;
import com.example.foodfrontendservice.service.ProductService;
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
@RequestMapping("/categories")
public class CategoryClientController {

    private final CategoryService categoryService;
    private final TokenExtractor tokenExtractor;
    private final ProductService productService ;

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
     * ✅ ОБНОВЛЕННЫЙ метод: Показать товары категории по ID
     */
    @GetMapping("/{categoryId}")
    public String getCategoryProducts(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String priceRange,
            HttpServletRequest request,
            Model model) {

        log.info("🗂️ Загрузка товаров категории с ID: {}", categoryId);

        try {
            // ✅ Получаем информацию о пользователе
            UserTokenInfo userInfo = tokenExtractor.getCurrentUserInfo(request);
            boolean isAuthenticated = (userInfo != null);
            addAuthDataToModel(model, userInfo, isAuthenticated);

            // ✅ Получаем информацию о категории
            CategoryDto category = getCategoryInfo(categoryId);
            if (category == null) {
                log.warn("❌ Категория с ID {} не найдена", categoryId);
                model.addAttribute("error", "Категория не найдена");
                return "category/category-not-found";
            }

            log.info("✅ Найдена категория: {} (ID: {})", category.getName(), categoryId);

            // ✅ Получаем продукты этой категории
            try {
                log.debug("🔍 Поиск продуктов: categoryId={}, page={}, size={}", categoryId, page, size);

                // Вызываем сервис для получения продуктов
                ProductResponseWrapper productsResponse = productService.getProductsByCategory(categoryId, page, size);

                if (productsResponse != null && productsResponse.isSuccessful() && productsResponse.hasProducts()) {
                    List<ProductResponseDto> products = productsResponse.getProducts();

                    log.info("✅ Найдено {} продуктов в категории '{}'", products.size(), category.getName());

                    // ✅ Добавляем данные продуктов в модель
                    model.addAttribute("products", products);
                    model.addAttribute("hasProducts", true);
                    model.addAttribute("totalCount", productsResponse.getTotalCount());

                    // ✅ Пагинация
                    model.addAttribute("hasNext", productsResponse.getHasNext());
                    model.addAttribute("hasPrevious", productsResponse.getHasPrevious());
                    model.addAttribute("nextPage", page + 1);
                    model.addAttribute("previousPage", page - 1);

                    // ✅ Статистика для фильтров
                    addProductStatistics(model, products);

                } else {
                    log.warn("⚠️ Продукты не найдены в категории {}: {}",
                            categoryId, productsResponse != null ? productsResponse.getMessage() : "Пустой ответ");

                    model.addAttribute("products", Collections.emptyList());
                    model.addAttribute("hasProducts", false);
                    model.addAttribute("totalCount", 0);
                    model.addAttribute("hasNext", false);
                    model.addAttribute("hasPrevious", false);

                    String message = "В категории пока нет товаров";
                    if (productsResponse != null && productsResponse.getMessage() != null) {
                        message = productsResponse.getMessage();
                    }
                    model.addAttribute("message", message);
                }

            } catch (Exception productException) {
                log.error("❌ Ошибка получения продуктов категории {}: {}", categoryId, productException.getMessage());

                // Показываем категорию, но без продуктов
                model.addAttribute("products", Collections.emptyList());
                model.addAttribute("hasProducts", false);
                model.addAttribute("totalCount", 0);
                model.addAttribute("hasNext", false);
                model.addAttribute("hasPrevious", false);
                model.addAttribute("message", "Временные проблемы с загрузкой товаров. Попробуйте позже.");
            }

            // ✅ Добавляем общие данные
            model.addAttribute("category", category);
            model.addAttribute("categoryFound", true);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("priceRange", priceRange);

            log.info("✅ Страница категории '{}' загружена", category.getName());

        } catch (Exception e) {
            log.error("💥 Исключение при загрузке категории {}", categoryId, e);
            model.addAttribute("categoryFound", false);
            model.addAttribute("error", "Произошла ошибка при загрузке категории");
        }

        return "category/category-products";
    }

    /**
     * ✅ Добавление статистики продуктов для фильтров
     */
    private void addProductStatistics(Model model, List<ProductResponseDto> products) {
        if (products == null || products.isEmpty()) return;

        try {
            // Диапазоны цен (используем finalPrice для учета скидок)
            Double minPrice = products.stream()
                    .filter(p -> p.getFinalPrice() != null)
                    .mapToDouble(p -> p.getFinalPrice().doubleValue())
                    .min().orElse(0.0);

            Double maxPrice = products.stream()
                    .filter(p -> p.getFinalPrice() != null)
                    .mapToDouble(p -> p.getFinalPrice().doubleValue())
                    .max().orElse(0.0);

            // Средняя цена
            Double avgPrice = products.stream()
                    .filter(p -> p.getFinalPrice() != null)
                    .mapToDouble(p -> p.getFinalPrice().doubleValue())
                    .average()
                    .orElse(0.0);

            // Количество уникальных магазинов
            long uniqueStores = products.stream()
                    .filter(p -> p.getStoreId() != null)
                    .mapToLong(ProductResponseDto::getStoreId)
                    .distinct()
                    .count();

            // Количество товаров со скидкой
            long discountedProducts = products.stream()
                    .filter(p -> p.getHasDiscount() != null && p.getHasDiscount())
                    .count();

            // Количество популярных товаров
            long popularProducts = products.stream()
                    .filter(p -> p.getIsPopular() != null && p.getIsPopular())
                    .count();

            model.addAttribute("minPrice", Math.round(minPrice * 100.0) / 100.0);
            model.addAttribute("maxPrice", Math.round(maxPrice * 100.0) / 100.0);
            model.addAttribute("averagePrice", Math.round(avgPrice * 100.0) / 100.0);
            model.addAttribute("uniqueStoresCount", uniqueStores);
            model.addAttribute("discountedProductsCount", discountedProducts);
            model.addAttribute("popularProductsCount", popularProducts);

            log.debug("📊 Статистика продуктов: цена от {} до {}, средняя {}, магазинов {}, со скидкой {}, популярных {}",
                    minPrice, maxPrice, avgPrice, uniqueStores, discountedProducts, popularProducts);

        } catch (Exception e) {
            log.warn("⚠️ Ошибка вычисления статистики продуктов: {}", e.getMessage());
        }
    }

    /**
     * ✅ Вспомогательный метод получения информации о категории
     */
    private CategoryDto getCategoryInfo(Long categoryId) {
        try {
            ListApiResponse<CategoryDto> categoriesResponse = categoryService.getActiveCategoriesBrief();

            if (categoriesResponse != null && categoriesResponse.getSuccess() && categoriesResponse.getData() != null) {
                return categoriesResponse.getData().stream()
                        .filter(c -> c.getId().equals(categoryId))
                        .findFirst()
                        .orElse(null);
            }
        } catch (Exception e) {
            log.error("Ошибка получения информации о категории {}", categoryId, e);
        }
        return null;
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