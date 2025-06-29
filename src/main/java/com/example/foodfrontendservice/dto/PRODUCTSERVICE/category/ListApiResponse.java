package com.example.foodfrontendservice.dto.PRODUCTSERVICE.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Универсальная обертка для списков (для категорий)
 * @param <T> тип элементов в списке
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListApiResponse<T> {

    /**
     * Список данных
     */
    private List<T> data;

    /**
     * Количество элементов в списке
     */
    private Integer totalCount;

    /**
     * Статус успешности операции
     */
    private Boolean success;

    /**
     * Сообщение об ошибке (если есть)
     */
    private String message;

    /**
     * Временная метка создания ответа
     */
    private LocalDateTime timestamp;

    // ================================
    // СТАТИЧЕСКИЕ МЕТОДЫ ДЛЯ СОЗДАНИЯ
    // ================================

    /**
     * Создать успешный ответ со списком данных
     * @param items список элементов
     * @param <T> тип элементов
     * @return успешный ответ со списком
     */
    public static <T> ListApiResponse<T> success(List<T> items) {
        return ListApiResponse.<T>builder()
                .data(items)
                .totalCount(items != null ? items.size() : 0)
                .success(true)
                .message(null)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Создать пустой успешный ответ
     * @param <T> тип элементов
     * @return пустой успешный ответ
     */
    public static <T> ListApiResponse<T> empty() {
        return ListApiResponse.<T>builder()
                .data(Collections.emptyList())
                .totalCount(0)
                .success(true)
                .message(null)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Создать пустой успешный ответ с сообщением
     * @param message сообщение
     * @param <T> тип элементов
     * @return пустой успешный ответ с сообщением
     */
    public static <T> ListApiResponse<T> emptyWithMessage(String message) {
        return ListApiResponse.<T>builder()
                .data(Collections.emptyList())
                .totalCount(0)
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Создать ответ с ошибкой
     * @param message сообщение об ошибке
     * @param <T> тип элементов
     * @return ответ с ошибкой
     */
    public static <T> ListApiResponse<T> error(String message) {
        return ListApiResponse.<T>builder()
                .data(Collections.emptyList())
                .totalCount(0)
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Создать успешный ответ с кастомным сообщением
     * @param items список элементов
     * @param message сообщение об успехе
     * @param <T> тип элементов
     * @return успешный ответ с сообщением
     */
    public static <T> ListApiResponse<T> successWithMessage(List<T> items, String message) {
        return ListApiResponse.<T>builder()
                .data(items)
                .totalCount(items != null ? items.size() : 0)
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Создать успешный ответ с кастомным totalCount
     * (полезно когда показываем только часть от общего количества)
     * @param items список элементов
     * @param customTotalCount кастомное общее количество
     * @param <T> тип элементов
     * @return успешный ответ с кастомным totalCount
     */
    public static <T> ListApiResponse<T> successWithCustomCount(List<T> items, Integer customTotalCount) {
        return ListApiResponse.<T>builder()
                .data(items)
                .totalCount(customTotalCount)
                .success(true)
                .message(null)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ================================

    /**
     * Проверить, является ли ответ успешным
     * @return true если ответ успешный
     */
    public boolean isSuccess() {
        return success != null && success;
    }

    /**
     * Проверить, содержит ли ответ данные
     * @return true если ответ содержит данные
     */
    public boolean hasData() {
        return data != null && !data.isEmpty();
    }

    /**
     * Проверить, является ли список пустым
     * @return true если список пустой
     */
    public boolean isEmpty() {
        return data == null || data.isEmpty();
    }

    /**
     * Проверить, является ли ответ ошибкой
     * @return true если ответ содержит ошибку
     */
    public boolean isError() {
        return success == null || !success;
    }

    /**
     * Получить количество элементов в текущем списке
     * @return количество элементов
     */
    public int getDataSize() {
        return data != null ? data.size() : 0;
    }

    /**
     * Получить данные или пустой список если данных нет
     * @return список данных (никогда не null)
     */
    public List<T> getDataOrEmpty() {
        return data != null ? data : Collections.emptyList();
    }

    /**
     * Получить первый элемент списка (если есть)
     * @return первый элемент или null
     */
    public T getFirstItem() {
        return hasData() ? data.get(0) : null;
    }

    /**
     * Получить последний элемент списка (если есть)
     * @return последний элемент или null
     */
    public T getLastItem() {
        return hasData() ? data.get(data.size() - 1) : null;
    }
}