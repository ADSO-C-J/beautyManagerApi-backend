package com.beautyManager.beautyManagerApi.controller;

import com.beautyManager.beautyManagerApi.dto.staffScheduleDto.StaffScheduleRequestDTO;
import com.beautyManager.beautyManagerApi.dto.staffScheduleDto.StaffScheduleResponseDTO;
import com.beautyManager.beautyManagerApi.dto.staffScheduleDto.StaffScheduleUpdateDTO;
import com.beautyManager.beautyManagerApi.service.staffScheduleService.StaffScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/staff-schedules")
@RequiredArgsConstructor
@Tag(name = "Horarios del personal", description = "Gestión de horarios semanales del staff (protegido con JWT)")
public class StaffScheduleController {

    private final StaffScheduleService staffScheduleService;

    @Operation(summary = "Obtener todos los horarios")
    @GetMapping
    public ResponseEntity<List<StaffScheduleResponseDTO>> findAll() {
        return ResponseEntity.ok(staffScheduleService.findAll());
    }

    @Operation(summary = "Obtener los horarios de un staff")
    @GetMapping("/staff/{staffId}")
    public ResponseEntity<List<StaffScheduleResponseDTO>> findByStaffId(@PathVariable UUID staffId) {
        return ResponseEntity.ok(staffScheduleService.findByStaffId(staffId));
    }

    @Operation(summary = "Obtener un horario por ID")
    @GetMapping("/{id}")
    public ResponseEntity<StaffScheduleResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(staffScheduleService.findById(id));
    }

    @Operation(summary = "Crear un horario")
    @PostMapping
    public ResponseEntity<StaffScheduleResponseDTO> create(@Valid @RequestBody StaffScheduleRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(staffScheduleService.create(dto));
    }

    @Operation(summary = "Actualizar un horario")
    @PutMapping("/{id}")
    public ResponseEntity<StaffScheduleResponseDTO> update(@PathVariable UUID id,
                                                           @Valid @RequestBody StaffScheduleUpdateDTO dto) {
        return ResponseEntity.ok(staffScheduleService.update(id, dto));
    }

    @Operation(summary = "Eliminar un horario")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        staffScheduleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}