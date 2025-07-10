package com.example.foodfrontendservice.dto.AUTSERVICE;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/// 🔄 DTO для обновления адреса с геокодированием
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserAddressDto {

    @NotBlank(message = "Улица обязательна")
    @Size(max = 255, message = "Название улицы не должно превышать 255 символов")
    private String street;

    @NotBlank(message = "Город обязателен")
    @Size(max = 100, message = "Название города не должно превышать 100 символов")
    private String city;

    @Size(max = 100, message = "Название региона не должно превышать 100 символов")
    private String region;

    @Size(max = 100, message = "Название страны не должно превышать 100 символов")
    private String country;

    @Size(max = 20, message = "Почтовый индекс не должен превышать 20 символов")
    private String postalCode;

    // 🔄 Автоматически получить координаты через геокодирование
    @Builder.Default
    private Boolean autoGeocode = true;
}
