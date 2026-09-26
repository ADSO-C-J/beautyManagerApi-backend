package com.beautyManager.beautyManagerApi.controller;

import com.beautyManager.beautyManagerApi.dto.reportDto.MonthlyRevenueDTO;
import com.beautyManager.beautyManagerApi.dto.reportDto.ReportMetricsDTO;
import com.beautyManager.beautyManagerApi.dto.reportDto.ServicePopularityDTO;
import com.beautyManager.beautyManagerApi.dto.reportDto.StaffPerformanceDTO;
import com.beautyManager.beautyManagerApi.service.reportService.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Métricas y análisis del negocio (protegido con JWT)")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Métricas clave del negocio (ingresos, citas, clientes nuevos, cancelaciones)")
    @GetMapping("/metrics")
    public ResponseEntity<ReportMetricsDTO> getMetrics(
            @RequestParam(required = false, defaultValue = "month") String range) {
        return ResponseEntity.ok(reportService.getMetrics(range));
    }

    @Operation(summary = "Ingresos agrupados por mes")
    @GetMapping("/revenue/monthly")
    public ResponseEntity<List<MonthlyRevenueDTO>> getMonthlyRevenue() {
        return ResponseEntity.ok(reportService.getMonthlyRevenue());
    }

    @Operation(summary = "Servicios más solicitados")
    @GetMapping("/services/popularity")
    public ResponseEntity<List<ServicePopularityDTO>> getServicePopularity() {
        return ResponseEntity.ok(reportService.getServicePopularity());
    }

    @Operation(summary = "Desempeño por estilista")
    @GetMapping("/staff/performance")
    public ResponseEntity<List<StaffPerformanceDTO>> getStaffPerformance() {
        return ResponseEntity.ok(reportService.getStaffPerformance());
    }
}