package com.beautyManager.beautyManagerApi.controller;

import com.beautyManager.beautyManagerApi.dto.staffDto.StaffRequestDTO;
import com.beautyManager.beautyManagerApi.dto.staffDto.StaffResponseDTO;
import com.beautyManager.beautyManagerApi.dto.staffDto.StaffUpdateDTO;
import com.beautyManager.beautyManagerApi.service.staffService.StaffService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@Tag(name = "Personal", description = "CRUD del personal del salón (protegido con JWT)")
public class StaffController {

    private final StaffService staffService;

    @GetMapping
    public ResponseEntity<List<StaffResponseDTO>> findAll() {
        return ResponseEntity.ok(staffService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StaffResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(staffService.findById(id));
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<List<StaffResponseDTO>> findByBusiness(@PathVariable UUID businessId) {
        return ResponseEntity.ok(staffService.findByBusiness(businessId));
    }

    @PostMapping
    public ResponseEntity<StaffResponseDTO> create(@Valid @RequestBody StaffRequestDTO dto) {
        StaffResponseDTO created = staffService.create(dto);
        return ResponseEntity.created(URI.create("/api/staff/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StaffResponseDTO> update(@PathVariable UUID id,
                                                   @Valid @RequestBody StaffUpdateDTO dto) {
        return ResponseEntity.ok(staffService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        staffService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
