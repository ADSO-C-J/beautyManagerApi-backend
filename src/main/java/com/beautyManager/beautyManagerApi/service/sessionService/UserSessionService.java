package com.beautyManager.beautyManagerApi.service.sessionService;

import com.beautyManager.beautyManagerApi.dto.sessionDto.SessionResponseDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface UserSessionService {

    /**
     * Registra una nueva sesión a partir de un refresh token en claro.
     * Devuelve el DTO de la sesión creada.
     */
    SessionResponseDTO createSession(UUID userId, String rawRefreshToken, LocalDateTime expiresAt,
                                     String ipAddress, String userAgent);

    /** Sesiones activas (no expiradas) del usuario autenticado. */
    List<SessionResponseDTO> findMySessions(String email);

    /** Revoca (elimina) una sesión propia. Devuelve true si existía. */
    boolean revokeSession(UUID sessionId, String email);

    /** Revoca todas las sesiones del usuario (logout en todos los dispositivos). */
    long revokeAllMySessions(String email);
}
