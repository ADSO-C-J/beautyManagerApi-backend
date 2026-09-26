package com.beautyManager.beautyManagerApi.repository;

import com.beautyManager.beautyManagerApi.entity.AppointmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {
    List<AppointmentEntity> findAllByBusinessIdAndDeletedAtIsNull(UUID businessId);
    List<AppointmentEntity> findAllByBusinessIdAndDeletedAtIsNullAndScheduledAtBetween(
            UUID businessId, LocalDateTime start, LocalDateTime end);
    Optional<AppointmentEntity> findByIdAndDeletedAtIsNull(UUID id);

    long countByBusinessIdAndDeletedAtIsNull(UUID businessId);

    /**
     * La columna status es un enum nativo de PostgreSQL (appointment_status),
     * por lo que se castea el parámetro para poder compararlo.
     */
    @Query(value = """
            SELECT COUNT(*) FROM appointments
            WHERE business_id = :businessId
              AND deleted_at IS NULL
              AND status = CAST(:status AS appointment_status)
            """, nativeQuery = true)
    long countByBusinessIdAndStatusNative(@Param("businessId") UUID businessId,
                                          @Param("status") String status);
}
