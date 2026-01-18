package com.biblioteca.prestamos.client;

import com.biblioteca.prestamos.client.dto.UsuarioResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "usuarios-service", url = "${client.usuarios-service.url:http://localhost:8081}")
public interface UsuariosClient {

    @GetMapping("/api/usuarios/{id}")
    ResponseEntity<UsuarioResponseDto> obtenerUsuario(@PathVariable("id") Long usuarioId);
}
