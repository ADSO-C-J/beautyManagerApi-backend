package com.beautyManager.beautyManagerApi.controller;

import com.beautyManager.beautyManagerApi.dto.auth.AuthResponseDTO;
import com.beautyManager.beautyManagerApi.dto.auth.LoginRequestDTO;
import com.beautyManager.beautyManagerApi.dto.auth.RegisterRequestDTO;
import com.beautyManager.beautyManagerApi.dto.auth.RegisterResponseDTO;
import com.beautyManager.beautyManagerApi.dto.auth.UserSummaryDTO;
import com.beautyManager.beautyManagerApi.service.authService.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Registro, login y datos del usuario autenticado")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Iniciar sesión", description = "Autentica con email y contraseña y devuelve un token JWT.")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @Operation(summary = "Registrar usuario", description = "Crea un usuario con rol 'cliente' y devuelve sus datos.")
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(dto));
    }

    /**
     * Endpoint protegido: requiere un JWT válido.
     * Devuelve los datos del usuario autenticado.
     */
    @Operation(summary = "Obtener mi perfil", description = "Requiere autenticación. Devuelve los datos del usuario del token.")
    @GetMapping("/profile")
    public ResponseEntity<UserSummaryDTO> profile(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(authService.getCurrentUser(principal.getUsername()));
    }
}