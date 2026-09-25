package com.beautyManager.beautyManagerApi.controller;
// controller/FacialAnalysisController.java

import com.beautyManager.beautyManagerApi.dto.facialDto.*;
import com.beautyManager.beautyManagerApi.dto.facialDto.FacialAnalysisResponseDTO;
import com.beautyManager.beautyManagerApi.service.facialService.FacialAnalysisService;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients/{clientId}/facial-analyses")
@RequiredArgsConstructor
public class FacialAnalysisController {

    private final FacialAnalysisService service;

    @GetMapping
    public ResponseEntity<List<FacialAnalysisResponseDTO>> findByClient(@PathVariable UUID clientId) {
        return ResponseEntity.ok(service.findByClient(clientId));
    }

    @PostMapping
    public ResponseEntity<FacialAnalysisResponseDTO> create(
            @PathVariable UUID clientId,
            @Valid @RequestBody FacialAnalysisRequestDTO dto) {
        FacialAnalysisResponseDTO created = service.create(clientId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacialAnalysisResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/recommendations")
    public ResponseEntity<FacialRecommendationResponseDTO> addRecommendation(
            @PathVariable UUID id,
            @Valid @RequestBody FacialRecommendationRequestDTO dto) {
        FacialRecommendationResponseDTO created = service.addRecommendation(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/recommendations/{recommendationId}")
    public ResponseEntity<Void> deleteRecommendation(@PathVariable UUID recommendationId) {
        // requiere un método delete(UUID) en FacialRecommendationRepository/Service — ver Errores Comunes
        return ResponseEntity.noContent().build();
    }
}
