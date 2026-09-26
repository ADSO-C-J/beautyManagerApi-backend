package com.beautyManager.beautyManagerApi.repository;

import com.beautyManager.beautyManagerApi.entity.RevokedTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedTokenEntity, String> {

    boolean existsByJti(String jti);

    /** Purga los tokens ya expirados (deja de ser necesario guardarlos). */
    long deleteByExpiresAtBefore(OffsetDateTime cutoff);

    long deleteByUserId(UUID userId);
}