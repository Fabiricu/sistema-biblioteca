package com.biblioteca.libros.repository;

import com.biblioteca.libros.model.entity.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {

    Optional<Libro> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

//    @Query("SELECT l.ejemplaresDisponibles > 0 FROM Libro l WHERE l.id = :libroId")
//    boolean isLibroDisponible(@Param("libroId") Long libroId);

    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN (l.ejemplaresDisponibles > 0) ELSE false END " +
            "FROM Libro l WHERE l.id = :libroId")
    boolean isLibroDisponible(@Param("libroId") Long libroId);

    @Query("SELECT l.ejemplaresDisponibles FROM Libro l WHERE l.id = :libroId")
    Optional<Integer> findEjemplaresDisponiblesById(@Param("libroId") Long libroId);
}
