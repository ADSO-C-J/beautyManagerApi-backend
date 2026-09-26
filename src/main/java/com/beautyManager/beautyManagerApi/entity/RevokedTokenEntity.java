package com.beautyManager.beautyManagerApi.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Token JWT revocado (logout). Se guarda el identificador único del token
 * (claim 'jti') hasta que expira; después la fila puede purgarse.
 */
@Entity
@Table(name = "revoked_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevokedTokenEntity {

    /** Identificador único del JWT (claim 'jti'). */
    @Id
    @Column(name = "jti")
    private String jti;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "revoked_at", nullable = false, updatable = false)
    private OffsetDateTime revokedAt;
}