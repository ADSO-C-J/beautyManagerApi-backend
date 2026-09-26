package com.beautyManager.beautyManagerApi.service.tokenService;

import java.util.UUID;

public interface RevokedTokenService {

    /** Marca un token como revocado hasta su expiración. */
    void revoke(String jti, UUID userId, long expiresAtMillis);

    /** ¿Está este token en la lista de revocados? */
    boolean isRevoked(String jti);

    /** Elimina los registros ya expirados (tarea de limpieza). */
    long purgeExpired();
}