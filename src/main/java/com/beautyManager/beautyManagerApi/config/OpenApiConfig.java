package com.beautyManager.beautyManagerApi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI 3 / Swagger UI.
 * <p>
 * Define la información general del API y el esquema de seguridad Bearer (JWT)
 * para que, desde Swagger UI, puedas autenticarte y probar endpoints protegidos.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI beautyManagerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BeautyManager API")
                        .description("""
                                API REST del sistema de gestión de salón de belleza BeautyManager.

                                Incluye autenticación con JWT (login/registro) y los módulos de
                                usuarios, servicios, clientes, estilistas y citas.

                                ## Autenticación
                                1. Usa el endpoint `POST /api/auth/register` para crear un usuario.
                                2. Usa `POST /api/auth/login` para obtener un token.
                                3. Pulsa el botón **Authorize** de arriba y pega el token.
                                """)
                        .version("1.0.0")
                        .contact(new Contact().name("BeautyManager")))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Token JWT obtenido en POST /api/auth/login")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}