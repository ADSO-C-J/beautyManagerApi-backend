package com.beautyManager.beautyManagerApi.dto.paymentDto;

import com.beautyManager.beautyManagerApi.enums.PaymentMethod;
import com.beautyManager.beautyManagerApi.enums.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentUpdateDTO {

    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "El monto no debe tener más de 2 decimales")
    private BigDecimal amount;

    private PaymentMethod method;

    @NotNull(message = "El estado del pago es obligatorio")
    private PaymentStatus status;

    private String reference;

    private String notes;
}