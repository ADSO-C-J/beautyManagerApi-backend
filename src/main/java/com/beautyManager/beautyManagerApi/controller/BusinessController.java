package com.beautyManager.beautyManagerApi.controller;

import com.beautyManager.beautyManagerApi.dto.config.BusinessRequestDTO;
import com.beautyManager.beautyManagerApi.dto.config.BusinessResponseDTO;
import com.beautyManager.beautyManagerApi.service.businessService.BusinessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/business")
@RequiredArgsConstructor
@Tag(name = "Configuración", description = "Configuración del negocio, horarios y notificaciones (protegido con JWT)")
public class BusinessController {

    private final BusinessService businessService;

    @Operation(summary = "Obtener configuración del negocio")
    @GetMapping
    public ResponseEntity<BusinessResponseDTO> get() {
        return ResponseEntity.ok(businessService.get());
    }

    @Operation(summary = "Actualizar configuración del negocio")
    @PutMapping
    public ResponseEntity<BusinessResponseDTO> update(@Valid @RequestBody BusinessRequestDTO dto) {
        return ResponseEntity.ok(businessService.update(dto));
    }
}