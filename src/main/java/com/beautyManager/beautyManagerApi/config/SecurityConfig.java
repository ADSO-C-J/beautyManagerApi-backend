package com.beautyManager.beautyManagerApi.config;

import com.beautyManager.beautyManagerApi.security.JwtAuthenticationFilter;
import com.beautyManager.beautyManagerApi.security.UnauthorizedEntryPoint;
import lombok.RequiredArgsConstructor;
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

import java.util.List;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UnauthorizedEntryPoint unauthorizedEntryPoint;

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
                    .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                    // Documentación OpenAPI / Swagger UI (pública)
                    .requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/swagger-ui.html/**").permitAll()
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
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
