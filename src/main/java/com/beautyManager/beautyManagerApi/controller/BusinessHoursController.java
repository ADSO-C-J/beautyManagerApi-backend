package com.beautyManager.beautyManagerApi.controller;

import com.beautyManager.beautyManagerApi.dto.config.BusinessHoursRequestDTO;
import com.beautyManager.beautyManagerApi.dto.config.BusinessHoursResponseDTO;
import com.beautyManager.beautyManagerApi.service.businessHoursService.BusinessHoursService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/business/{businessId}/hours")
@RequiredArgsConstructor
@Tag(name = "Configuración", description = "Configuración del negocio, horarios y notificaciones (protegido con JWT)")
public class BusinessHoursController {

    private final BusinessHoursService businessHoursService;

    @Operation(summary = "Listar horarios del negocio")
    @GetMapping
    public ResponseEntity<List<BusinessHoursResponseDTO>> find(@PathVariable UUID businessId) {
        return ResponseEntity.ok(businessHoursService.findByBusinessId(businessId));
    }

    @Operation(summary = "Crear o actualizar horario de un día")
    @PutMapping
    public ResponseEntity<BusinessHoursResponseDTO> upsert(
            @PathVariable UUID businessId,
            @Valid @RequestBody BusinessHoursRequestDTO dto) {
        return ResponseEntity.ok(businessHoursService.upsert(businessId, dto));
    }
}