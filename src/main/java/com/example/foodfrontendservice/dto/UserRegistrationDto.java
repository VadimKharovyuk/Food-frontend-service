package com.example.foodfrontendservice.dto;

import com.example.foodfrontendservice.enums.UserRole;
import lombok.Data;

@Data
public class UserRegistrationDto {
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private UserRole userRole;
}