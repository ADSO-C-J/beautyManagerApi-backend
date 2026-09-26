package com.beautyManager.beautyManagerApi.config;

import com.beautyManager.beautyManagerApi.service.tokenService.RevokedTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Purga periódicamente los tokens JWT revocados que ya expiraron.
 *
 * Un token revocado solo necesita guardarse hasta su fecha de expiración; a
 * partir de ahí el propio JWT caduca y la fila deja de aportar nada. Sin esta
 * limpieza la tabla revoked_tokens crecería indefinidamente.
 *
 * Se puede desactivar con app.jwt.revoked-token-purge.enabled=false.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        name = "app.jwt.revoked-token-purge.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class RevokedTokenCleanupTask {

    private final RevokedTokenService revokedTokenService;

    /**
     * Se ejecuta cada hora por defecto (expresiones cron de Spring).
     * Configurable con app.jwt.revoked-token-purge.cron.
     */
    @Scheduled(cron = "${app.jwt.revoked-token-purge.cron:0 0 * * * *}")
    public void purgeExpiredTokens() {
        try {
            long removed = revokedTokenService.purgeExpired();
            if (removed > 0) {
                log.info("Purga de tokens revocados: {} registro(s) eliminado(s)", removed);
            }
        } catch (Exception ex) {
            // Una purga fallida no debe tumbar la aplicación ni el scheduler.
            log.error("Error al purgar tokens revocados: {}", ex.getMessage(), ex);
        }
    }
}