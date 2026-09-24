package com.beautyManager.beautyManagerApi.controller;

import com.beautyManager.beautyManagerApi.dto.auditLogDto.AuditLogPageDTO;
import com.beautyManager.beautyManagerApi.dto.auditLogDto.AuditLogResponseDTO;
import com.beautyManager.beautyManagerApi.service.auditLogService.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Auditoría", description = "Consulta del historial de cambios del sistema (solo lectura, protegido con JWT)")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @Operation(summary = "Listar registros de auditoría (paginado y filtrable)")
    @GetMapping
    public ResponseEntity<AuditLogPageDTO> findAll(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String tableName,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(auditLogService.findAll(userId, tableName, action, page, size));
    }

    @Operation(summary = "Obtener un registro de auditoría por ID")
    @GetMapping("/{id}")
    public ResponseEntity<AuditLogResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(auditLogService.findById(id));
    }

    @Operation(summary = "Tablas auditadas disponibles (para filtros)")
    @GetMapping("/table-names")
    public ResponseEntity<List<String>> findTableNames() {
        return ResponseEntity.ok(auditLogService.findTableNames());
    }

    @Operation(summary = "Acciones auditadas disponibles (para filtros)")
    @GetMapping("/actions")
    public ResponseEntity<List<String>> findActions() {
        return ResponseEntity.ok(auditLogService.findActions());
    }
}