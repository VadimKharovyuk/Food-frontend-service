package com.example.foodfrontendservice.dto.PRODUCTSERVICE.store;


import com.example.foodfrontendservice.dto.PRODUCTSERVICE.StoreResponseDto;
import lombok.Data;

import lombok.Data;

@Data
public class StoreCreationResponse {
    private StoreResponseDto store;
    private Boolean success;
    private String message;
    private String errorCode;

    public static StoreCreationResponse success(StoreResponseDto store) {
        StoreCreationResponse response = new StoreCreationResponse();
        response.setStore(store);
        response.setSuccess(true);
        response.setMessage("Магазин успешно создан");
        return response;
    }

    public static StoreCreationResponse error(String message, String errorCode) {
        StoreCreationResponse response = new StoreCreationResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setErrorCode(errorCode);
        return response;
    }
}
