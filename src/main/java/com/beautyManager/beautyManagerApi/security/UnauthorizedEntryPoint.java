package com.beautyManager.beautyManagerApi.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Punto de entrada de seguridad: responde con un JSON 401
 * cuando una petición protegida llega sin credenciales válidas.
 */
@Component
public class UnauthorizedEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        String message = "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"No autorizado\",\"message\":\"Se requiere un token JWT válido para acceder a este recurso\",\"path\":\"%s\"}"
                .formatted(
                        LocalDateTime.now(),
                        HttpStatus.UNAUTHORIZED.value(),
                        request.getRequestURI());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(message);
    }
}