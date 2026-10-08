package com.beautyManager.beautyManagerApi.service.authService;

import com.beautyManager.beautyManagerApi.dto.auth.AuthResponseDTO;
import com.beautyManager.beautyManagerApi.dto.auth.LoginRequestDTO;
import com.beautyManager.beautyManagerApi.dto.auth.RefreshRequestDTO;
import com.beautyManager.beautyManagerApi.dto.auth.RegisterRequestDTO;
import com.beautyManager.beautyManagerApi.dto.auth.RegisterResponseDTO;
import com.beautyManager.beautyManagerApi.dto.auth.UserSummaryDTO;

public interface AuthService {
    AuthResponseDTO login(LoginRequestDTO dto, String ipAddress, String userAgent);
    RegisterResponseDTO register(RegisterRequestDTO dto);
    UserSummaryDTO getCurrentUser(String email);

    /**
     * Renueva la sesión a partir de un refresh token válido.
     * Rota el refresh token (invalida el anterior) y emite un nuevo JWT.
     */
    AuthResponseDTO refresh(RefreshRequestDTO dto, String ipAddress, String userAgent);

    /**
     * Cierra la sesión del token indicado, revocándolo para que deje de ser válido.
     *
     * @param authorizationHeader header "Authorization: Bearer <token>"
     * @return true si el token quedó revocado
     */
    boolean logout(String authorizationHeader);
}