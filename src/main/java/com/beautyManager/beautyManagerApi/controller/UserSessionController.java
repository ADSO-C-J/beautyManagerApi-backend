package com.beautyManager.beautyManagerApi.controller;

import com.beautyManager.beautyManagerApi.dto.sessionDto.SessionResponseDTO;
import com.beautyManager.beautyManagerApi.service.sessionService.UserSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Tag(name = "Sesiones", description = "Gestión de sesiones activas del usuario autenticado (protegido con JWT)")
public class UserSessionController {

    private final UserSessionService userSessionService;

    @Operation(summary = "Mis sesiones activas")
    @GetMapping
    public ResponseEntity<List<SessionResponseDTO>> findMine(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(userSessionService.findMySessions(principal.getUsername()));
    }

    @Operation(summary = "Cerrar una sesión específica (dispositivo)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> revoke(@PathVariable UUID id,
                                                      @AuthenticationPrincipal UserDetails principal) {
        boolean revoked = userSessionService.revokeSession(id, principal.getUsername());
        return ResponseEntity.ok(Map.of("revoked", revoked));
    }

    @Operation(summary = "Cerrar todas mis sesiones (logout en todos los dispositivos)")
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> revokeAll(
            @AuthenticationPrincipal UserDetails principal) {
        long revoked = userSessionService.revokeAllMySessions(principal.getUsername());
        return ResponseEntity.ok(Map.of("revoked", revoked));
    }
}
