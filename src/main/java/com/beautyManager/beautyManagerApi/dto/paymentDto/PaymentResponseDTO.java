package com.beautyManager.beautyManagerApi.dto.paymentDto;

import com.beautyManager.beautyManagerApi.enums.PaymentMethod;
import com.beautyManager.beautyManagerApi.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PaymentResponseDTO {

    private UUID id;
    private UUID appointmentId;
    private BigDecimal amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String reference;
    private String notes;
    private UUID createdBy;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}