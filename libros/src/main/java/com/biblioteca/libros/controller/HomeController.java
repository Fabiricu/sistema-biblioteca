package com.biblioteca.libros.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return """
               🚀 Servicio de Libros - Biblioteca Digital
               
               ✅ Estado: ACTIVO
               📍 Puerto: 8082
               
               🔗 Endpoints principales:
               • /api/libros - Gestión completa de libros
               • /swagger-ui.html - Documentación API
               • /actuator/health - Estado del servicio
               
               📊 Características:
               • Spring Boot 3.5.9
               • MySQL Database
               • SpringDoc OpenAPI 3.0
               • Spring Boot Actuator
               """;
    }
}
