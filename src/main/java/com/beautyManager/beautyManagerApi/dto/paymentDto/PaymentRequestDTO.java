package com.beautyManager.beautyManagerApi.dto.paymentDto;

import com.beautyManager.beautyManagerApi.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentRequestDTO {

    @NotNull(message = "El appointment ID es obligatorio")
    private UUID appointmentId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "El monto no debe tener más de 2 decimales")
    private BigDecimal amount;

    @NotNull(message = "El método de pago es obligatorio")
    private PaymentMethod method;

    private String reference;

    private String notes;
}