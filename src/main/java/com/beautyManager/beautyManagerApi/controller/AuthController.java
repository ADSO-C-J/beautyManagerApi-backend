package com.beautyManager.beautyManagerApi.controller;

import com.beautyManager.beautyManagerApi.dto.auth.AuthResponseDTO;
import com.beautyManager.beautyManagerApi.dto.auth.LoginRequestDTO;
import com.beautyManager.beautyManagerApi.dto.auth.RegisterRequestDTO;
import com.beautyManager.beautyManagerApi.dto.auth.RegisterResponseDTO;
import com.beautyManager.beautyManagerApi.dto.auth.UserSummaryDTO;
import com.beautyManager.beautyManagerApi.service.authService.AuthService;
import com.beautyManager.beautyManagerApi.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Registro, login, logout y datos del usuario autenticado")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @Operation(summary = "Iniciar sesión", description = "Autentica con email y contraseña y devuelve un token JWT. Registra la sesión (IP, user-agent) y devuelve un refresh token.")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto,
                                                 jakarta.servlet.http.HttpServletRequest request) {
        return ResponseEntity.ok(authService.login(dto, request.getRemoteAddr(), request.getHeader("User-Agent")));
    }

    @Operation(summary = "Registrar usuario", description = "Crea un usuario con rol 'cliente' y devuelve sus datos.")
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(dto));
    }

    @Operation(summary = "Cerrar sesión", description = "Revoca el token JWT actual para que deje de ser válido aunque no haya expirado. Requiere autenticación.")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        boolean revoked = authService.logout(authorizationHeader);
        return ResponseEntity.ok(Map.of("revoked", revoked));
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