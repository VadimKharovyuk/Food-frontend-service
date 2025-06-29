package com.example.foodfrontendservice.dto.PRODUCTSERVICE.category;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryDto {
    private Long id;
    private String name;
    private Boolean isActive;
    private String imageUrl;  // ← Добавили поле для изображения
    private Integer sortOrder; // ← И sortOrder для полноты
}