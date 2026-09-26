package com.beautyManager.beautyManagerApi.service.reportService;

import com.beautyManager.beautyManagerApi.dto.reportDto.MonthlyRevenueDTO;
import com.beautyManager.beautyManagerApi.dto.reportDto.ReportMetricsDTO;
import com.beautyManager.beautyManagerApi.dto.reportDto.ServicePopularityDTO;
import com.beautyManager.beautyManagerApi.dto.reportDto.StaffPerformanceDTO;

import java.util.List;

public interface ReportService {

    /** Métricas clave del negocio (ingresos, citas, clientes nuevos, cancelaciones). */
    ReportMetricsDTO getMetrics(String range);

    /** Ingresos agrupados por mes. */
    List<MonthlyRevenueDTO> getMonthlyRevenue();

    /** Servicios más solicitados. */
    List<ServicePopularityDTO> getServicePopularity();

    /** Desempeño por estilista. */
    List<StaffPerformanceDTO> getStaffPerformance();
}