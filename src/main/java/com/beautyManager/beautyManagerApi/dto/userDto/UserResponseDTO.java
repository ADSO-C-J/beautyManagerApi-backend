package com.beautyManager.beautyManagerApi.dto;

import com.beautyManager.beautyManagerApi.enums.UserRole;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserResponseDTO {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String avatarUrl;
    private UserRole role;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
