package com.beautyManager.beautyManagerApi.dto.reportDto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Métricas clave del negocio para el dashboard de reportes.
 */
@Data
@Builder
public class ReportMetricsDTO {

    private BigDecimal totalRevenue;
    private Long totalAppointments;
    private Long newClients;
    private Double cancellationRate;
}