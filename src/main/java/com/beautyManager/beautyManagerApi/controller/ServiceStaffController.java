package com.beautyManager.beautyManagerApi.controller;

import com.beautyManager.beautyManagerApi.dto.staffServiceDto.StaffServiceResponseDTO;
import com.beautyManager.beautyManagerApi.service.staffServiceService.StaffServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/services/{serviceId}/staff")
@RequiredArgsConstructor
@Tag(name = "Personal por servicio", description = "Consulta de miembros del personal que ofrecen un servicio (protegido con JWT)")
public class ServiceStaffController {

    private final StaffServiceService staffServiceService;

    @Operation(summary = "Miembros del personal que ofrecen un servicio")
    @GetMapping
    public ResponseEntity<List<StaffServiceResponseDTO>> findByService(@PathVariable UUID serviceId) {
        return ResponseEntity.ok(staffServiceService.findByService(serviceId));
    }
}