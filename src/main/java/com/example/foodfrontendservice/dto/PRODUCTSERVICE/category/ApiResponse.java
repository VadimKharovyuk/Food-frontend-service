package com.example.foodfrontendservice.dto.PRODUCTSERVICE.category;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse<T> {

    /**
     * Данные ответа
     */
    @JsonProperty("data")
    private T data;

    /**
     * Статус успешности операции
     */
    @JsonProperty("success")
    private Boolean success;

    /**
     * Сообщение об ошибке (если есть)
     */
    @JsonProperty("message")
    private String message;

    /**
     * Временная метка создания ответа
     * ✅ Изменено на String для совместимости с JSON
     */
    @JsonProperty("timestamp")
    private String timestamp;

    // ================================
    // СТАТИЧЕСКИЕ МЕТОДЫ ДЛЯ СОЗДАНИЯ
    // ================================

    /**
     * Создать успешный ответ с данными
     * @param data данные для ответа
     * @param <T> тип данных
     * @return успешный ответ
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .data(data)
                .success(true)
                .message(null)
                .timestamp(LocalDateTime.now().toString()) // ← Автоматически в String
                .build();
    }

    /**
     * Создать успешный ответ с данными и сообщением
     * @param data данные для ответа
     * @param message сообщение
     * @param <T> тип данных
     * @return успешный ответ
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .data(data)
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Создать ответ с ошибкой
     * @param message сообщение об ошибке
     * @param <T> тип данных
     * @return ответ с ошибкой
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .data(null)
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now().toString()) // ← Автоматически в String
                .build();
    }

    /**
     * Создать ответ "не найдено"
     * @param message сообщение о том, что не найдено
     * @param <T> тип данных
     * @return ответ "не найдено"
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.<T>builder()
                .data(null)
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Проверить, является ли ответ успешным
     * @return true если ответ успешный
     */
    public boolean isSuccess() {
        return success != null && success;
    }

    /**
     * Проверить, является ли ответ ошибкой
     * @return true если ответ содержит ошибку
     */
    public boolean isError() {
        return success == null || !success;
    }
}