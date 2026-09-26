package com.beautyManager.beautyManagerApi.service.tokenService;

import com.beautyManager.beautyManagerApi.entity.RevokedTokenEntity;
import com.beautyManager.beautyManagerApi.repository.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RevokedTokenServiceImpl implements RevokedTokenService {

    private final RevokedTokenRepository revokedTokenRepository;

    @Override
    @Transactional
    public void revoke(String jti, UUID userId, long expiresAtMillis) {
        if (jti == null || revokedTokenRepository.existsByJti(jti)) {
            return;
        }
        OffsetDateTime expiresAt = OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(expiresAtMillis), ZoneOffset.UTC);

        revokedTokenRepository.save(RevokedTokenEntity.builder()
                .jti(jti)
                .userId(userId)
                .expiresAt(expiresAt)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRevoked(String jti) {
        return jti != null && revokedTokenRepository.existsByJti(jti);
    }

    @Override
    @Transactional
    public long purgeExpired() {
        return revokedTokenRepository.deleteByExpiresAtBefore(OffsetDateTime.now(ZoneOffset.UTC));
    }
}