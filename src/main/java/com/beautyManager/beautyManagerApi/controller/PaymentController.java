package com.beautyManager.beautyManagerApi.controller;

import com.beautyManager.beautyManagerApi.dto.paymentDto.PaymentRequestDTO;
import com.beautyManager.beautyManagerApi.dto.paymentDto.PaymentResponseDTO;
import com.beautyManager.beautyManagerApi.dto.paymentDto.PaymentUpdateDTO;
import com.beautyManager.beautyManagerApi.enums.PaymentStatus;
import com.beautyManager.beautyManagerApi.service.paymentService.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Pagos", description = "Consulta, creación y actualización de pagos (protegido con JWT)")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Obtener todos los pagos")
    @GetMapping
    public ResponseEntity<List<PaymentResponseDTO>> findAll() {
        return ResponseEntity.ok(paymentService.findAll());
    }

    @Operation(summary = "Obtener un pago por ID")
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.findById(id));
    }

    @Operation(summary = "Obtener los pagos de una cita específica")
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<List<PaymentResponseDTO>> findByAppointmentId(@PathVariable UUID appointmentId) {
        return ResponseEntity.ok(paymentService.findByAppointmentId(appointmentId));
    }

    @Operation(summary = "Obtener pagos por estado")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentResponseDTO>> findByStatus(@PathVariable PaymentStatus status) {
        return ResponseEntity.ok(paymentService.findByStatus(status));
    }

    @Operation(summary = "Registrar un nuevo pago")
    @PostMapping
    public ResponseEntity<PaymentResponseDTO> create(
            @Valid @RequestBody PaymentRequestDTO dto,
            @AuthenticationPrincipal UserDetails principal) {
        UUID createdBy = principal != null
                ? paymentService.findCreatedBy(principal.getUsername())
                : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.create(dto, createdBy));
    }

    @Operation(summary = "Actualizar un pago")
    @PutMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> update(@PathVariable UUID id,
                                                     @Valid @RequestBody PaymentUpdateDTO dto) {
        return ResponseEntity.ok(paymentService.update(id, dto));
    }

    @Operation(summary = "Eliminar un pago")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        paymentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}