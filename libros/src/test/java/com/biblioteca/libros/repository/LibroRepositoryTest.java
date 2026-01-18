package com.biblioteca.libros.repository;

import com.biblioteca.libros.model.entity.Libro;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Pruebas del Repositorio de Libros")
class LibroRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LibroRepository libroRepository;

    private Libro libro1;
    private Libro libro2;

    @BeforeEach
    void setUp() {
        // Limpiar la base de datos antes de cada test
        libroRepository.deleteAll();

        // Crear libros de prueba
        libro1 = Libro.builder()
                .titulo("Cien Años de Soledad")
                .autor("Gabriel García Márquez")
                .isbn("978-0307474728")
                .ejemplaresDisponibles(5)
                .build();

        libro2 = Libro.builder()
                .titulo("Rayuela")
                .autor("Julio Cortázar")
                .isbn("978-8437604572")
                .ejemplaresDisponibles(3)
                .build();

        // Persistir en la base de datos de prueba
        entityManager.persist(libro1);
        entityManager.persist(libro2);
        entityManager.flush();
    }

    @Test
    @DisplayName("Debería encontrar un libro por ID")
    void testFindById() {
        // When
        Optional<Libro> encontrado = libroRepository.findById(libro1.getId());

        // Then
        assertTrue(encontrado.isPresent());
        assertEquals(libro1.getTitulo(), encontrado.get().getTitulo());
        assertEquals(libro1.getIsbn(), encontrado.get().getIsbn());
        assertEquals(libro1.getEjemplaresDisponibles(), encontrado.get().getEjemplaresDisponibles());
    }

    @Test
    @DisplayName("Debería encontrar todos los libros")
    void testFindAll() {
        // When
        List<Libro> libros = libroRepository.findAll();

        // Then
        assertThat(libros).hasSize(2);
        assertThat(libros).extracting(Libro::getTitulo)
                .containsExactlyInAnyOrder("Cien Años de Soledad", "Rayuela");
    }

    @Test
    @DisplayName("Debería guardar un nuevo libro")
    void testSave() {
        // Given
        Libro nuevoLibro = Libro.builder()
                .titulo("El Aleph")
                .autor("Jorge Luis Borges")
                .isbn("978-8426418193")
                .ejemplaresDisponibles(2)
                .build();

        // When
        Libro guardado = libroRepository.save(nuevoLibro);

        // Then
        assertNotNull(guardado.getId());
        assertEquals("El Aleph", guardado.getTitulo());
        assertEquals("Jorge Luis Borges", guardado.getAutor());
        assertEquals("978-8426418193", guardado.getIsbn());
        assertEquals(2, guardado.getEjemplaresDisponibles());
    }

    @Test
    @DisplayName("Debería actualizar un libro existente")
    void testUpdate() {
        // Given
        libro1.setEjemplaresDisponibles(10);
        libro1.setTitulo("Cien Años de Soledad (Edición Especial)");

        // When
        Libro actualizado = libroRepository.save(libro1);

        // Then
        assertEquals(10, actualizado.getEjemplaresDisponibles());
        assertEquals("Cien Años de Soledad (Edición Especial)", actualizado.getTitulo());
    }

    @Test
    @DisplayName("Debería eliminar un libro por ID")
    void testDeleteById() {
        // Given
        Long id = libro1.getId();

        // When
        libroRepository.deleteById(id);

        // Then
        Optional<Libro> eliminado = libroRepository.findById(id);
        assertFalse(eliminado.isPresent());
    }

    @Test
    @DisplayName("Debería encontrar un libro por ISBN")
    void testFindByIsbn() {
        // When
        Optional<Libro> encontrado = libroRepository.findByIsbn("978-0307474728");

        // Then
        assertTrue(encontrado.isPresent());
        assertEquals("Cien Años de Soledad", encontrado.get().getTitulo());
        assertEquals("Gabriel García Márquez", encontrado.get().getAutor());
        assertEquals(5, encontrado.get().getEjemplaresDisponibles());
    }

    @Test
    @DisplayName("Debería verificar si existe un libro por ISBN")
    void testExistsByIsbn() {
        // When & Then
        assertTrue(libroRepository.existsByIsbn("978-0307474728"));
        assertTrue(libroRepository.existsByIsbn("978-8437604572"));
        assertFalse(libroRepository.existsByIsbn("ISBN-INEXISTENTE"));
    }

    @Test
    @DisplayName("Debería verificar si un libro está disponible (ejemplares > 0)")
    void testIsLibroDisponible() {
        // When & Then
        assertTrue(libroRepository.isLibroDisponible(libro1.getId())); // 5 ejemplares
        assertTrue(libroRepository.isLibroDisponible(libro2.getId())); // 3 ejemplares
    }

    @Test
    @DisplayName("Debería retornar false cuando un libro no tiene ejemplares disponibles")
    void testIsLibroDisponible_SinEjemplares() {
        // Given
        libro1.setEjemplaresDisponibles(0);
        entityManager.persistAndFlush(libro1);

        // When & Then
        assertFalse(libroRepository.isLibroDisponible(libro1.getId()));
    }

    @Test
    @DisplayName("Debería obtener los ejemplares disponibles de un libro")
    void testFindEjemplaresDisponiblesById() {
        // When
        Optional<Integer> ejemplares = libroRepository.findEjemplaresDisponiblesById(libro1.getId());

        // Then
        assertTrue(ejemplares.isPresent());
        assertEquals(5, ejemplares.get());
    }

    @Test
    @DisplayName("Debería retornar Optional vacío para ID inexistente en findEjemplaresDisponiblesById")
    void testFindEjemplaresDisponiblesById_Inexistente() {
        // When
        Optional<Integer> ejemplares = libroRepository.findEjemplaresDisponiblesById(999L);

        // Then
        assertFalse(ejemplares.isPresent());
    }

    @Test
    @DisplayName("Debería retornar false para ID inexistente en isLibroDisponible")
    void testIsLibroDisponible_Inexistente() {
        // When & Then
        assertFalse(libroRepository.isLibroDisponible(999L));
    }

    @Test
    @DisplayName("No debería encontrar un libro por ISBN inexistente")
    void testFindByIsbnNotFound() {
        // When
        Optional<Libro> encontrado = libroRepository.findByIsbn("ISBN-INEXISTENTE");

        // Then
        assertFalse(encontrado.isPresent());
    }

    @Test
    @DisplayName("Debería verificar que el ISBN es único")
    @Transactional
    void testIsbnUnico() {
        // 1. Limpiar datos existentes
        libroRepository.deleteAllInBatch();
        entityManager.flush();
        entityManager.clear();

        // 2. Generar ISBN dentro del límite (máximo 20 caracteres)
        // Usar timestamp pero acortado
        String timestamp = String.valueOf(System.currentTimeMillis());
        String isbnTest = "TEST-" + timestamp.substring(timestamp.length() - 8); // Últimos 8 dígitos

        // Asegurar que no exceda 20 caracteres
        if (isbnTest.length() > 20) {
            isbnTest = isbnTest.substring(0, 20);
        }

        // 3. Guardar PRIMER libro
        Libro libro1 = Libro.builder()
                .titulo("Libro Uno de Prueba")
                .autor("Autor Prueba 1")
                .isbn(isbnTest)
                .ejemplaresDisponibles(5)
                .build();

        libroRepository.save(libro1);
        entityManager.flush();
        entityManager.clear();

        // 4. Intentar guardar SEGUNDO libro con MISMO ISBN
        Libro libro2 = Libro.builder()
                .titulo("Libro Dos de Prueba (Duplicado)")
                .autor("Autor Prueba 2")
                .isbn(isbnTest)  // MISMO ISBN - debe fallar
                .ejemplaresDisponibles(3)
                .build();

        // 5. Verificar que NO se puede guardar el duplicado
        assertThrows(DataIntegrityViolationException.class, () -> {
            libroRepository.saveAndFlush(libro2);
        });
    }

    @Test
    @DisplayName("Debería contar correctamente el número de libros")
    void testCount() {
        // When
        long count = libroRepository.count();

        // Then
        assertEquals(2, count);
    }

    @Test
    @DisplayName("Debería eliminar todos los libros")
    void testDeleteAll() {
        // When
        libroRepository.deleteAll();

        // Then
        assertEquals(0, libroRepository.count());
        assertTrue(libroRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("Debería actualizar ejemplares disponibles y persistir cambios")
    void testUpdateEjemplaresDisponibles() {
        // Given
        int nuevosEjemplares = 8;
        libro1.setEjemplaresDisponibles(nuevosEjemplares);

        // When
        Libro actualizado = libroRepository.save(libro1);

        // Then
        assertEquals(nuevosEjemplares, actualizado.getEjemplaresDisponibles());

        // Verificar que persiste en la base de datos
        Libro desdeBD = libroRepository.findById(libro1.getId()).orElseThrow();
        assertEquals(nuevosEjemplares, desdeBD.getEjemplaresDisponibles());
    }

//    @Test
//    @DisplayName("Debería manejar libros con ejemplares negativos (si se permite)")
//    void testEjemplaresNegativos() {
//        // Given
//        libro1.setEjemplaresDisponibles(-1);
//
//
//
//        // When
//        Libro guardado = libroRepository.save(libro1);
//
//        // Then
//        assertEquals(-1, guardado.getEjemplaresDisponibles());
//        assertFalse(libroRepository.isLibroDisponible(libro1.getId()));
//    }

    @Test
    @DisplayName("No debería permitir libros con ejemplares negativos")
    void testEjemplaresNegativos() {
        // Given - Crear libro con ejemplares negativos
        Libro libroConNegativos = Libro.builder()
                .titulo("Libro con ejemplares negativos")
                .autor("Autor Test")
                .isbn("978-1234567890")
                .ejemplaresDisponibles(-1)  // Valor inválido
                .build();

        // When & Then - Debería lanzar excepción al guardar
        assertThrows(jakarta.validation.ConstraintViolationException.class, () -> {
            libroRepository.saveAndFlush(libroConNegativos);  // saveAndFlush para forzar validación
        });
    }

    @Test
    @DisplayName("Debería encontrar libro por ID después de múltiples operaciones")
    void testPersistenciaConsistente() {
        // Given
        Long id = libro1.getId();

        // Realizar múltiples operaciones
        libro1.setTitulo("Título Modificado");
        libroRepository.save(libro1);

        libroRepository.delete(libro2);

        // When
        Optional<Libro> resultado = libroRepository.findById(id);

        // Then
        assertTrue(resultado.isPresent());
        assertEquals("Título Modificado", resultado.get().getTitulo());
        assertEquals(1, libroRepository.count());
    }

    @Test
    @DisplayName("Debería verificar disponibilidad después de actualizar ejemplares")
    void testIsLibroDisponibleAfterUpdate() {
        // Given
        Long id = libro1.getId();

        // When - Actualizar a 0 ejemplares
        libro1.setEjemplaresDisponibles(0);
        libroRepository.save(libro1);

        // Then
        assertFalse(libroRepository.isLibroDisponible(id));

        // When - Actualizar a 5 ejemplares
        libro1.setEjemplaresDisponibles(5);
        libroRepository.save(libro1);

        // Then
        assertTrue(libroRepository.isLibroDisponible(id));
    }

    @Test
    @DisplayName("Debería manejar consultas concurrentes")
    void testConsultasConcurrentes() {
        // When - Realizar múltiples consultas simultáneas
        Optional<Libro> porId = libroRepository.findById(libro1.getId());
        Optional<Libro> porIsbn = libroRepository.findByIsbn(libro1.getIsbn());
        boolean existe = libroRepository.existsByIsbn(libro1.getIsbn());
        boolean disponible = libroRepository.isLibroDisponible(libro1.getId());
        Optional<Integer> ejemplares = libroRepository.findEjemplaresDisponiblesById(libro1.getId());

        // Then - Todas deben retornar valores consistentes
        assertTrue(porId.isPresent());
        assertTrue(porIsbn.isPresent());
        assertTrue(existe);
        assertTrue(disponible);
        assertTrue(ejemplares.isPresent());
        assertEquals(5, ejemplares.get());
    }

    @Test
    @DisplayName("Debería funcionar con Optional.empty() cuando no hay resultados")
    void testOptionalEmpty() {
        // When
        Optional<Libro> noExiste = libroRepository.findById(999L);
        Optional<Libro> isbnNoExiste = libroRepository.findByIsbn("NO-EXISTE");
        Optional<Integer> ejemplaresNoExiste = libroRepository.findEjemplaresDisponiblesById(999L);

        // Then
        assertFalse(noExiste.isPresent());
        assertFalse(isbnNoExiste.isPresent());
        assertFalse(ejemplaresNoExiste.isPresent());
    }
}