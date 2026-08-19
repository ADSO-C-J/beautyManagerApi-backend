package com.beautyManager.beautyManagerApi.controller;

import com.beautyManager.beautyManagerApi.dto.config.NotificationPreferenceDTO;
import com.beautyManager.beautyManagerApi.service.notificationPreferenceService.NotificationPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notification-preferences")
@RequiredArgsConstructor
@Tag(name = "Configuración", description = "Configuración del negocio, horarios y notificaciones (protegido con JWT)")
public class NotificationPreferenceController {

    private final NotificationPreferenceService preferenceService;

    @Operation(summary = "Obtener preferencias de notificación de un usuario")
    @GetMapping("/{userId}")
    public ResponseEntity<NotificationPreferenceDTO> getByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(preferenceService.getByUserId(userId));
    }

    @Operation(summary = "Actualizar preferencias de notificación de un usuario")
    @PutMapping("/{userId}")
    public ResponseEntity<NotificationPreferenceDTO> update(
            @PathVariable UUID userId,
            @RequestBody NotificationPreferenceDTO dto) {
        return ResponseEntity.ok(preferenceService.update(userId, dto));
    }
}