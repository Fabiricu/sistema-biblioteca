package com.biblioteca.libros.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "LibroResponseDTO", description = "DTO para respuesta de libro")
public class LibroResponseDTO {

    @Schema(description = "ID único del libro", example = "1")
    private Long id;

    @Schema(description = "Título del libro", example = "Cien años de soledad")
    private String titulo;

    @Schema(description = "Autor del libro", example = "Gabriel García Márquez")
    private String autor;

    @Schema(description = "ISBN del libro", example = "978-0307474728")
    private String isbn;

    @Schema(description = "Ejemplares disponibles", example = "5")
    private int ejemplaresDisponibles;

    @Schema(description = "Indica si el libro está disponible para préstamo", example = "true")
    private boolean disponible;
}
