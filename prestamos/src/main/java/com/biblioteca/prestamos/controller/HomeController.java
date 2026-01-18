package com.biblioteca.prestamos.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return """
               🚀 Servicio de Préstamos - Biblioteca Digital
               
               ✅ Estado: ACTIVO
               📍 Puerto: 8083
               
               🔗 Endpoints principales:
               • /api/prestamos - Gestión de préstamos
               • /swagger-ui.html - Documentación API
               • /actuator/health - Estado del servicio
               
               🔌 Dependencias:
               • Microservicio Libros (8082)
               • Microservicio Usuarios (8081)
               
               📊 Características:
               • Spring Boot 3.5.9
               • Comunicación REST con otros servicios
               • Validación de préstamos
               • Control de fechas y renovaciones
               """;
    }
}
