package com.beautyManager.beautyManagerApi.controller;

import com.beautyManager.beautyManagerApi.dto.AppointmentResponseDTO;
import com.beautyManager.beautyManagerApi.dto.CreateAppointmentRequestDTO;
import com.beautyManager.beautyManagerApi.service.appointmentService.AppointmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import com.beautyManager.beautyManagerApi.dto.UpdateAppointmentRequestDTO;
import java.util.UUID;


@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "Citas", description = "Consulta por rango de fechas y creación de citas (protegido con JWT)")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    public ResponseEntity<List<AppointmentResponseDTO>> findAll(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo) {
        return ResponseEntity.ok(appointmentService.findAll(dateFrom, dateTo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> update(
            @PathVariable UUID id, @RequestBody UpdateAppointmentRequestDTO dto) {
        return ResponseEntity.ok(appointmentService.update(id,dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        appointmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> create(@RequestBody CreateAppointmentRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.create(dto));
    }
}