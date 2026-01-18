package com.biblioteca.prestamos.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PrestamoRequestDTO", description = "DTO para crear un préstamo")
public class PrestamoRequestDTO {

    @Schema(description = "ID del libro a prestar", example = "1", required = true)
    @NotNull(message = "El ID del libro es obligatorio")
    @Positive(message = "El ID del libro debe ser positivo")
    private Long libroId;

    @Schema(description = "ID del usuario que solicita el préstamo", example = "123", required = true)
    @NotNull(message = "El ID del usuario es obligatorio")
    @Positive(message = "El ID del usuario debe ser positivo")
    private Long usuarioId;

    @Schema(description = "Fecha de devolución prevista", example = "2024-12-31", required = true)
    @NotNull(message = "La fecha de devolución prevista es obligatoria")
    @Future(message = "La fecha de devolución debe ser futura")
    private LocalDate fechaDevolucionPrevista;

    @Schema(description = "Observaciones adicionales", example = "Préstamo para investigación")
    private String observaciones;
}
