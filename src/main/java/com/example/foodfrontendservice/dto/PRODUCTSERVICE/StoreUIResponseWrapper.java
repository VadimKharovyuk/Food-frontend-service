package com.example.foodfrontendservice.dto.PRODUCTSERVICE;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

// Для StoreUIDto (уже есть PagedApiResponse, но добавлю специализированную)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreUIResponseWrapper {
    private List<StoreUIDto> stores;
    private Integer totalCount;
    private Boolean success;
    private String message;
    private LocalDateTime timestamp;

    // Статические методы...
    public static StoreUIResponseWrapper success(List<StoreUIDto> stores) {
        return StoreUIResponseWrapper.builder()
                .stores(stores)
                .totalCount(stores.size())
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static StoreUIResponseWrapper error(String message) {
        return StoreUIResponseWrapper.builder()
                .stores(Collections.emptyList())
                .totalCount(0)
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
