package com.beautyManager.beautyManagerApi.repository;

import com.beautyManager.beautyManagerApi.entity.AppointmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {
    List<AppointmentEntity> findAllByBusinessIdAndDeletedAtIsNull(UUID businessId);
    List<AppointmentEntity> findAllByBusinessIdAndDeletedAtIsNullAndScheduledAtBetween(
            UUID businessId, LocalDateTime start, LocalDateTime end);
}