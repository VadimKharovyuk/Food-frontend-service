package com.example.foodfrontendservice.Client.Fallback;

import com.example.foodfrontendservice.Client.CategoryServiceClient;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.category.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class CategoryServiceClientFallback implements CategoryServiceClient {


    @Override
    public ResponseEntity<ListApiResponse<CategoryResponseDto>> getAllActiveCategories() {
        log.warn("Fallback: Category service unavailable for active categories");
        return ResponseEntity.ok(ListApiResponse.error("Сервис категорий временно недоступен"));
    }

    @Override
    public ResponseEntity<ListApiResponse<CategoryResponseDto>> getAllCategories() {
        log.warn("Fallback: Category service unavailable for all categories");
        return ResponseEntity.ok(ListApiResponse.error("Сервис категорий временно недоступен"));
    }

    @Override
    public ResponseEntity<ListApiResponse<CategoryResponseDto>> searchCategories(String name) {
        log.warn("Fallback: Category service unavailable for search: {}", name);
        return ResponseEntity.ok(ListApiResponse.error("Поиск категорий временно недоступен"));
    }

    // ================================
    // 📊 ПОЛУЧЕНИЕ СПИСКОВ - КРАТКАЯ ИНФОРМАЦИЯ
    // ================================

    @Override
    public ResponseEntity<ListApiResponse<CategoryDto>> getActiveCategoriesBrief() {
        log.warn("Fallback: Category service unavailable for brief categories");
        return ResponseEntity.ok(ListApiResponse.error("Сервис категорий временно недоступен"));
    }

    @Override
    public ResponseEntity<ListApiResponse<CategoryDto>> getAllCategoriesBrief() {
        log.warn("Fallback: Category service unavailable for all brief categories");
        return ResponseEntity.ok(ListApiResponse.error("Сервис категорий временно недоступен"));
    }

    @Override
    public ResponseEntity<ListApiResponse<CategoryDto>> searchCategoriesBrief(String name) {
        log.warn("Fallback: Category service unavailable for brief search: {}", name);
        return ResponseEntity.ok(ListApiResponse.error("Поиск категорий временно недоступен"));
    }

    @Override
    public ResponseEntity<ListApiResponse<CategoryDto>> getCategoriesBriefByIds(List<Long> ids) {
        log.warn("Fallback: Category service unavailable for categories by IDs: {}", ids);
        return ResponseEntity.ok(ListApiResponse.error("Сервис категорий временно недоступен"));
    }

    // ================================
    // 🔍 ПОЛУЧЕНИЕ ОТДЕЛЬНЫХ КАТЕГОРИЙ
    // ================================

    @Override
    public ResponseEntity<ApiResponse<CategoryResponseDto>> getCategoryById(Long id) {
        log.warn("Fallback: Category service unavailable for category ID: {}", id);
        return ResponseEntity.ok(ApiResponse.error("Категория временно недоступна"));
    }

    @Override
    public ResponseEntity<ApiResponse<CategoryDto>> getCategoryBrief(Long id) {
        log.warn("Fallback: Category service unavailable for brief category ID: {}", id);
        return ResponseEntity.ok(ApiResponse.error("Категория временно недоступна"));
    }

    // ================================
    // ✏️ СОЗДАНИЕ И ОБНОВЛЕНИЕ КАТЕГОРИЙ
    // ================================

    @Override
    public ResponseEntity<ApiResponse<CategoryResponseDto>> createCategory(CreateCategoryDto createCategoryDto) {
        log.warn("Fallback: Category service unavailable for creating category: {}",
                createCategoryDto != null ? createCategoryDto.getName() : "unknown");
        return ResponseEntity.ok(ApiResponse.error("Создание категории временно недоступно"));
    }

    @Override
    public ResponseEntity<ApiResponse<CategoryResponseDto>> updateCategory(Long id, CreateCategoryDto updateCategoryDto) {
        log.warn("Fallback: Category service unavailable for updating category ID: {}", id);
        return ResponseEntity.ok(ApiResponse.error("Обновление категории временно недоступно"));
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> deleteCategory(Long id) {
        log.warn("Fallback: Category service unavailable for deleting category: {}", id);
        return ResponseEntity.ok(ApiResponse.error("Удаление категории временно недоступно"));
    }

    @Override
    public ResponseEntity<ApiResponse<CategoryResponseDto>> toggleCategoryStatus(Long id) {
        log.warn("Fallback: Category service unavailable for toggling category status: {}", id);
        return ResponseEntity.ok(ApiResponse.error("Изменение статуса категории временно недоступно"));
    }



    // ================================
    // 🛠️ ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ================================

    @Override
    public ResponseEntity<ApiResponse<Long>> getActiveCategoriesCount() {
        log.warn("Fallback: Category service unavailable for count");
        return ResponseEntity.ok(ApiResponse.error("Подсчет категорий временно недоступен"));
    }

    @Override
    public ResponseEntity<ApiResponse<Boolean>> checkCategoryExists(String name) {
        log.warn("Fallback: Category service unavailable for existence check: {}", name);
        return ResponseEntity.ok(ApiResponse.error("Проверка существования категории временно недоступна"));
    }

    @Override
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        log.warn("Fallback: Category service health check failed");
        return ResponseEntity.ok(ApiResponse.error("Category service is down"));
    }
}