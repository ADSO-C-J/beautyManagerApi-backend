package com.beautyManager.beautyManagerApi.service.paymentService;

import com.beautyManager.beautyManagerApi.dto.paymentDto.PaymentRequestDTO;
import com.beautyManager.beautyManagerApi.dto.paymentDto.PaymentResponseDTO;
import com.beautyManager.beautyManagerApi.dto.paymentDto.PaymentUpdateDTO;
import com.beautyManager.beautyManagerApi.entity.AppointmentEntity;
import com.beautyManager.beautyManagerApi.entity.PaymentEntity;
import com.beautyManager.beautyManagerApi.entity.User;
import com.beautyManager.beautyManagerApi.enums.PaymentStatus;
import com.beautyManager.beautyManagerApi.exception.ResourceNotFoundException;
import com.beautyManager.beautyManagerApi.repository.AppointmentRepository;
import com.beautyManager.beautyManagerApi.repository.PaymentRepository;
import com.beautyManager.beautyManagerApi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> findAll() {
        return paymentRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDTO findById(UUID id) {
        PaymentEntity payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con id: " + id));
        return toDTO(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> findByAppointmentId(UUID appointmentId) {
        return paymentRepository.findAllByAppointmentId(appointmentId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> findByStatus(PaymentStatus status) {
        return paymentRepository.findAllByStatus(status)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PaymentResponseDTO create(PaymentRequestDTO dto, UUID createdBy) {
        ensureAppointmentExists(dto.getAppointmentId());
        ensureAppointmentNotAlreadyPaid(dto.getAppointmentId());

        PaymentEntity payment = PaymentEntity.builder()
                .appointmentId(dto.getAppointmentId())
                .amount(dto.getAmount())
                .method(dto.getMethod())
                .status(PaymentStatus.pendiente)
                .reference(dto.getReference())
                .notes(dto.getNotes())
                .createdBy(createdBy)
                .paidAt(null)
                .build();

        return toDTO(paymentRepository.save(payment));
    }
@Override
    @Transactional
    public PaymentResponseDTO update(UUID id, PaymentUpdateDTO dto) {
        PaymentEntity payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con id: " + id));

        if (PaymentStatus.pagado.equals(payment.getStatus()) && !PaymentStatus.pagado.equals(dto.getStatus())) {
            throw new IllegalArgumentException("No se puede revertir un pago ya registrado como pagado");
        }

        if (dto.getAmount() != null) {
            payment.setAmount(dto.getAmount());
        }
        if (dto.getMethod() != null) {
            payment.setMethod(dto.getMethod());
        }
        if (dto.getReference() != null) {
            payment.setReference(dto.getReference());
        }
        if (dto.getNotes() != null) {
            payment.setNotes(dto.getNotes());
        }

        handleStatusTransition(payment, dto.getStatus());
        payment.setUpdatedAt(LocalDateTime.now());

        return toDTO(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        PaymentEntity payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con id: " + id));

        if (PaymentStatus.pagado.equals(payment.getStatus()) || PaymentStatus.reembolsado.equals(payment.getStatus())) {
            throw new IllegalArgumentException("No se puede eliminar un pago ya registrado como pagado o reembolsado");
        }

        paymentRepository.delete(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal sumTotalPagado() {
        return paymentRepository.sumTotalPagado();
    }

@Override
    @Transactional(readOnly = true)
    public UUID findCreatedBy(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .map(User::getId)
                .orElse(null);
    }
    private void handleStatusTransition(PaymentEntity payment, PaymentStatus newStatus) {
        payment.setStatus(newStatus);

        if (PaymentStatus.pagado.equals(newStatus) && payment.getPaidAt() == null) {
            payment.setPaidAt(LocalDateTime.now());
        } else if (PaymentStatus.pendiente.equals(newStatus)) {
            payment.setPaidAt(null);
        }
    }

    private void ensureAppointmentExists(UUID appointmentId) {
        AppointmentEntity appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("La cita no existe con id: " + appointmentId));

        if (appointment.getDeletedAt() != null) {
            throw new ResourceNotFoundException("La cita no existe con id: " + appointmentId);
        }
    }

    private void ensureAppointmentNotAlreadyPaid(UUID appointmentId) {
        paymentRepository.findByAppointmentId(appointmentId)
                .filter(payment -> PaymentStatus.pagado.equals(payment.getStatus()) || PaymentStatus.reembolsado.equals(payment.getStatus()))
                .ifPresent(payment -> {
                    throw new IllegalArgumentException(
                            "La cita " + appointmentId + " ya tiene un pago registrado como " + payment.getStatus());
                });
    }

    private PaymentResponseDTO toDTO(PaymentEntity payment) {
        return PaymentResponseDTO.builder()
                .id(payment.getId())
                .appointmentId(payment.getAppointmentId())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .reference(payment.getReference())
                .notes(payment.getNotes())
                .createdBy(payment.getCreatedBy())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}