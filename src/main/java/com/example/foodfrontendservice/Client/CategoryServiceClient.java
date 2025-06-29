package com.example.foodfrontendservice.Client;

import com.example.foodfrontendservice.Client.Fallback.CategoryServiceClientFallback;
import com.example.foodfrontendservice.config.FeignConfig;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.category.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// CategoryServiceClient
@FeignClient(
        name = "product-service",
        path = "/api/categories",
        fallback = CategoryServiceClientFallback.class,
        contextId = "categoryServiceClient"
)
public interface CategoryServiceClient {


    /**
     * 📊 Получить краткий список активных категорий (для dropdown/селекторов)
     */
    @GetMapping("/brief")
    ResponseEntity<ListApiResponse<CategoryDto>> getActiveCategoriesBrief();

    /**
     * 📋 Получить все категории включая неактивные (полная информация)
     * Требует: роль ADMIN
     */
    @GetMapping("/all")
    ResponseEntity<ListApiResponse<CategoryResponseDto>> getAllCategories();

    /**
     * 📋 Получить все активные категории (полная информация)
     */
    @GetMapping
    ResponseEntity<ListApiResponse<CategoryResponseDto>> getAllActiveCategories();


    /**
     * 🔍 Поиск категорий по названию (полная информация)
     */
    @GetMapping("/search")
    ResponseEntity<ListApiResponse<CategoryResponseDto>> searchCategories(@RequestParam String name);

    // ================================
    // 📊 ПОЛУЧЕНИЕ СПИСКОВ - КРАТКАЯ ИНФОРМАЦИЯ (ПРОЕКЦИИ)
    // ================================


    /**
     * 📊 Получить краткий список всех категорий (включая неактивные)
     * Требует: роль ADMIN
     */
    @GetMapping("/brief/all")
    ResponseEntity<ListApiResponse<CategoryDto>> getAllCategoriesBrief();

    /**
     * 🔍 Поиск кратких данных категорий по названию
     */
    @GetMapping("/brief/search")
    ResponseEntity<ListApiResponse<CategoryDto>> searchCategoriesBrief(@RequestParam String name);

    /**
     * 📊 Получить краткие данные категорий по списку ID
     */
    @PostMapping("/brief/by-ids")
    ResponseEntity<ListApiResponse<CategoryDto>> getCategoriesBriefByIds(@RequestBody List<Long> ids);

    // ================================
    // 🔍 ПОЛУЧЕНИЕ ОТДЕЛЬНЫХ КАТЕГОРИЙ
    // ================================

    /**
     * 🔍 Получить категорию по ID (полная информация)
     */
    @GetMapping("/{id}")
    ResponseEntity<ApiResponse<CategoryResponseDto>> getCategoryById(@PathVariable Long id);

    /**
     * 📊 Получить краткую информацию о категории по ID
     */
    @GetMapping("/{id}/brief")
    ResponseEntity<ApiResponse<CategoryDto>> getCategoryBrief(@PathVariable Long id);

    // ================================
    // ✏️ СОЗДАНИЕ И ОБНОВЛЕНИЕ КАТЕГОРИЙ (ADMIN ONLY)
    // ================================

    /**
     * ➕ Создать новую категорию (с возможностью загрузки изображения)
     * Требует: роль ADMIN
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ApiResponse<CategoryResponseDto>> createCategory(
            @RequestHeader("X-User-Id") Long userId,
            @ModelAttribute CreateCategoryDto createCategoryDto
    );

    /**
     * ✏️ Обновить существующую категорию
     * Требует: роль ADMIN
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ApiResponse<CategoryResponseDto>> updateCategory(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @ModelAttribute CreateCategoryDto updateCategoryDto
    );

    /**
     * 🗑️ Удалить категорию (мягкое удаление - деактивация)
     * Требует: роль ADMIN
     */
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId
    );

    /**
     * 🔄 Переключить статус категории (активна/неактивна)
     * Требует: роль ADMIN
     */
    @PatchMapping("/{id}/toggle")
    ResponseEntity<ApiResponse<CategoryResponseDto>> toggleCategoryStatus(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId
    );


    // ================================
    // 🛠️ ВСПОМОГАТЕЛЬНЫЕ ENDPOINT'Ы
    // ================================

    /**
     * 📊 Получить количество активных категорий
     */
    @GetMapping("/count")
    ResponseEntity<ApiResponse<Long>> getActiveCategoriesCount();

    /**
     * 🔍 Проверить существование категории по имени
     */
    @GetMapping("/exists")
    ResponseEntity<ApiResponse<Boolean>> checkCategoryExists(@RequestParam String name);

    /**
     * 🔧 Проверка работоспособности сервиса категорий
     */
    @GetMapping("/health")
    ResponseEntity<ApiResponse<String>> healthCheck();
}