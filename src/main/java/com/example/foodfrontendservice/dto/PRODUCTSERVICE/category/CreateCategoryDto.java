package com.example.foodfrontendservice.dto.PRODUCTSERVICE.category;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreateCategoryDto {
    private String name;
    private String description;
    private MultipartFile imageFile;
    private Boolean isActive = true;
    private Integer sortOrder = 0;
}
