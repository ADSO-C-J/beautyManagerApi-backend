package com.beautyManager.beautyManagerApi.dto;

import com.beautyManager.beautyManagerApi.enums.TypeServices;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Servicio incluido en una cita, con el snapshot de precio y duración
 * capturado al momento de agendar (tabla appointment_services).
 */
@Data
@Builder
public class AppointmentServiceItemDTO {
    private UUID id;
    private UUID serviceId;
    private String name;
    private TypeServices category;
    private BigDecimal priceAtTime;
    private Integer durationAtTime;
}