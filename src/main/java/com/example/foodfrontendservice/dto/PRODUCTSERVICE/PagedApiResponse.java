package com.example.foodfrontendservice.dto.PRODUCTSERVICE;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedApiResponse<T> {
    private List<T> data;
    private Integer totalCount;
    private Boolean hasNext;
    private Boolean hasPrevious;
    private Integer currentPage;
    private Integer pageSize;
    private Boolean success;
    private String message;
    private LocalDateTime timestamp;



    // Для обычного списка без пагинации
    public static <T> PagedApiResponse<T> success(List<T> items) {
        return PagedApiResponse.<T>builder()
                .data(items)
                .totalCount(items.size())
                .hasNext(false)
                .hasPrevious(false)
                .currentPage(0)
                .pageSize(items.size())
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Для создания из полученных данных с других микросервисов
    public static <T> PagedApiResponse<T> success(List<T> items, Integer totalCount,
                                                  Boolean hasNext, Boolean hasPrevious,
                                                  Integer currentPage, Integer pageSize) {
        return PagedApiResponse.<T>builder()
                .data(items)
                .totalCount(totalCount)
                .hasNext(hasNext)
                .hasPrevious(hasPrevious)
                .currentPage(currentPage)
                .pageSize(pageSize)
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> PagedApiResponse<T> error(String message) {
        return PagedApiResponse.<T>builder()
                .data(Collections.emptyList())
                .totalCount(0)
                .hasNext(false)
                .hasPrevious(false)
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}