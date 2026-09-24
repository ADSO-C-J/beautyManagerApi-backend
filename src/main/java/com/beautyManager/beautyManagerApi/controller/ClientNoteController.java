package com.beautyManager.beautyManagerApi.controller;

import com.beautyManager.beautyManagerApi.dto.clientNoteDto.ClientNoteRequestDTO;
import com.beautyManager.beautyManagerApi.dto.clientNoteDto.ClientNoteResponseDTO;
import com.beautyManager.beautyManagerApi.dto.clientNoteDto.ClientNoteUpdateDTO;
import com.beautyManager.beautyManagerApi.service.clientNoteService.ClientNoteService;
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
@RequestMapping("/api/clients/{clientId}/notes")
@RequiredArgsConstructor
@Tag(name = "Notas de cliente", description = "Notas internas del equipo sobre los clientes (protegido con JWT)")
public class ClientNoteController {

    private final ClientNoteService clientNoteService;

    @Operation(summary = "Notas de un cliente")
    @GetMapping
    public ResponseEntity<List<ClientNoteResponseDTO>> findByClient(@PathVariable UUID clientId) {
        return ResponseEntity.ok(clientNoteService.findByClient(clientId));
    }

    @Operation(summary = "Obtener una nota por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ClientNoteResponseDTO> findById(@PathVariable UUID clientId, @PathVariable UUID id) {
        return ResponseEntity.ok(clientNoteService.findById(id));
    }

    @Operation(summary = "Crear una nota para un cliente")
    @PostMapping
    public ResponseEntity<ClientNoteResponseDTO> create(
            @PathVariable UUID clientId,
            @Valid @RequestBody ClientNoteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientNoteService.create(clientId, dto));
    }

    @Operation(summary = "Actualizar una nota")
    @PutMapping("/{id}")
    public ResponseEntity<ClientNoteResponseDTO> update(
            @PathVariable UUID clientId,
            @PathVariable UUID id,
            @Valid @RequestBody ClientNoteUpdateDTO dto) {
        return ResponseEntity.ok(clientNoteService.update(id, dto));
    }

    @Operation(summary = "Eliminar una nota (borrado lógico)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID clientId, @PathVariable UUID id) {
        clientNoteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}