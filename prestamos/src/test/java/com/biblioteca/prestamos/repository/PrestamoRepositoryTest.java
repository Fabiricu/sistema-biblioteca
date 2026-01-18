package com.biblioteca.prestamos.repository;

import com.biblioteca.prestamos.model.entity.Prestamo;
import com.biblioteca.prestamos.model.enums.EstadoPrestamo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("Pruebas del Repositorio de Préstamos")
class PrestamoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PrestamoRepository prestamoRepository;

    private Prestamo prestamoActivo;
    private Prestamo prestamoDevuelto;
    private Prestamo prestamoVencido;

    @BeforeEach
    void setUp() {
        // Limpiar base de datos antes de cada test
        prestamoRepository.deleteAll();

        // Crear préstamos de prueba
        prestamoActivo = Prestamo.builder()
                .libroId(1L)
                .usuarioId(1L)
                .fechaPrestamo(LocalDate.now().minusDays(5))
                .fechaDevolucionPrevista(LocalDate.now().plusDays(9))
                .estado(EstadoPrestamo.ACTIVO)
                .observaciones("Préstamo activo de prueba")
                .build();

        prestamoDevuelto = Prestamo.builder()
                .libroId(2L)
                .usuarioId(1L)
                .fechaPrestamo(LocalDate.now().minusDays(15))
                .fechaDevolucionPrevista(LocalDate.now().minusDays(1))
                .fechaDevolucionReal(LocalDate.now().minusDays(1))
                .estado(EstadoPrestamo.DEVUELTO)
                .observaciones("Préstamo devuelto")
                .build();

        prestamoVencido = Prestamo.builder()
                .libroId(3L)
                .usuarioId(2L)
                .fechaPrestamo(LocalDate.now().minusDays(20))
                .fechaDevolucionPrevista(LocalDate.now().minusDays(6))
                .estado(EstadoPrestamo.ACTIVO)  // Estará vencido pero estado ACTIVO
                .diasRetraso(6)
                .observaciones("Préstamo vencido")
                .build();

        // Guardar en base de datos
        entityManager.persist(prestamoActivo);
        entityManager.persist(prestamoDevuelto);
        entityManager.persist(prestamoVencido);
        entityManager.flush();
    }

    @Test
    @DisplayName("Debería encontrar un préstamo por ID")
    void findById_Success() {
        // When
        Optional<Prestamo> encontrado = prestamoRepository.findById(prestamoActivo.getId());

        // Then
        assertTrue(encontrado.isPresent());
        assertEquals(prestamoActivo.getLibroId(), encontrado.get().getLibroId());
        assertEquals(prestamoActivo.getUsuarioId(), encontrado.get().getUsuarioId());
        assertEquals(EstadoPrestamo.ACTIVO, encontrado.get().getEstado());
    }

    @Test
    @DisplayName("Debería retornar Optional vacío para ID inexistente")
    void findById_NotFound() {
        // When
        Optional<Prestamo> encontrado = prestamoRepository.findById(999L);

        // Then
        assertFalse(encontrado.isPresent());
    }

    @Test
    @DisplayName("Debería encontrar todos los préstamos de un usuario")
    void findByUsuarioId_Success() {
        // When
        List<Prestamo> prestamosUsuario1 = prestamoRepository.findByUsuarioId(1L);
        List<Prestamo> prestamosUsuario2 = prestamoRepository.findByUsuarioId(2L);

        // Then
        assertThat(prestamosUsuario1).hasSize(2); // Activo + Devuelto
        assertThat(prestamosUsuario2).hasSize(1); // Vencido

        // Verificar que todos los préstamos son del usuario correcto
        assertThat(prestamosUsuario1).allMatch(p -> p.getUsuarioId().equals(1L));
        assertThat(prestamosUsuario2).allMatch(p -> p.getUsuarioId().equals(2L));
    }

    @Test
    @DisplayName("Debería encontrar todos los préstamos de un libro")
    void findByLibroId_Success() {
        // When
        List<Prestamo> prestamosLibro1 = prestamoRepository.findByLibroId(1L);

        // Then
        assertThat(prestamosLibro1).hasSize(1);
        assertEquals(1L, prestamosLibro1.get(0).getLibroId());
        assertEquals("Préstamo activo de prueba", prestamosLibro1.get(0).getObservaciones());
    }

    @Test
    @DisplayName("Debería encontrar préstamos por usuario y estado")
    void findByUsuarioIdAndEstado_Success() {
        // When
        List<Prestamo> prestamosActivosUsuario1 = prestamoRepository
                .findByUsuarioIdAndEstado(1L, EstadoPrestamo.ACTIVO);

        List<Prestamo> prestamosDevueltosUsuario1 = prestamoRepository
                .findByUsuarioIdAndEstado(1L, EstadoPrestamo.DEVUELTO);

        // Then
        assertThat(prestamosActivosUsuario1).hasSize(1);
        assertThat(prestamosDevueltosUsuario1).hasSize(1);

        assertEquals(EstadoPrestamo.ACTIVO, prestamosActivosUsuario1.get(0).getEstado());
        assertEquals(EstadoPrestamo.DEVUELTO, prestamosDevueltosUsuario1.get(0).getEstado());
    }

    @Test
    @DisplayName("Debería encontrar préstamo activo por libro")
    void findByLibroIdAndEstado_Success() {
        // When
        Optional<Prestamo> prestamoActivoLibro1 = prestamoRepository
                .findByLibroIdAndEstado(1L, EstadoPrestamo.ACTIVO);

        Optional<Prestamo> prestamoActivoLibro2 = prestamoRepository
                .findByLibroIdAndEstado(2L, EstadoPrestamo.ACTIVO);

        // Then
        assertTrue(prestamoActivoLibro1.isPresent());
        assertFalse(prestamoActivoLibro2.isPresent()); // Libro 2 está devuelto, no activo

        assertEquals(1L, prestamoActivoLibro1.get().getLibroId());
        assertEquals(EstadoPrestamo.ACTIVO, prestamoActivoLibro1.get().getEstado());
    }

    @Test
    @DisplayName("Debería contar préstamos activos de un usuario")
    void countByUsuarioIdAndEstado_Success() {
        // When
        long countActivosUsuario1 = prestamoRepository
                .countByUsuarioIdAndEstado(1L, EstadoPrestamo.ACTIVO);

        long countDevueltosUsuario1 = prestamoRepository
                .countByUsuarioIdAndEstado(1L, EstadoPrestamo.DEVUELTO);

        long countActivosUsuario2 = prestamoRepository
                .countByUsuarioIdAndEstado(2L, EstadoPrestamo.ACTIVO);

        // Then
        assertEquals(1L, countActivosUsuario1);
        assertEquals(1L, countDevueltosUsuario1);
        assertEquals(1L, countActivosUsuario2); // El vencido sigue estando como ACTIVO
    }

    @Test
    @DisplayName("Debería encontrar préstamos vencidos")
    void findPrestamosVencidos_Success() {
        // When
        List<Prestamo> prestamosVencidos = prestamoRepository
                .findPrestamosVencidos(LocalDate.now());

        // Then
        assertThat(prestamosVencidos).hasSize(1);
        assertEquals(3L, prestamosVencidos.get(0).getLibroId());
        assertTrue(prestamosVencidos.get(0).isVencido());
        assertEquals(6, prestamosVencidos.get(0).getDiasRetraso());
    }

    @Test
    @DisplayName("Debería encontrar préstamos por rango de fechas")
    void findByFechaPrestamoBetween_Success() {
        // Given
        LocalDate inicio = LocalDate.now().minusDays(30);
        LocalDate fin = LocalDate.now();

        // When
        List<Prestamo> prestamos = prestamoRepository.findByFechaPrestamoBetween(inicio, fin);

        // Then
        assertThat(prestamos).hasSize(3);
        assertThat(prestamos).allMatch(p ->
                !p.getFechaPrestamo().isBefore(inicio) &&
                        !p.getFechaPrestamo().isAfter(fin)
        );
    }

    @Test
    @DisplayName("Debería encontrar préstamos con retraso")
    void findPrestamosConRetraso_Success() {
        // When
        List<Prestamo> prestamosConRetraso = prestamoRepository.findPrestamosConRetraso();

        // Then
        assertThat(prestamosConRetraso).hasSize(1);
        assertEquals(3L, prestamosConRetraso.get(0).getLibroId());
        assertTrue(prestamosConRetraso.get(0).getDiasRetraso() > 0);
    }

    @Test
    @DisplayName("Debería verificar si un libro está prestado")
    void isLibroPrestado_Success() {
        // When
        boolean libro1Prestado = prestamoRepository.isLibroPrestado(1L);  // Activo
        boolean libro2Prestado = prestamoRepository.isLibroPrestado(2L);  // Devuelto
        boolean libro3Prestado = prestamoRepository.isLibroPrestado(3L);  // Vencido (pero activo)
        boolean libro4Prestado = prestamoRepository.isLibroPrestado(4L);  // No existe

        // Then
        assertTrue(libro1Prestado);
        assertFalse(libro2Prestado); // Devuelto, no está prestado
        assertTrue(libro3Prestado);  // Vencido pero estado ACTIVO, técnicamente prestado
        assertFalse(libro4Prestado); // No existe
    }

    @Test
    @DisplayName("Debería guardar un nuevo préstamo")
    void save_Success() {
        // Given
        Prestamo nuevoPrestamo = Prestamo.builder()
                .libroId(4L)
                .usuarioId(3L)
                .fechaPrestamo(LocalDate.now())
                .fechaDevolucionPrevista(LocalDate.now().plusDays(21))
                .estado(EstadoPrestamo.ACTIVO)
                .observaciones("Nuevo préstamo")
                .build();

        // When
        Prestamo guardado = prestamoRepository.save(nuevoPrestamo);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertNotNull(guardado.getId());

        // Verificar que se guardó correctamente
        Optional<Prestamo> encontrado = prestamoRepository.findById(guardado.getId());
        assertTrue(encontrado.isPresent());
        assertEquals(4L, encontrado.get().getLibroId());
        assertEquals(3L, encontrado.get().getUsuarioId());
        assertEquals("Nuevo préstamo", encontrado.get().getObservaciones());
        assertNotNull(encontrado.get().getCreatedAt());
        assertNotNull(encontrado.get().getUpdatedAt());
    }

    @Test
    @DisplayName("Debería actualizar un préstamo existente")
    void update_Success() {
        // Given
        Prestamo prestamo = prestamoRepository.findById(prestamoActivo.getId()).get();
        prestamo.setObservaciones("Observaciones actualizadas");
        prestamo.setDiasRetraso(2);

        // When
        Prestamo actualizado = prestamoRepository.save(prestamo);
        entityManager.flush();
        entityManager.clear();

        // Then
        Prestamo desdeBD = prestamoRepository.findById(actualizado.getId()).get();
        assertEquals("Observaciones actualizadas", desdeBD.getObservaciones());
        assertEquals(2, desdeBD.getDiasRetraso());
        assertNotNull(desdeBD.getUpdatedAt());
    }

    @Test
    @DisplayName("Debería eliminar un préstamo por ID")
    void deleteById_Success() {
        // Given
        Long id = prestamoActivo.getId();

        // When
        prestamoRepository.deleteById(id);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Prestamo> eliminado = prestamoRepository.findById(id);
        assertFalse(eliminado.isPresent());

        // Verificar que los otros préstamos siguen existiendo
        assertEquals(2, prestamoRepository.count());
    }

    @Test
    @DisplayName("Debería calcular días de retraso correctamente")
    void calcularDiasRetraso_Success() {
        // Given
        Prestamo prestamo = prestamoRepository.findById(prestamoVencido.getId()).get();

        // When
        prestamo.calcularDiasRetraso();

        // Then
        assertTrue(prestamo.getDiasRetraso() > 0);
        assertTrue(prestamo.isVencido());
    }

    @Test
    @DisplayName("Debería lanzar excepción al guardar préstamo sin libroId")
    void save_WithoutLibroId_ThrowsException() {
        // Given
        Prestamo prestamoInvalido = Prestamo.builder()
                .usuarioId(1L)
                .fechaPrestamo(LocalDate.now())
                .fechaDevolucionPrevista(LocalDate.now().plusDays(14))
                .estado(EstadoPrestamo.ACTIVO)
                .build();

        // When & Then
        assertThatThrownBy(() -> {
            prestamoRepository.save(prestamoInvalido);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("libro_id");
    }

    @Test
    @DisplayName("Debería lanzar excepción al guardar préstamo sin usuarioId")
    void save_WithoutUsuarioId_ThrowsException() {
        // Given
        Prestamo prestamoInvalido = Prestamo.builder()
                .libroId(1L)
                .fechaPrestamo(LocalDate.now())
                .fechaDevolucionPrevista(LocalDate.now().plusDays(14))
                .estado(EstadoPrestamo.ACTIVO)
                .build();

        // When & Then
        assertThatThrownBy(() -> {
            prestamoRepository.save(prestamoInvalido);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("usuario_id");
    }

    @Test
    @DisplayName("Debería lanzar excepción al guardar préstamo sin fecha préstamo")
    void save_WithoutFechaPrestamo_ThrowsException() {
        // Given
        Prestamo prestamoInvalido = Prestamo.builder()
                .libroId(1L)
                .usuarioId(1L)
                .fechaDevolucionPrevista(LocalDate.now().plusDays(14))
                .estado(EstadoPrestamo.ACTIVO)
                .build();

        // When & Then
        assertThatThrownBy(() -> {
            prestamoRepository.save(prestamoInvalido);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("fecha_prestamo");
    }

    @Test
    @DisplayName("Debería lanzar excepción al guardar préstamo sin fecha devolución prevista")
    void save_WithoutFechaDevolucionPrevista_ThrowsException() {
        // Given
        Prestamo prestamoInvalido = Prestamo.builder()
                .libroId(1L)
                .usuarioId(1L)
                .fechaPrestamo(LocalDate.now())
                .estado(EstadoPrestamo.ACTIVO)
                .build();

        // When & Then
        assertThatThrownBy(() -> {
            prestamoRepository.save(prestamoInvalido);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("fecha_devolucion_prevista");
    }

    @Test
    @DisplayName("Debería retornar todos los préstamos")
    void findAll_Success() {
        // When
        List<Prestamo> todos = prestamoRepository.findAll();

        // Then
        assertThat(todos).hasSize(3);
        assertThat(todos).extracting(Prestamo::getEstado)
                .containsExactlyInAnyOrder(EstadoPrestamo.ACTIVO, EstadoPrestamo.DEVUELTO, EstadoPrestamo.ACTIVO);
    }

    @Test
    @DisplayName("Debería contar el total de préstamos")
    void count_Success() {
        // When
        long count = prestamoRepository.count();

        // Then
        assertEquals(3L, count);
    }
}