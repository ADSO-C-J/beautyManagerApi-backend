package com.beautyManager.beautyManagerApi.controller;

import com.beautyManager.beautyManagerApi.dto.UserRequestDTO;
import com.beautyManager.beautyManagerApi.dto.UserResponseDTO;
import com.beautyManager.beautyManagerApi.dto.serviceDto.ServiceRequestDTO;
import com.beautyManager.beautyManagerApi.dto.serviceDto.ServiceResponseDTO;
import com.beautyManager.beautyManagerApi.service.serviceService.ServiceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@Tag(name = "Servicios", description = "Consulta y creación de servicios del salón (protegido con JWT)")
public class ServiceController {
    private final ServiceService serviceService;

    // GET /api/services
    @GetMapping
    public ResponseEntity<List<ServiceResponseDTO>> findAll() {
        return ResponseEntity.ok(serviceService.findAll());
    }

    // GET /api/services/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceService.findById(id));
    }

    // POST /api/serivces
    @PostMapping
    public ResponseEntity<ServiceResponseDTO> create(@Valid @RequestBody ServiceRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceService.create(dto));
    }

}
