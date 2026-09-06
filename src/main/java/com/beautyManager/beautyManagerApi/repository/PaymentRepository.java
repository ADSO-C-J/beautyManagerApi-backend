package com.beautyManager.beautyManagerApi.repository;

import com.beautyManager.beautyManagerApi.entity.PaymentEntity;
import com.beautyManager.beautyManagerApi.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {

    List<PaymentEntity> findAllByAppointmentId(UUID appointmentId);

    List<PaymentEntity> findAllByStatus(PaymentStatus status);

    Optional<PaymentEntity> findByAppointmentId(UUID appointmentId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentEntity p WHERE p.status = 'pagado'")
    BigDecimal sumTotalPagado();

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0) FROM PaymentEntity p
            WHERE p.status = 'pagado' AND p.appointmentId IN :appointmentIds
            """)
    BigDecimal sumPagadoByAppointmentIds(@Param("appointmentIds") List<UUID> appointmentIds);
}