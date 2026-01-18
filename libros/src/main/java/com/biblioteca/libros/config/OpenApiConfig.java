package com.biblioteca.libros.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8082}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        Server localServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("Servidor Local de Desarrollo");

        Server productionServer = new Server()
                .url("https://api.biblioteca-digital.com/libros")
                .description("Servidor de Producción");

        Contact contact = new Contact()
                .name("Equipo Biblioteca Digital")
                .email("soporte@biblioteca-digital.com")
                .url("https://www.biblioteca-digital.com");

        License mitLicense = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("📚 Microservicio de Libros - Biblioteca Digital")
                .version("1.0.0")
                .description("""
                    API REST completa para la gestión de libros del sistema de Biblioteca Digital.
                    
                    ## 🚀 Características
                    - ✅ CRUD completo de libros
                    - ✅ Gestión de stock y disponibilidad
                    - ✅ Validación de ISBN único
                    - ✅ Endpoints para préstamos y devoluciones
                    - ✅ Manejo de errores personalizado
                    - ✅ Documentación interactiva
                    
                    ## 📖 Modelo de Datos
                    - **id**: Identificador único (autogenerado)
                    - **titulo**: Título del libro (obligatorio)
                    - **autor**: Autor del libro (obligatorio)
                    - **isbn**: Código único del libro (obligatorio, único)
                    - **ejemplaresDisponibles**: Cantidad disponible (mínimo 0)
                    - **disponible**: Calculado automáticamente (true si ejemplaresDisponibles > 0)
                    
                    ## 🔗 Enlaces útiles
                    - [Repositorio GitHub](https://github.com/biblioteca-digital/libros)
                    - [Documentación Completa](https://docs.biblioteca-digital.com/libros)
                    - [Panel de Administración](https://admin.biblioteca-digital.com)
                    """)
                .contact(contact)
                .license(mitLicense)
                .termsOfService("https://www.biblioteca-digital.com/terms");

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer, productionServer));
    }
}
