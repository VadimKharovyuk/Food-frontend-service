package com.example.foodfrontendservice.dto.AUTSERVICE;




import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// 📍 DTO для обновления геолокации пользователя
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserLocationDto {

    @NotNull(message = "Широта обязательна")
    @DecimalMin(value = "-90.0", message = "Широта должна быть не меньше -90")
    @DecimalMax(value = "90.0", message = "Широта должна быть не больше 90")
    @Digits(integer = 2, fraction = 8, message = "Некорректный формат широты")
    private BigDecimal latitude;

    @NotNull(message = "Долгота обязательна")
    @DecimalMin(value = "-180.0", message = "Долгота должна быть не меньше -180")
    @DecimalMax(value = "180.0", message = "Долгота должна быть не больше 180")
    @Digits(integer = 3, fraction = 8, message = "Некорректный формат долготы")
    private BigDecimal longitude;

    // 📍 Опциональный адрес
    @Size(max = 255, message = "Название улицы не должно превышать 255 символов")
    private String street;

    @Size(max = 100, message = "Название города не должно превышать 100 символов")
    private String city;

    @Size(max = 100, message = "Название региона не должно превышать 100 символов")
    private String region;

    @Size(max = 100, message = "Название страны не должно превышать 100 символов")
    private String country;

    @Size(max = 20, message = "Почтовый индекс не должен превышать 20 символов")
    private String postalCode;

    // 🔄 Флаг для автоматического геокодирования
    @Builder.Default
    private Boolean autoGeocode = false;
}


