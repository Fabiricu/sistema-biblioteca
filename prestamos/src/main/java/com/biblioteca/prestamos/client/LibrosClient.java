package com.biblioteca.prestamos.client;

import com.biblioteca.prestamos.client.dto.LibroResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@FeignClient(name = "libros-service", url = "${client.libros-service.url:http://localhost:8082}")
public interface LibrosClient {

    @GetMapping("/api/libros/{id}")
    ResponseEntity<LibroResponseDto> obtenerLibro(@PathVariable("id") Long libroId);

    @GetMapping("/api/libros/{id}/disponible")
    ResponseEntity<Boolean> verificarDisponibilidad(@PathVariable("id") Long libroId);

    @PostMapping("/api/libros/{id}/prestar")
    ResponseEntity<Void> prestarLibro(@PathVariable("id") Long libroId);

    @PostMapping("/api/libros/{id}/devolver")
    ResponseEntity<Void> devolverLibro(@PathVariable("id") Long libroId);

    @GetMapping("/api/libros/{id}/existe")
    ResponseEntity<Boolean> existeLibro(@PathVariable("id") Long libroId);
}
