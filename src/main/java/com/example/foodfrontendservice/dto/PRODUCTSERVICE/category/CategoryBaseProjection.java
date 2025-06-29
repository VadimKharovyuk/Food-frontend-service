package com.example.foodfrontendservice.dto.PRODUCTSERVICE.category;

public interface CategoryBaseProjection {
    Long getId();
    String getName();
    Boolean getIsActive();
    Integer getSortOrder();
}
