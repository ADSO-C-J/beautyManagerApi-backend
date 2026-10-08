package com.beautyManager.beautyManagerApi.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Petición de renovación de sesión: el cliente envía el refresh token emitido
 * en
 * el login para obtener un nuevo JWT sin volver a introducir credenciales.
 */
@Data
public class RefreshRequestDTO {

    @NotBlank(message = "El refresh token es obligatorio")
    private String refreshToken;
}
