package com.example.foodfrontendservice.Client;

import com.example.foodfrontendservice.Client.Fallback.CategoryServiceClientFallback;
import com.example.foodfrontendservice.config.FeignConfig;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.category.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
@FeignClient(
        name = "product-service",
        path = "/api/categories",
        fallback = CategoryServiceClientFallback.class,
        configuration = FeignConfig.class,
        contextId = "categoryServiceClient"
)
public interface CategoryServiceClient {


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ApiResponse<CategoryResponseDto>> createCategory(
            @ModelAttribute CreateCategoryDto createCategoryDto
    );

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ApiResponse<CategoryResponseDto>> updateCategory(
            @PathVariable Long id,
            @ModelAttribute CreateCategoryDto updateCategoryDto
    );

    // ПОЛУЧЕНИЕ СПИСКОВ
    @GetMapping("/brief")
    ResponseEntity<ListApiResponse<CategoryDto>> getActiveCategoriesBrief();

    @GetMapping("/all")
    ResponseEntity<ListApiResponse<CategoryResponseDto>> getAllCategories();

    @GetMapping
    ResponseEntity<ListApiResponse<CategoryResponseDto>> getAllActiveCategories();

    @GetMapping("/search")
    ResponseEntity<ListApiResponse<CategoryResponseDto>> searchCategories(@RequestParam String name);

    @GetMapping("/brief/all")
    ResponseEntity<ListApiResponse<CategoryDto>> getAllCategoriesBrief();

    @GetMapping("/brief/search")
    ResponseEntity<ListApiResponse<CategoryDto>> searchCategoriesBrief(@RequestParam String name);

    @PostMapping("/brief/by-ids")
    ResponseEntity<ListApiResponse<CategoryDto>> getCategoriesBriefByIds(@RequestBody List<Long> ids);

    // ПОЛУЧЕНИЕ ОТДЕЛЬНЫХ КАТЕГОРИЙ
    @GetMapping("/{id}")
    ResponseEntity<ApiResponse<CategoryResponseDto>> getCategoryById(@PathVariable Long id);

    @GetMapping("/{id}/brief")
    ResponseEntity<ApiResponse<CategoryDto>> getCategoryBrief(@PathVariable Long id);

    // УДАЛЕНИЕ И TOGGLE (userId через заголовок автоматически)
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id);

    @PostMapping("/{id}/toggle")
    ResponseEntity<ApiResponse<CategoryResponseDto>> toggleCategoryStatus(@PathVariable Long id);
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    @GetMapping("/count")
    ResponseEntity<ApiResponse<Long>> getActiveCategoriesCount();

    @GetMapping("/exists")
    ResponseEntity<ApiResponse<Boolean>> checkCategoryExists(@RequestParam String name);

    @GetMapping("/health")
    ResponseEntity<ApiResponse<String>> healthCheck();
}