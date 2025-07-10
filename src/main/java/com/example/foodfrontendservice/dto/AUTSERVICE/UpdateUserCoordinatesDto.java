package com.example.foodfrontendservice.dto.AUTSERVICE;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserCoordinatesDto {

    @NotNull(message = "Широта обязательна")
    @DecimalMin(value = "-90.0", message = "Широта должна быть не меньше -90")
    @DecimalMax(value = "90.0", message = "Широта должна быть не больше 90")
    @Digits(integer = 2, fraction = 15, message = "Некорректный формат широты")  // ← Увеличили fraction
    private Double latitude;

    @NotNull(message = "Долгота обязательна")
    @DecimalMin(value = "-180.0", message = "Долгота должна быть не меньше -180")
    @DecimalMax(value = "180.0", message = "Долгота должна быть не больше 180")
    @Digits(integer = 3, fraction = 15, message = "Некорректный формат долготы")  // ← Увеличили fraction
    private Double longitude;
}