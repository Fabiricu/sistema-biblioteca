package com.biblioteca.prestamos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Préstamos - Biblioteca")
                        .version("1.0")
                        .description("API REST para gestión de préstamos de libros")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo")
                                .email("desarrollo@biblioteca.com")));
    }
}