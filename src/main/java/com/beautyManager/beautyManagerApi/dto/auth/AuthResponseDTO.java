package com.beautyManager.beautyManagerApi.dto.auth;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Respuesta de login: token JWT + datos del usuario autenticado.
 * El frontend lee las claves `token`, `refresh_token` y `expires_in`
 * (estas dos últimas son opcionales; aquí se envían en camelCase).
 */
@Data
@Builder
public class AuthResponseDTO {
    private String token;
    private String refreshToken;
    private Long expiresIn;
    private String tokenType;
    private UserSummaryDTO user;
    private List<String> roles;
}