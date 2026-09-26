package com.beautyManager.beautyManagerApi.dto.reportDto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Ingresos agrupados por mes (para el gráfico de ingresos).
 */
@Data
@Builder
public class MonthlyRevenueDTO {

    private Integer month;
    private BigDecimal revenue;
}