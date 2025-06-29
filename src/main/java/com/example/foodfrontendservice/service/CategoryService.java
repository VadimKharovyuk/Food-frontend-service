
package com.example.foodfrontendservice.service;
import com.example.foodfrontendservice.Client.CategoryServiceClient;
import com.example.foodfrontendservice.dto.PRODUCTSERVICE.category.*;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryServiceClient categoryServiceClient;



    /**
     * Получить все активные категории (полная информация)
     */
    public ListApiResponse<CategoryResponseDto> getAllActiveCategories() {
        log.debug("Getting all active categories from product service");

        try {
            ResponseEntity<ListApiResponse<CategoryResponseDto>> response =
                    categoryServiceClient.getAllActiveCategories();

            if (response.getBody() != null) {
                log.debug("Successfully retrieved {} active categories",
                        response.getBody().getTotalCount());
                return response.getBody();
            } else {
                log.warn("Empty response from product service for active categories");
                return ListApiResponse.error("Не удалось получить категории");
            }

        } catch (Exception e) {
            log.error("Error calling product service for active categories", e);
            return ListApiResponse.error("Ошибка связи с сервисом продуктов");
        }
    }

    /**
     * Получить все категории включая неактивные (только для админов)
     */
    public ListApiResponse<CategoryResponseDto> getAllCategories() {
        log.debug("Getting all categories from product service");

        try {
            ResponseEntity<ListApiResponse<CategoryResponseDto>> response =
                    categoryServiceClient.getAllCategories();

            if (response.getBody() != null) {
                log.debug("Successfully retrieved {} total categories",
                        response.getBody().getTotalCount());
                return response.getBody();
            } else {
                log.warn("Empty response from product service for all categories");
                return ListApiResponse.error("Не удалось получить все категории");
            }

        } catch (Exception e) {
            log.error("Error calling product service for all categories", e);
            return ListApiResponse.error("Ошибка связи с сервисом продуктов");
        }
    }

    /**
     * Поиск категорий по названию (полная информация)
     */
    public ListApiResponse<CategoryResponseDto> searchCategories(String name) {
        log.debug("Searching categories by name: {}", name);

        try {
            ResponseEntity<ListApiResponse<CategoryResponseDto>> response =
                    categoryServiceClient.searchCategories(name);

            if (response.getBody() != null) {
                log.debug("Successfully found {} categories for search: {}",
                        response.getBody().getTotalCount(), name);
                return response.getBody();
            } else {
                return ListApiResponse.error("Не удалось выполнить поиск категорий");
            }

        } catch (Exception e) {
            log.error("Error searching categories by name: {}", name, e);
            return ListApiResponse.error("Ошибка поиска категорий");
        }
    }

    // ================================
    // 📊 ПОЛУЧЕНИЕ СПИСКОВ - КРАТКАЯ ИНФОРМАЦИЯ (ПРОЕКЦИИ)
    // ================================

    /**
     * Получить краткий список активных категорий (для dropdown/селекторов)
     */
    public ListApiResponse<CategoryDto> getActiveCategoriesBrief() {
        log.debug("Getting brief active categories from product service");

        try {
            // Добавьте подробное логирование
            log.info("Calling product-service at /api/categories/brief");

            ResponseEntity<ListApiResponse<CategoryDto>> response =
                    categoryServiceClient.getActiveCategoriesBrief();

            log.info("Response status: {}", response.getStatusCode());
            log.info("Response body is null: {}", response.getBody() == null);

            if (response.getBody() != null) {
                log.debug("Successfully retrieved {} brief categories",
                        response.getBody().getTotalCount());
                return response.getBody();
            } else {
                log.warn("Empty response from product service for brief categories");
                return ListApiResponse.error("Не удалось получить краткие данные категорий");
            }

        } catch (FeignException e) {
            log.error("Feign error calling product service: status={}, message={}",
                    e.status(), e.getMessage());
            return ListApiResponse.error("Ошибка связи с сервисом продуктов: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error calling product service", e);
            return ListApiResponse.error("Ошибка связи с сервисом продуктов: " + e.getClass().getSimpleName());
        }
    }
    /**
     * Получить краткий список всех категорий (только для админов)
     */
    public ListApiResponse<CategoryDto> getAllCategoriesBrief() {
        log.debug("Getting all brief categories from product service");

        try {
            ResponseEntity<ListApiResponse<CategoryDto>> response =
                    categoryServiceClient.getAllCategoriesBrief();

            if (response.getBody() != null) {
                return response.getBody();
            } else {
                return ListApiResponse.error("Не удалось получить все краткие категории");
            }

        } catch (Exception e) {
            log.error("Error calling product service for all brief categories", e);
            return ListApiResponse.error("Ошибка связи с сервисом продуктов");
        }
    }

    /**
     * Поиск кратких данных категорий по названию
     */
    public ListApiResponse<CategoryDto> searchCategoriesBrief(String name) {
        log.debug("Searching brief categories by name: {}", name);

        try {
            ResponseEntity<ListApiResponse<CategoryDto>> response =
                    categoryServiceClient.searchCategoriesBrief(name);

            if (response.getBody() != null) {
                return response.getBody();
            } else {
                return ListApiResponse.error("Не удалось выполнить поиск кратких категорий");
            }

        } catch (Exception e) {
            log.error("Error searching brief categories by name: {}", name, e);
            return ListApiResponse.error("Ошибка поиска кратких категорий");
        }
    }

    /**
     * Получить краткие данные категорий по списку ID
     */
    public ListApiResponse<CategoryDto> getCategoriesBriefByIds(List<Long> ids) {
        log.debug("Getting brief categories by IDs: {}", ids);

        try {
            ResponseEntity<ListApiResponse<CategoryDto>> response =
                    categoryServiceClient.getCategoriesBriefByIds(ids);

            if (response.getBody() != null) {
                return response.getBody();
            } else {
                return ListApiResponse.error("Не удалось получить категории по ID");
            }

        } catch (Exception e) {
            log.error("Error getting brief categories by IDs: {}", ids, e);
            return ListApiResponse.error("Ошибка получения категорий по ID");
        }
    }

    // ================================
    // 🔍 ПОЛУЧЕНИЕ ОТДЕЛЬНЫХ КАТЕГОРИЙ
    // ================================

    /**
     * Получить категорию по ID (полная информация)
     */
    public ApiResponse<CategoryResponseDto> getCategoryById(Long id) {
        log.debug("Getting category by ID: {}", id);

        try {
            ResponseEntity<ApiResponse<CategoryResponseDto>> response =
                    categoryServiceClient.getCategoryById(id);

            if (response.getBody() != null) {
                return response.getBody();
            } else {
                return ApiResponse.error("Не удалось получить категорию");
            }

        } catch (Exception e) {
            log.error("Error getting category by ID: {}", id, e);
            return ApiResponse.error("Ошибка получения категории");
        }
    }

    /**
     * Получить краткую информацию о категории по ID
     */
    public ApiResponse<CategoryDto> getCategoryBrief(Long id) {
        log.debug("Getting brief category by ID: {}", id);

        try {
            ResponseEntity<ApiResponse<CategoryDto>> response =
                    categoryServiceClient.getCategoryBrief(id);

            if (response.getBody() != null) {
                return response.getBody();
            } else {
                return ApiResponse.error("Не удалось получить краткие данные категории");
            }

        } catch (Exception e) {
            log.error("Error getting brief category by ID: {}", id, e);
            return ApiResponse.error("Ошибка получения краткой категории");
        }
    }

    // ================================
    // ✏️ СОЗДАНИЕ И ОБНОВЛЕНИЕ КАТЕГОРИЙ (ADMIN ONLY)
    // ================================


    /**
     * Создать новую категорию
     */
    public ApiResponse<CategoryResponseDto> createCategory(CreateCategoryDto createCategoryDto) {
        log.debug("Creating category: {}", createCategoryDto.getName());

        try {
            // userId автоматически передается через FeignAuthInterceptor в заголовке X-User-Id
            ResponseEntity<ApiResponse<CategoryResponseDto>> response =
                    categoryServiceClient.createCategory(createCategoryDto);

            if (response.getBody() != null) {
                log.debug("Successfully created category: {}", createCategoryDto.getName());
                return response.getBody();
            } else {
                return ApiResponse.error("Не удалось создать категорию");
            }

        } catch (Exception e) {
            log.error("Error creating category: {}", createCategoryDto.getName(), e);
            return ApiResponse.error("Ошибка создания категории: " + e.getMessage());
        }
    }

    /**
     * Обновить существующую категорию
     */
    public ApiResponse<CategoryResponseDto> updateCategory(Long id, CreateCategoryDto updateCategoryDto) {
        log.debug("Updating category ID: {} with name: {}", id, updateCategoryDto.getName());

        try {
            // userId автоматически передается через FeignAuthInterceptor в заголовке X-User-Id
            ResponseEntity<ApiResponse<CategoryResponseDto>> response =
                    categoryServiceClient.updateCategory(id, updateCategoryDto);

            if (response.getBody() != null) {
                log.debug("Successfully updated category ID: {}", id);
                return response.getBody();
            } else {
                return ApiResponse.error("Не удалось обновить категорию");
            }

        } catch (Exception e) {
            log.error("Error updating category ID: {}", id, e);
            return ApiResponse.error("Ошибка обновления категории: " + e.getMessage());
        }
    }

    /**
     * Удалить категорию (мягкое удаление)
     * userId передается автоматически через FeignAuthInterceptor
     */
    public ApiResponse<Void> deleteCategory(Long id) {
        log.debug("Deleting category: {}", id);

        try {
            ResponseEntity<ApiResponse<Void>> response =
                    categoryServiceClient.deleteCategory(id);

            if (response.getBody() != null) {
                log.debug("Successfully deleted category ID: {}", id);
                return response.getBody();
            } else {
                return ApiResponse.error("Не удалось удалить категорию");
            }

        } catch (Exception e) {
            log.error("Error deleting category: {}", id, e);
            return ApiResponse.error("Ошибка удаления категории: " + e.getMessage());
        }
    }

    /**
     * Переключить статус категории
     * userId передается автоматически через FeignAuthInterceptor
     */
    /**
     * Переключить статус категории
     */
    public ApiResponse<CategoryResponseDto> toggleCategoryStatus(Long id) {
        log.debug("Toggling category status: {}", id);

        try {
            ResponseEntity<ApiResponse<CategoryResponseDto>> response =
                    categoryServiceClient.toggleCategoryStatus(id);

            if (response.getBody() != null) {
                log.debug("Successfully toggled status for category ID: {}", id);
                return response.getBody();
            } else {
                return ApiResponse.error("Не удалось изменить статус категории");
            }

        } catch (Exception e) {
            log.error("Error toggling category status: {}", id, e);
            return ApiResponse.error("Ошибка изменения статуса категории: " + e.getMessage());
        }
    }

    // ================================
    // 🛠️ ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ================================

    /**
     * Получить количество активных категорий
     */
    public ApiResponse<Long> getActiveCategoriesCount() {
        log.debug("Getting active categories count");

        try {
            ResponseEntity<ApiResponse<Long>> response =
                    categoryServiceClient.getActiveCategoriesCount();

            if (response.getBody() != null) {
                return response.getBody();
            } else {
                return ApiResponse.error("Не удалось получить количество категорий");
            }

        } catch (Exception e) {
            log.error("Error getting categories count", e);
            return ApiResponse.error("Ошибка получения количества категорий");
        }
    }

    /**
     * Проверить существование категории по имени
     */
    public ApiResponse<Boolean> checkCategoryExists(String name) {
        log.debug("Checking if category exists: {}", name);

        try {
            ResponseEntity<ApiResponse<Boolean>> response =
                    categoryServiceClient.checkCategoryExists(name);

            if (response.getBody() != null) {
                return response.getBody();
            } else {
                return ApiResponse.error("Не удалось проверить существование категории");
            }

        } catch (Exception e) {
            log.error("Error checking category existence: {}", name, e);
            return ApiResponse.error("Ошибка проверки существования категории");
        }
    }

    /**
     * Проверка работоспособности сервиса категорий
     */
    public ApiResponse<String> healthCheck() {
        try {
            ResponseEntity<ApiResponse<String>> response =
                    categoryServiceClient.healthCheck();

            if (response.getBody() != null) {
                return response.getBody();
            } else {
                return ApiResponse.error("Health check failed");
            }

        } catch (Exception e) {
            log.error("Error during health check", e);
            return ApiResponse.error("Category service health check failed");
        }
    }
}
