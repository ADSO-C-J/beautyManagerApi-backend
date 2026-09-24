package com.beautyManager.beautyManagerApi.controller;

import com.beautyManager.beautyManagerApi.dto.notificationDto.NotificationRequestDTO;
import com.beautyManager.beautyManagerApi.dto.notificationDto.NotificationResponseDTO;
import com.beautyManager.beautyManagerApi.service.notificationService.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Bandeja de notificaciones del usuario autenticado (protegido con JWT)")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Mis notificaciones")
    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> findMine(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(notificationService.findMyNotifications(principal.getUsername()));
    }

    @Operation(summary = "Mis notificaciones no leídas")
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponseDTO>> findMineUnread(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(notificationService.findMyUnreadNotifications(principal.getUsername()));
    }

    @Operation(summary = "Número de notificaciones no leídas")
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> countUnread(
            @AuthenticationPrincipal UserDetails principal) {
        long count = notificationService.countMyUnread(principal.getUsername());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @Operation(summary = "Marcar una notificación como leída")
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(notificationService.markAsRead(id, principal.getUsername()));
    }

    @Operation(summary = "Marcar todas mis notificaciones como leídas")
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Long>> markAllAsRead(
            @AuthenticationPrincipal UserDetails principal) {
        long updated = notificationService.markAllAsRead(principal.getUsername());
        return ResponseEntity.ok(Map.of("updated", updated));
    }

    @Operation(summary = "Crear una notificación")
    @PostMapping
    public ResponseEntity<NotificationResponseDTO> create(
            @Valid @RequestBody NotificationRequestDTO dto,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.create(dto, principal.getUsername()));
    }

    @Operation(summary = "Eliminar una notificación")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal) {
        notificationService.delete(id, principal.getUsername());
        return ResponseEntity.noContent().build();
    }
}