package com.beautyManager.beautyManagerApi.controller;

import com.beautyManager.beautyManagerApi.dto.clientPreferenceDto.ClientPreferenceRequestDTO;
import com.beautyManager.beautyManagerApi.dto.clientPreferenceDto.ClientPreferenceResponseDTO;
import com.beautyManager.beautyManagerApi.dto.clientPreferenceDto.ClientPreferenceUpdateDTO;
import com.beautyManager.beautyManagerApi.service.clientPreferenceService.ClientPreferenceService;
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
@RequestMapping("/api/clients/{clientId}/preferences")
@RequiredArgsConstructor
@Tag(name = "Preferencias de cliente", description = "Preferencias clave-valor de cada cliente (protegido con JWT)")
public class ClientPreferenceController {

    private final ClientPreferenceService clientPreferenceService;

    @Operation(summary = "Preferencias de un cliente")
    @GetMapping
    public ResponseEntity<List<ClientPreferenceResponseDTO>> findByClient(@PathVariable UUID clientId) {
        return ResponseEntity.ok(clientPreferenceService.findByClient(clientId));
    }

    @Operation(summary = "Obtener una preferencia por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ClientPreferenceResponseDTO> findById(@PathVariable UUID clientId, @PathVariable UUID id) {
        return ResponseEntity.ok(clientPreferenceService.findById(id));
    }

    @Operation(summary = "Crear una preferencia para un cliente")
    @PostMapping
    public ResponseEntity<ClientPreferenceResponseDTO> create(
            @PathVariable UUID clientId,
            @Valid @RequestBody ClientPreferenceRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientPreferenceService.create(clientId, dto));
    }

    @Operation(summary = "Actualizar el valor de una preferencia")
    @PutMapping("/{id}")
    public ResponseEntity<ClientPreferenceResponseDTO> update(
            @PathVariable UUID clientId,
            @PathVariable UUID id,
            @Valid @RequestBody ClientPreferenceUpdateDTO dto) {
        return ResponseEntity.ok(clientPreferenceService.update(id, dto));
    }

    @Operation(summary = "Eliminar una preferencia")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID clientId, @PathVariable UUID id) {
        clientPreferenceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}