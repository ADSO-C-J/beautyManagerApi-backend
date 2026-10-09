package com.beautyManager.beautyManagerApi.config;

import com.beautyManager.beautyManagerApi.security.JwtAuthenticationFilter;
import com.beautyManager.beautyManagerApi.security.UnauthorizedEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UnauthorizedEntryPoint unauthorizedEntryPoint;

    /**
     * Orígenes extra permitidos por CORS, separados por coma (ej. en Render:
     * CORS_ALLOWED_ORIGINS=https://otro-dominio.com,https://*.vercel.app).
     * Permite añadir dominios en producción sin recompilar.
     */
    @Value("${CORS_ALLOWED_ORIGINS:}")
    private String corsAllowedOrigins;

    /**
     * Configura el encoder de contraseñas usando BCrypt.
     * BCrypt es un algoritmo de hash seguro diseñado específicamente para contraseñas.
     * Incluye un salt automático y un costo configurable.
     *
     * @return PasswordEncoder configurado con BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())   // usa el bean CorsConfigurationSource de abajo
            .csrf(csrf -> csrf.disable())
            // API stateless: no se guarda sesión en el servidor
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Respuesta JSON 401 cuando una petición protegida llega sin token válido
            .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedEntryPoint))
            .authorizeHttpRequests(auth -> auth
                    // Endpoints públicos de autenticación
                    .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/refresh").permitAll()
                    // Documentación OpenAPI / Swagger UI (pública)
                    .requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/swagger-ui.html/**").permitAll()
                    // Página de error de Spring: debe ser pública para no enmascarar
                    // errores reales (400/404/409/500) como 401. El re-despacho interno a
                    // /error pierde el contexto de autenticación, así que si esta ruta no
                    // es pública CUALQUIER excepción del controlador se reporta como 401.
                    .requestMatchers("/error", "/error/**").permitAll()
                    // Solo 'health' es publico: lo usa el HEALTHCHECK del contenedor
                    // (docker) y los orquestadores. El resto de Actuator
                    // (/actuator/env, /metrics, ...) sigue requiriendo token.
                    .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                    // El resto de la API requiere autenticación
                    .anyRequest().authenticated()
            )
            // Ejecuta nuestro filtro JWT antes del filtro estándar de nombre-usuario/contraseña
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Evita que Spring Boot registre el filtro JWT también como un filtro de servlet global.
     * El filtro solo debe ejecutarse dentro de la cadena de Spring Security (addFilterBefore).
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    /**
     * Configuración global de CORS para permitir que el frontend (Vite, puerto 5173)
     * pueda consumir la API. Spring Security usa este bean al activar .cors() arriba.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Patrones de origen: cubre cualquier puerto de localhost/127.0.0.1 (Vite usa
        // 5173 por defecto pero salta al siguiente puerto libre si está ocupado).
        List<String> originPatterns = new ArrayList<>();
        originPatterns.add("http://localhost:[*]");
        originPatterns.add("http://127.0.0.1:[*]");
        // Frontend desplegado en Vercel (producción + previews).
        originPatterns.add("https://beauty-manager-seven.vercel.app");
        originPatterns.add("https://*.vercel.app");
        // Orígenes adicionales inyectados por variable de entorno (sin recompilar).
        if (corsAllowedOrigins != null && !corsAllowedOrigins.isBlank()) {
            for (String origin : corsAllowedOrigins.split(",")) {
                String trimmed = origin.trim();
                if (!trimmed.isEmpty()) {
                    originPatterns.add(trimmed);
                }
            }
        }
        config.setAllowedOriginPatterns(originPatterns);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
