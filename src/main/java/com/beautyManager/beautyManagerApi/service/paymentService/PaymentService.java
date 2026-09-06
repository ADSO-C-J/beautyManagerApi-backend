package com.beautyManager.beautyManagerApi.service.paymentService;

import com.beautyManager.beautyManagerApi.dto.paymentDto.PaymentRequestDTO;
import com.beautyManager.beautyManagerApi.dto.paymentDto.PaymentResponseDTO;
import com.beautyManager.beautyManagerApi.dto.paymentDto.PaymentUpdateDTO;
import com.beautyManager.beautyManagerApi.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PaymentService {

    List<PaymentResponseDTO> findAll();

    PaymentResponseDTO findById(UUID id);

    List<PaymentResponseDTO> findByAppointmentId(UUID appointmentId);

    List<PaymentResponseDTO> findByStatus(PaymentStatus status);

    PaymentResponseDTO create(PaymentRequestDTO dto, UUID createdBy);

    PaymentResponseDTO update(UUID id, PaymentUpdateDTO dto);

    void delete(UUID id);

    BigDecimal sumTotalPagado();

    UUID findCreatedBy(String email);
}