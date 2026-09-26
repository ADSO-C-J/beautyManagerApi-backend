package com.beautyManager.beautyManagerApi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Mapea la vista v_staff_performance (solo lectura).
 * Expone métricas agregadas por estilista: citas completadas, ingresos y rating.
 */
@Entity
@Table(name = "v_staff_performance")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffPerformanceViewEntity {

    @Id
    @Column(name = "staff_id")
    private UUID staffId;

    @Column(name = "staff_name")
    private String staffName;

    private String specialty;

    @Column(name = "total_appointments")
    private Long totalAppointments;

    @Column(name = "total_revenue")
    private BigDecimal totalRevenue;

    @Column(name = "avg_rating", columnDefinition = "numeric")
    private BigDecimal avgRating;

    @Column(name = "total_reviews")
    private Long totalReviews;
}