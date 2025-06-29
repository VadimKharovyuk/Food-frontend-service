package com.example.foodfrontendservice.dto.PRODUCTSERVICE.category;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateCategoryDto {
    private String name;
    private String description;

    @JsonIgnore
    private MultipartFile imageFile;

    private Boolean isActive = true;
    private Integer sortOrder = 0;
}