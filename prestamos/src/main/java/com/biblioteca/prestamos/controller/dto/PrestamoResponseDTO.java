package com.biblioteca.prestamos.controller.dto;

import com.biblioteca.prestamos.model.enums.EstadoPrestamo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PrestamoResponseDTO", description = "DTO con información del préstamo")
public class PrestamoResponseDTO {

    @Schema(description = "ID del préstamo", example = "1")
    private Long id;

    @Schema(description = "ID del libro", example = "1")
    private Long libroId;

    @Schema(description = "Título del libro", example = "Cien años de soledad")
    private String tituloLibro;

    @Schema(description = "ID del usuario", example = "123")
    private Long usuarioId;

    @Schema(description = "Fecha del préstamo", example = "2024-01-12")
    private LocalDate fechaPrestamo;

    @Schema(description = "Fecha de devolución prevista", example = "2024-01-26")
    private LocalDate fechaDevolucionPrevista;

    @Schema(description = "Fecha de devolución real", example = "2024-01-25")
    private LocalDate fechaDevolucionReal;

    @Schema(description = "Estado del préstamo", example = "ACTIVO")
    private EstadoPrestamo estado;

    @Schema(description = "Días de retraso", example = "2")
    private Integer diasRetraso;

    @Schema(description = "Observaciones", example = "Préstamo para investigación")
    private String observaciones;

    @Schema(description = "Indica si el préstamo está vencido", example = "false")
    private Boolean vencido;
}