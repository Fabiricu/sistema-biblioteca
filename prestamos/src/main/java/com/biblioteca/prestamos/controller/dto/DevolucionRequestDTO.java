package com.biblioteca.prestamos.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "DevolucionRequestDTO", description = "DTO para registrar una devolución")
public class DevolucionRequestDTO {

    @Schema(description = "Observaciones de la devolución", example = "Libro en buen estado")
    private String observaciones;

    @Schema(description = "Indica si el libro se perdió", example = "false")
    @Builder.Default
    private Boolean libroPerdido = false;
}
