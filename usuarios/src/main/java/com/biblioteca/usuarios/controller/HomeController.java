package com.biblioteca.usuarios.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return """
               🚀 Servicio de Usuarios - Biblioteca Digital
               
               ✅ Estado: ACTIVO
               📍 Puerto: 8081
               
               🔗 Endpoints principales:
               • /api/usuarios - Gestión completa de Usuarios
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
