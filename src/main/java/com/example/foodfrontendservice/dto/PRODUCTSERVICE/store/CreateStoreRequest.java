package com.example.foodfrontendservice.dto.PRODUCTSERVICE.store;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateStoreRequest {

//    @NotBlank(message = "Название магазина обязательно")
//    @Size(max = 100, message = "Название не должно превышать 100 символов")
    private String name;
//
//    @Size(max = 500, message = "Описание не должно превышать 500 символов")
    private String description;

    // АДРЕС КАК ОТДЕЛЬНЫЕ ПОЛЯ
//    @NotBlank(message = "Улица обязательна")
//    @Size(max = 200, message = "Адрес улицы не должен превышать 200 символов")
    private String street;

//    @NotBlank(message = "Город обязателен")
//    @Size(max = 100, message = "Название города не должно превышать 100 символов")
    private String city;
//
//    @Size(max = 100, message = "Название региона не должно превышать 100 символов")
    private String region;

//    @Pattern(regexp = "^[0-9]{5,10}$", message = "Неверный формат почтового индекса")
    private String postalCode;

//    @NotBlank(message = "Страна обязательна")
//    @Size(max = 100, message = "Название страны не должно превышать 100 символов")
    private String country;
//
//    @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]{10,20}$", message = "Неверный формат телефона")
    private String phone;
//
//    @Email(message = "Неверный формат email")
//    @Size(max = 100, message = "Email не должен превышать 100 символов")
    private String email;

//    @NotNull(message = "Радиус доставки обязателен")
//    @Min(value = 1, message = "Радиус доставки должен быть не менее 1 км")
//    @Max(value = 50, message = "Радиус доставки не может превышать 50 км")
    private Integer deliveryRadius;

//    @NotNull(message = "Стоимость доставки обязательна")
//    @DecimalMin(value = "0.00", message = "Стоимость доставки не может быть отрицательной")
//    @DecimalMax(value = "99999.99", message = "Стоимость доставки слишком высокая")
    private BigDecimal deliveryFee;

//    @NotNull(message = "Время доставки обязательно")
//    @Min(value = 10, message = "Время доставки должно быть не менее 10 минут")
//    @Max(value = 180, message = "Время доставки не может превышать 180 минут")
    private Integer estimatedDeliveryTime;

    @JsonIgnore
    private MultipartFile imageFile;

    // ✅ Результат загрузки изображения (если уже загружено)
    private String imageUrl;
    private String imageId;

    private Boolean isActive = true;

    // Метод для создания объекта адреса (если нужен)
    public CreateAddressRequest getAddressObject() {
        CreateAddressRequest address = new CreateAddressRequest();
        address.setStreet(this.street);
        address.setCity(this.city);
        address.setRegion(this.region);
        address.setPostalCode(this.postalCode);
        address.setCountry(this.country);
        address.setAutoGeocode(true);
        return address;
    }
}