package com.beautyManager.beautyManagerApi.dto.reportDto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Rendimiento por estilista (vista v_staff_performance).
 */
@Data
@Builder
public class StaffPerformanceDTO {

    private UUID staffId;
    private String staffName;
    private String specialty;
    private Long totalAppointments;
    private BigDecimal totalRevenue;
    private BigDecimal avgRating;
    private Long totalReviews;
}