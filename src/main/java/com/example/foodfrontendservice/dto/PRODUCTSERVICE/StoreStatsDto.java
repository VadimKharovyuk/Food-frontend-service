package com.example.foodfrontendservice.dto.PRODUCTSERVICE;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreStatsDto {
    private Integer totalActiveStores;
    private Boolean hasStores;
    private String status;
}
