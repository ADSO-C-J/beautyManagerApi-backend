package com.beautyManager.beautyManagerApi.dto.reportDto;

import com.beautyManager.beautyManagerApi.enums.TypeServices;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Popularidad de un servicio: cuántas veces se ha agendado y cuánto ha generado.
 */
@Data
@Builder
public class ServicePopularityDTO {

    private String serviceName;
    private TypeServices category;
    private Long bookingCount;
    private BigDecimal revenue;
}