package com.biblioteca.prestamos.model.entity;

import com.biblioteca.prestamos.model.enums.EstadoPrestamo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "prestamos")
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "libro_id", nullable = false)
    private Long libroId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "fecha_prestamo", nullable = false)
    private LocalDate fechaPrestamo;

    @Column(name = "fecha_devolucion_prevista", nullable = false)
    private LocalDate fechaDevolucionPrevista;

    @Column(name = "fecha_devolucion_real")
    private LocalDate fechaDevolucionReal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPrestamo estado;

    @Column(name = "dias_retraso")
    private Integer diasRetraso;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (estado == null) {
            estado = EstadoPrestamo.ACTIVO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void calcularDiasRetraso() {
        if (estado == EstadoPrestamo.ACTIVO &&
                LocalDate.now().isAfter(fechaDevolucionPrevista)) {
            this.diasRetraso = (int) java.time.temporal.ChronoUnit.DAYS.between(
                    fechaDevolucionPrevista, LocalDate.now()
            );
        } else {
            this.diasRetraso = 0;
        }
    }

    public boolean isVencido() {
        return estado == EstadoPrestamo.ACTIVO &&
                LocalDate.now().isAfter(fechaDevolucionPrevista);
    }
}