package com.biblioteca.libros.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "libros",
        uniqueConstraints = @UniqueConstraint(columnNames = "isbn"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El título es obligatorio")
    @Column(nullable = false, length = 200)
    private String titulo;

    @NotBlank(message = "El autor es obligatorio")
    @Column(nullable = false, length = 100)
    private String autor;

    @NotBlank(message = "El ISBN es obligatorio")
    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Min(value = 0, message = "Los ejemplares disponibles no pueden ser negativos")
    @Column(nullable = false)
    @Builder.Default
    private int ejemplaresDisponibles = 0;

    // Método para verificar disponibilidad
    public boolean estaDisponible() {
        return ejemplaresDisponibles > 0;
    }

    // Método para prestar un ejemplar
    public boolean prestarEjemplar() {
        if (ejemplaresDisponibles > 0) {
            ejemplaresDisponibles--;
            return true;
        }
        return false;
    }

    // Método para devolver un ejemplar
    public void devolverEjemplar() {
        ejemplaresDisponibles++;
    }
}