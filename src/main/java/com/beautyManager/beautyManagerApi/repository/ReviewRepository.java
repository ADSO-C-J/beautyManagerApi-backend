package com.beautyManager.beautyManagerApi.repository;

import com.beautyManager.beautyManagerApi.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID> {

    List<ReviewEntity> findAllByOrderByCreatedAtDesc();

    List<ReviewEntity> findAllByAppointmentId(UUID appointmentId);

    List<ReviewEntity> findAllByClientId(UUID clientId);

    List<ReviewEntity> findAllByStaffIdOrderByCreatedAtDesc(UUID staffId);

    Optional<ReviewEntity> findByAppointmentId(UUID appointmentId);
}