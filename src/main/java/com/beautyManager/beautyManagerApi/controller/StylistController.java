package com.beautyManager.beautyManagerApi.controller;

import com.beautyManager.beautyManagerApi.dto.*;
import com.beautyManager.beautyManagerApi.service.stylistService.StylistService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stylists")
@RequiredArgsConstructor
@Tag(name = "Estilistas", description = "Listado de estilistas del salón (protegido con JWT)")
public class StylistController {

    private final StylistService stylistService;

    @GetMapping
    public ResponseEntity<List<StylistResponseDTO>> findAll() {
        return ResponseEntity.ok(stylistService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StylistResponseDTO> findById(@PathVariable UUID id){
        return ResponseEntity.ok(stylistService.findById(id));
    }

    @PostMapping
    public ResponseEntity<StylistResponseDTO> create(@Valid @RequestBody CreateStylistRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stylistService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StylistResponseDTO> update(@PathVariable UUID id,
                                                    @Valid @RequestBody UpdateStylistRequestDTO dto){
        return ResponseEntity.ok(stylistService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        stylistService.delete(id);
        return ResponseEntity.noContent().build();
    }

}


