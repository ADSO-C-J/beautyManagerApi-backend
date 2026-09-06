package com.beautyManager.beautyManagerApi.dto.staffDto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class StaffRequestDTO {

    @NotNull(message = "El user ID es obligatorio")
    private UUID userId;

    @NotNull(message = "El business ID es obligatorio")
    private UUID businessId;

    @Size(max = 100, message = "La especialidad no debe exceder 100 caracteres")
    private String specialty;

    @Size(max = 1000, message = "La bio no debe exceder 1000 caracteres")
    private String bio;

    private LocalDate hireDate;

    @NotNull(message = "El porcentaje de comisión es obligatorio")
    @Digits(integer = 3, fraction = 2, message = "El porcentaje de comisión no debe tener más de 2 decimales")
    @DecimalMin(value = "0", message = "El porcentaje de comisión no puede ser negativo")
    private BigDecimal commissionPct;

    private Boolean isActive;
}
