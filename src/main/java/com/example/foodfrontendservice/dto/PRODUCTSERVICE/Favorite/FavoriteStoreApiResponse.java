package com.example.foodfrontendservice.dto.PRODUCTSERVICE.Favorite;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteStoreApiResponse<T> {


    private T data;


    private Boolean success;


    private String message;


    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;


    private Metadata metadata;


    /**
     * Создать успешный ответ с данными
     */
    public static <T> FavoriteStoreApiResponse<T> success(T data) {
        return FavoriteStoreApiResponse.<T>builder()
                .data(data)
                .success(true)
                .message("Операция выполнена успешно")
                .timestamp(LocalDateTime.now())
                .build();
    }


    public static <T> FavoriteStoreApiResponse<T> success(T data, String message) {
        return FavoriteStoreApiResponse.<T>builder()
                .data(data)
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }


    public static <T> FavoriteStoreApiResponse<T> error(String message) {
        return FavoriteStoreApiResponse.<T>builder()
                .data(null)
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }


    public static <T> FavoriteStoreApiResponse<T> empty(String message) {
        return FavoriteStoreApiResponse.<T>builder()
                .data(null)
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }



    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metadata {
        private Long totalCount;
        private Integer page;
        private Integer size;
        private Long userId;
        private String operation; // ADD, REMOVE, GET, LIST
    }
}