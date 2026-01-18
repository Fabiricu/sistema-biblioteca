package com.biblioteca.prestamos.repository;

import com.biblioteca.prestamos.model.entity.Prestamo;
import com.biblioteca.prestamos.model.enums.EstadoPrestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    long countByEstado(EstadoPrestamo estado);

    List<Prestamo> findByUsuarioId(Long usuarioId);

    List<Prestamo> findByLibroId(Long libroId);



    List<Prestamo> findByEstado(EstadoPrestamo estado);

    List<Prestamo> findByUsuarioIdAndEstado(Long usuarioId, EstadoPrestamo estado);

    Optional<Prestamo> findByLibroIdAndEstado(Long libroId, EstadoPrestamo estado);

    long countByUsuarioIdAndEstado(Long usuarioId, EstadoPrestamo estado);

    @Query("SELECT p FROM Prestamo p WHERE p.estado = 'ACTIVO' AND p.fechaDevolucionPrevista < :fechaActual")
    List<Prestamo> findPrestamosVencidos(@Param("fechaActual") LocalDate fechaActual);

    List<Prestamo> findByFechaPrestamoBetween(LocalDate inicio, LocalDate fin);

    @Query("SELECT p FROM Prestamo p WHERE p.diasRetraso > 0")
    List<Prestamo> findPrestamosConRetraso();

    default boolean isLibroPrestado(Long libroId) {
        return findByLibroIdAndEstado(libroId, EstadoPrestamo.ACTIVO).isPresent();
    }
}
