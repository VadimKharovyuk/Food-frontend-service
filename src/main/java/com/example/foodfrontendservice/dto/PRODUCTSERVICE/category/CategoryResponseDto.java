package com.example.foodfrontendservice.dto.PRODUCTSERVICE.category;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CategoryResponseDto {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private Boolean isActive;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
