package com.beautyManager.beautyManagerApi.dto.auth;

import com.beautyManager.beautyManagerApi.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Usuario que se devuelve junto al token de login.
 */
@Data
@Builder
public class UserSummaryDTO {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String avatarUrl;
    private UserRole role;
}