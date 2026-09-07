package com.beautyManager.beautyManagerApi.repository;

import com.beautyManager.beautyManagerApi.entity.UserSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSessionEntity, UUID> {

    List<UserSessionEntity> findAllByUserIdAndExpiresAtAfterOrderByCreatedAtDesc(UUID userId, LocalDateTime now);

    Optional<UserSessionEntity> findByIdAndUserId(UUID id, UUID userId);

    long deleteByUserId(UUID userId);
}
