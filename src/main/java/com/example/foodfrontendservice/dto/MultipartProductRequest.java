package com.example.foodfrontendservice.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultipartProductRequest {
    private String productJson;  // JSON представление продукта
    private byte[] imageData;    // Данные изображения
    private String imageFileName;
    private String imageContentType;
}
