package com.beautyManager.beautyManagerApi.service.sessionService;

import com.beautyManager.beautyManagerApi.dto.sessionDto.SessionResponseDTO;
import com.beautyManager.beautyManagerApi.entity.UserSessionEntity;
import com.beautyManager.beautyManagerApi.exception.ResourceNotFoundException;
import com.beautyManager.beautyManagerApi.repository.UserRepository;
import com.beautyManager.beautyManagerApi.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserSessionServiceImpl implements UserSessionService {

    private final UserSessionRepository userSessionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public SessionResponseDTO createSession(UUID userId, String rawRefreshToken, LocalDateTime expiresAt,
                                            String ipAddress, String userAgent) {
        UserSessionEntity session = UserSessionEntity.builder()
                .userId(userId)
                .refreshToken(sha256(rawRefreshToken))
                .expiresAt(expiresAt)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
        return toDTO(userSessionRepository.save(session));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponseDTO> findMySessions(String email) {
        UUID userId = resolveUserId(email);
        return userSessionRepository
                .findAllByUserIdAndExpiresAtAfterOrderByCreatedAtDesc(userId, LocalDateTime.now())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean revokeSession(UUID sessionId, String email) {
        UUID userId = resolveUserId(email);
        UserSessionEntity session = userSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión no encontrada con id: " + sessionId));
        userSessionRepository.delete(session);
        return true;
    }

    @Override
    @Transactional
    public long revokeAllMySessions(String email) {
        UUID userId = resolveUserId(email);
        return userSessionRepository.deleteByUserId(userId);
    }

    private UUID resolveUserId(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .map(com.beautyManager.beautyManagerApi.entity.User::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
    }

    /** Hash SHA-256 en hexadecimal. */
    private String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    private SessionResponseDTO toDTO(UserSessionEntity s) {
        return SessionResponseDTO.builder()
                .id(s.getId())
                .userId(s.getUserId())
                .ipAddress(s.getIpAddress())
                .userAgent(s.getUserAgent())
                .createdAt(s.getCreatedAt())
                .expiresAt(s.getExpiresAt())
                .expired(s.getExpiresAt() != null && s.getExpiresAt().isBefore(LocalDateTime.now()))
                .build();
    }
}
