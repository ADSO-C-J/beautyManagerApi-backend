package com.beautyManager.beautyManagerApi.controller;

import com.beautyManager.beautyManagerApi.dto.reviewDto.RatingStatsDTO;
import com.beautyManager.beautyManagerApi.dto.reviewDto.ReviewRequestDTO;
import com.beautyManager.beautyManagerApi.dto.reviewDto.ReviewResponseDTO;
import com.beautyManager.beautyManagerApi.dto.reviewDto.ReviewUpdateDTO;
import com.beautyManager.beautyManagerApi.service.reviewService.ReviewService;
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
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reseñas", description = "Valoraciones de clientes sobre citas completadas (protegido con JWT)")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Obtener todas las reseñas")
    @GetMapping
    public ResponseEntity<List<ReviewResponseDTO>> findAll() {
        return ResponseEntity.ok(reviewService.findAll());
    }

    @Operation(summary = "Obtener una reseña por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(reviewService.findById(id));
    }

    @Operation(summary = "Obtener reseñas de una cita")
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<List<ReviewResponseDTO>> findByAppointmentId(@PathVariable UUID appointmentId) {
        return ResponseEntity.ok(reviewService.findByAppointmentId(appointmentId));
    }

    @Operation(summary = "Obtener reseñas de un cliente")
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ReviewResponseDTO>> findByClientId(@PathVariable UUID clientId) {
        return ResponseEntity.ok(reviewService.findByClientId(clientId));
    }

    @Operation(summary = "Obtener reseñas de un estilista")
    @GetMapping("/staff/{staffId}")
    public ResponseEntity<List<ReviewResponseDTO>> findByStaffId(@PathVariable UUID staffId) {
        return ResponseEntity.ok(reviewService.findByStaffId(staffId));
    }

    @Operation(summary = "Obtener promedio de rating por estilista")
    @GetMapping("/rating-stats")
    public ResponseEntity<List<RatingStatsDTO>> getRatingStats() {
        return ResponseEntity.ok(reviewService.getRatingStats());
    }

    @Operation(summary = "Crear una reseña")
    @PostMapping
    public ResponseEntity<ReviewResponseDTO> create(@Valid @RequestBody ReviewRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.create(dto));
    }

    @Operation(summary = "Actualizar una reseña")
    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> update(@PathVariable UUID id,
                                                    @Valid @RequestBody ReviewUpdateDTO dto) {
        return ResponseEntity.ok(reviewService.update(id, dto));
    }

    @Operation(summary = "Eliminar una reseña")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}