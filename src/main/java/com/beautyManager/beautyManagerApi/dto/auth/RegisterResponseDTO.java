package com.beautyManager.beautyManagerApi.dto.auth;

import com.beautyManager.beautyManagerApi.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Respuesta de registro (coincide con el RegisterApiResponse del frontend:
 * id, name, email, role).
 */
@Data
@Builder
public class RegisterResponseDTO {
    private UUID id;
    private String name;
    private String email;
    private UserRole role;
}