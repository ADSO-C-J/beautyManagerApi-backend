package com.beautyManager.beautyManagerApi.dto.serviceDto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ServiceResponseDTO {
    private UUID id;
    private String name;
    private Integer duration_min;
    private BigDecimal price;
    private String description;
    // Enum TypeServices serializado en minúsculas (cabello, manos, pies, caballeros, facial, otro)
    private String category;
    private Boolean is_popular;
}
