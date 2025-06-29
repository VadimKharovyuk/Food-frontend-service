package com.example.foodfrontendservice.controller;
import com.example.foodfrontendservice.config.CurrentUser;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.category.*;
import com.example.foodfrontendservice.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/frontend/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 📋 Получить все активные категории (полная информация)
     */
    @GetMapping
    public ResponseEntity<ListApiResponse<CategoryResponseDto>> getAllActiveCategories() {
        log.info("📋 GET /api/frontend/categories - Getting all active categories");

        ListApiResponse<CategoryResponseDto> response = categoryService.getAllActiveCategories();

        if (response.isSuccess()) {
            log.info("✅ Retrieved {} active categories", response.getTotalCount());
            return ResponseEntity.ok(response);
        } else {
            log.warn("❌ Failed to get active categories: {}", response.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 📋 Получить все категории включая неактивные (только для админов)
     */
    @GetMapping("/all")
    public ResponseEntity<ListApiResponse<CategoryResponseDto>> getAllCategories(
            HttpServletRequest request) {

        log.info("📋 GET /api/frontend/categories/all - Getting all categories");

        // Проверяем роль
        String userRole = request.getHeader("X-User-Role");
        if (!"ROLE_ADMIN".equals(userRole)) {
            log.warn("❌ Access denied for role: {}", userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ListApiResponse.error("Доступ запрещен"));
        }

        ListApiResponse<CategoryResponseDto> response = categoryService.getAllCategories();

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 🔍 Поиск категорий по названию
     */
    @GetMapping("/search")
    public ResponseEntity<ListApiResponse<CategoryResponseDto>> searchCategories(
            @RequestParam String name) {

        log.info("🔍 GET /api/frontend/categories/search - Searching categories: {}", name);

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ListApiResponse.error("Параметр поиска не может быть пустым"));
        }

        ListApiResponse<CategoryResponseDto> response = categoryService.searchCategories(name.trim());

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ================================
    // 📊 ПОЛУЧЕНИЕ СПИСКОВ - КРАТКАЯ ИНФОРМАЦИЯ
    // ================================

    /**
     * 📊 Получить краткий список активных категорий (для dropdown/селекторов)
     */
    @GetMapping("/brief")
    public ResponseEntity<ListApiResponse<CategoryBaseProjection>> getActiveCategoriesBrief() {
        log.info("📊 GET /api/frontend/categories/brief - Getting brief categories");

        ListApiResponse<CategoryBaseProjection> response = categoryService.getActiveCategoriesBrief();

        if (response.isSuccess()) {
            log.info("✅ Retrieved {} brief categories", response.getTotalCount());
            return ResponseEntity.ok(response);
        } else {
            log.warn("❌ Failed to get brief categories: {}", response.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 📊 Получить краткий список всех категорий (только для админов)
     */
    @GetMapping("/brief/all")
    public ResponseEntity<ListApiResponse<CategoryBaseProjection>> getAllCategoriesBrief(
            HttpServletRequest request) {

        log.info("📊 GET /api/frontend/categories/brief/all - Getting all brief categories");

        String userRole = request.getHeader("X-User-Role");
        if (!"ROLE_ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ListApiResponse.error("Доступ запрещен"));
        }

        ListApiResponse<CategoryBaseProjection> response = categoryService.getAllCategoriesBrief();

        return response.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 🔍 Поиск кратких данных категорий
     */
    @GetMapping("/brief/search")
    public ResponseEntity<ListApiResponse<CategoryBaseProjection>> searchCategoriesBrief(
            @RequestParam String name) {

        log.info("🔍 GET /api/frontend/categories/brief/search - Searching brief categories: {}", name);

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ListApiResponse.error("Параметр поиска не может быть пустым"));
        }

        ListApiResponse<CategoryBaseProjection> response = categoryService.searchCategoriesBrief(name.trim());

        return response.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 📊 Получить краткие данные категорий по списку ID
     */
    @PostMapping("/brief/by-ids")
    public ResponseEntity<ListApiResponse<CategoryBaseProjection>> getCategoriesBriefByIds(
            @RequestBody List<Long> ids) {

        log.info("📊 POST /api/frontend/categories/brief/by-ids - Getting categories by IDs: {}", ids);

        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ListApiResponse.error("Список ID не может быть пустым"));
        }

        ListApiResponse<CategoryBaseProjection> response = categoryService.getCategoriesBriefByIds(ids);

        return response.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // ================================
    // 🔍 ПОЛУЧЕНИЕ ОТДЕЛЬНЫХ КАТЕГОРИЙ
    // ================================

    /**
     * 🔍 Получить категорию по ID (полная информация)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> getCategoryById(@PathVariable Long id) {
        log.info("🔍 GET /api/frontend/categories/{} - Getting category by ID", id);

        ApiResponse<CategoryResponseDto> response = categoryService.getCategoryById(id);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else if (!response.isSuccess() && response.getMessage().contains("не найдена")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 📊 Получить краткую информацию о категории по ID
     */
    @GetMapping("/{id}/brief")
    public ResponseEntity<ApiResponse<CategoryBaseProjection>> getCategoryBrief(@PathVariable Long id) {
        log.info("📊 GET /api/frontend/categories/{}/brief - Getting brief category", id);

        ApiResponse<CategoryBaseProjection> response = categoryService.getCategoryBrief(id);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else if (!response.isSuccess() && response.getMessage().contains("не найдена")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ================================
    // ✏️ СОЗДАНИЕ И ОБНОВЛЕНИЕ КАТЕГОРИЙ (ADMIN ONLY)
    // ================================

    /**
     * ➕ Создать новую категорию
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CategoryResponseDto>> createCategory(
            @Valid @ModelAttribute CreateCategoryDto createCategoryDto,
            @CurrentUser Long userId,
            HttpServletRequest request) {

        log.info("➕ POST /api/frontend/categories - Creating category: {}", createCategoryDto.getName());

        // Проверяем роль
        String userRole = request.getHeader("X-User-Role");
        if (!"ROLE_ADMIN".equals(userRole)) {
            log.warn("❌ Access denied for user {} with role {}", userId, userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Доступ запрещен"));
        }

        ApiResponse<CategoryResponseDto> response = categoryService.createCategory(userId, createCategoryDto);

        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * ✏️ Обновить категорию
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CategoryResponseDto>> updateCategory(
            @PathVariable Long id,
            @Valid @ModelAttribute CreateCategoryDto updateCategoryDto,
            @CurrentUser Long userId,
            HttpServletRequest request) {

        log.info("✏️ PUT /api/frontend/categories/{} - Updating category", id);

        String userRole = request.getHeader("X-User-Role");
        if (!"ROLE_ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Доступ запрещен"));
        }

        ApiResponse<CategoryResponseDto> response = categoryService.updateCategory(id, userId, updateCategoryDto);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else if (!response.isSuccess() && response.getMessage().contains("не найдена")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 🗑️ Удалить категорию
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable Long id,
            @CurrentUser Long userId,
            HttpServletRequest request) {

        log.info("🗑️ DELETE /api/frontend/categories/{} - Deleting category", id);

        String userRole = request.getHeader("X-User-Role");
        if (!"ROLE_ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Доступ запрещен"));
        }

        ApiResponse<Void> response = categoryService.deleteCategory(id, userId);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else if (!response.isSuccess() && response.getMessage().contains("не найдена")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 🔄 Переключить статус категории
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> toggleCategoryStatus(
            @PathVariable Long id,
            @CurrentUser Long userId,
            HttpServletRequest request) {

        log.info("🔄 PATCH /api/frontend/categories/{}/toggle - Toggling category status", id);

        String userRole = request.getHeader("X-User-Role");
        if (!"ROLE_ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Доступ запрещен"));
        }

        ApiResponse<CategoryResponseDto> response = categoryService.toggleCategoryStatus(id, userId);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else if (!response.isSuccess() && response.getMessage().contains("не найдена")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ================================
    // 🛠️ ВСПОМОГАТЕЛЬНЫЕ ENDPOINT'Ы
    // ================================

    /**
     * 📊 Получить количество активных категорий
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getActiveCategoriesCount() {
        log.info("📊 GET /api/frontend/categories/count - Getting categories count");

        ApiResponse<Long> response = categoryService.getActiveCategoriesCount();

        return response.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 🔍 Проверить существование категории по имени
     */
    @GetMapping("/exists")
    public ResponseEntity<ApiResponse<Boolean>> checkCategoryExists(@RequestParam String name) {
        log.info("🔍 GET /api/frontend/categories/exists - Checking category: {}", name);

        ApiResponse<Boolean> response = categoryService.checkCategoryExists(name);

        return response.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 🔧 Проверка работоспособности
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        log.debug("🔧 GET /api/frontend/categories/health - Health check");

        ApiResponse<String> response = categoryService.healthCheck();

        return ResponseEntity.ok(response);
    }
}