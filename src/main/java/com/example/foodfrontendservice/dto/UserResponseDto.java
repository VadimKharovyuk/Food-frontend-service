package com.example.foodfrontendservice.dto;

import com.example.foodfrontendservice.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;

    private UserRole userRole;
    private String roleDisplayName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}