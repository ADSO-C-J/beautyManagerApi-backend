package com.beautyManager.beautyManagerApi.dto.serviceDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ServiceRequestDTO {

    @NotNull(message = "El business ID es obligatorio")
    private UUID businessId;

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    private String description;

    @NotBlank(message = "Debes seleccionar una categoria")
    private String category;

    @NotNull(message = "La duracion del servicio es obligatoria")
    @Positive(message = "La duracion debe ser un valor positivo")
    private Integer duration_min;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser un valor positivo")
    private BigDecimal price;
}
