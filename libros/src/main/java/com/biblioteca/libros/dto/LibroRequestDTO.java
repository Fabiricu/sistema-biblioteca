package com.biblioteca.libros.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor              // ← AGREGA ESTO
@AllArgsConstructor
@Schema(name = "LibroRequestDTO", description = "DTO para crear o actualizar un libro")
public class LibroRequestDTO {

    @Schema(
            description = "Título del libro",
            example = "Cien años de soledad",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 1,
            maxLength = 200
    )
    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    @Schema(
            description = "Autor del libro",
            example = "Gabriel García Márquez",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 1,
            maxLength = 100
    )
    @NotBlank(message = "El autor es obligatorio")
    private String autor;

    @Schema(
            description = "ISBN único del libro",
            example = "978-0307474728",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 10,
            maxLength = 20
    )
    @NotBlank(message = "El ISBN es obligatorio")
    private String isbn;

    @Schema(
            description = "Número de ejemplares disponibles",
            example = "5",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0"
    )
    @Min(value = 0, message = "Los ejemplares disponibles no pueden ser negativos")
    private int ejemplaresDisponibles;
}
