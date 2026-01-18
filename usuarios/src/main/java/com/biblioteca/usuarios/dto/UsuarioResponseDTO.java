// UsuarioResponseDTO.java
package com.biblioteca.usuarios.dto;

import lombok.Data;

@Data
public class UsuarioResponseDTO {
    private Long id;
    private String nombreCompleto;
    private String email;
    private boolean activo;
}