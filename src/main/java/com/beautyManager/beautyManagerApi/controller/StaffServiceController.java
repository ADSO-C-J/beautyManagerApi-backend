package com.beautyManager.beautyManagerApi.controller;

import com.beautyManager.beautyManagerApi.dto.serviceDto.ServiceResponseDTO;
import com.beautyManager.beautyManagerApi.dto.staffServiceDto.StaffServiceAssignRequestDTO;
import com.beautyManager.beautyManagerApi.dto.staffServiceDto.StaffServiceResponseDTO;
import com.beautyManager.beautyManagerApi.service.staffServiceService.StaffServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/staff/{staffId}/services")
@RequiredArgsConstructor
@Tag(name = "Servicios del personal", description = "Asignación de servicios a los miembros del personal (protegido con JWT)")
public class StaffServiceController {

    private final StaffServiceService staffServiceService;

    @Operation(summary = "Servicios asignados a un miembro del personal")
    @GetMapping
    public ResponseEntity<List<StaffServiceResponseDTO>> findByStaff(@PathVariable UUID staffId) {
        return ResponseEntity.ok(staffServiceService.findByStaff(staffId));
    }

    @Operation(summary = "Servicios disponibles (no asignados) para un miembro del personal")
    @GetMapping("/available")
    public ResponseEntity<List<ServiceResponseDTO>> findAvailableByStaff(@PathVariable UUID staffId) {
        return ResponseEntity.ok(staffServiceService.findAvailableByStaff(staffId));
    }

    @Operation(summary = "Asignar servicios a un miembro del personal")
    @PostMapping
    public ResponseEntity<List<StaffServiceResponseDTO>> assign(
            @PathVariable UUID staffId,
            @Valid @RequestBody StaffServiceAssignRequestDTO dto) {
        return ResponseEntity.ok(staffServiceService.assign(staffId, dto));
    }

    @Operation(summary = "Quitar un servicio de un miembro del personal")
    @DeleteMapping("/{serviceId}")
    public ResponseEntity<Void> unassign(@PathVariable UUID staffId, @PathVariable UUID serviceId) {
        staffServiceService.unassign(staffId, serviceId);
        return ResponseEntity.noContent().build();
    }
}