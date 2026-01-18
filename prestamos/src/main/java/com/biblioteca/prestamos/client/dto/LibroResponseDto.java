package com.biblioteca.prestamos.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibroResponseDto {
    private Long id;
    private String titulo;
    private String autor;
    private String isbn;
    private Integer ejemplaresDisponibles;
    private Boolean disponible;
}