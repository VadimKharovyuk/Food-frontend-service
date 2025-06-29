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
public class StoreResponseWrapper {
    private List<StoreResponseDto> stores;
    private Integer totalCount;
    private Boolean hasNext;
    private Boolean hasPrevious;
    private Integer currentPage;
    private Integer pageSize;
    private Boolean success;
    private String message;
    private LocalDateTime timestamp;

    public static StoreResponseWrapper error(String message) {
        return StoreResponseWrapper.builder()
                .stores(Collections.emptyList())
                .totalCount(0)
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
