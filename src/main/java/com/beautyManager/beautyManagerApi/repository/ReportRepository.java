package com.beautyManager.beautyManagerApi.repository;

import com.beautyManager.beautyManagerApi.entity.StaffPerformanceViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Consultas de solo lectura para el módulo de reportes.
 * Los agregados por estado/estilista se resuelven con derived queries en los
 * repositorios que mapean cada entidad (AppointmentRepository, ClientRepository,
 * PaymentRepository y AppointmentServiceRepository).
 */
@Repository
public interface ReportRepository extends JpaRepository<StaffPerformanceViewEntity, UUID> {

    List<StaffPerformanceViewEntity> findAllByOrderByTotalRevenueDesc();
}