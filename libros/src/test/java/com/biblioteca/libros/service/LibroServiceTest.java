package com.biblioteca.libros.service;

import com.biblioteca.libros.dto.LibroRequestDTO;
import com.biblioteca.libros.dto.LibroResponseDTO;
import com.biblioteca.libros.exception.IsbnDuplicadoException;
import com.biblioteca.libros.exception.LibroNoDisponibleException;
import com.biblioteca.libros.exception.LibroNotFoundException;
import com.biblioteca.libros.model.entity.Libro;
import com.biblioteca.libros.repository.LibroRepository;
import com.biblioteca.libros.service.impl.LibroServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas del Servicio de Libros")
class LibroServiceTest {

    @Mock
    private LibroRepository libroRepository;

    @InjectMocks
    private LibroServiceImpl libroService;

    private Libro libro;
    private LibroRequestDTO libroRequestDTO;

    @BeforeEach
    void setUp() {
        // Configurar libro de prueba
        libro = Libro.builder()
                .id(1L)
                .titulo("Cien Años de Soledad")
                .autor("Gabriel García Márquez")
                .isbn("978-0307474728")
                .ejemplaresDisponibles(5)
                .build();

        // Configurar DTO de request
        libroRequestDTO = LibroRequestDTO.builder()
                .titulo("Cien Años de Soledad")
                .autor("Gabriel García Márquez")
                .isbn("978-0307474728")
                .ejemplaresDisponibles(5)
                .build();
    }

    @Test
    @DisplayName("Debería crear un libro exitosamente")
    void testCrearLibro_Success() {
        // Given
        when(libroRepository.existsByIsbn(anyString())).thenReturn(false);
        when(libroRepository.save(any(Libro.class))).thenReturn(libro);

        // When
        LibroResponseDTO resultado = libroService.crearLibro(libroRequestDTO);

        // Then
        assertNotNull(resultado);
        assertEquals(libro.getId(), resultado.getId());
        assertEquals(libro.getTitulo(), resultado.getTitulo());
        assertEquals(libro.getIsbn(), resultado.getIsbn());
        assertEquals(libro.getEjemplaresDisponibles(), resultado.getEjemplaresDisponibles());
        assertTrue(resultado.isDisponible());

        verify(libroRepository).existsByIsbn(libroRequestDTO.getIsbn());
        verify(libroRepository).save(any(Libro.class));
    }

    @Test
    @DisplayName("Debería lanzar IsbnDuplicadoException al crear libro con ISBN duplicado")
    void testCrearLibro_IsbnDuplicado() {
        // Given
        when(libroRepository.existsByIsbn(anyString())).thenReturn(true);

        // When & Then
        IsbnDuplicadoException exception = assertThrows(IsbnDuplicadoException.class, () -> {
            libroService.crearLibro(libroRequestDTO);
        });

        assertEquals("El ISBN ya está registrado: " + libroRequestDTO.getIsbn(), exception.getMessage());
        verify(libroRepository).existsByIsbn(libroRequestDTO.getIsbn());
        verify(libroRepository, never()).save(any(Libro.class));
    }

    @Test
    @DisplayName("Debería obtener todos los libros")
    void testObtenerTodosLibros() {
        // Given
        Libro libro2 = Libro.builder()
                .id(2L)
                .titulo("Rayuela")
                .autor("Julio Cortázar")
                .isbn("978-8437604572")
                .ejemplaresDisponibles(0)
                .build();

        List<Libro> libros = Arrays.asList(libro, libro2);
        when(libroRepository.findAll()).thenReturn(libros);

        // When
        List<LibroResponseDTO> resultado = libroService.obtenerTodosLibros();

        // Then
        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(LibroResponseDTO::getTitulo)
                .containsExactly("Cien Años de Soledad", "Rayuela");
        assertThat(resultado).extracting(LibroResponseDTO::isDisponible)
                .containsExactly(true, false);

        verify(libroRepository).findAll();
    }

    @Test
    @DisplayName("Debería obtener un libro por ID exitosamente")
    void testObtenerLibroPorId_Success() {
        // Given
        when(libroRepository.findById(anyLong())).thenReturn(Optional.of(libro));

        // When
        LibroResponseDTO resultado = libroService.obtenerLibroPorId(1L);

        // Then
        assertNotNull(resultado);
        assertEquals(libro.getId(), resultado.getId());
        assertEquals(libro.getTitulo(), resultado.getTitulo());
        assertEquals(libro.getIsbn(), resultado.getIsbn());
        assertTrue(resultado.isDisponible());

        verify(libroRepository).findById(1L);
    }

    @Test
    @DisplayName("Debería lanzar LibroNotFoundException al obtener libro con ID inexistente")
    void testObtenerLibroPorId_NotFound() {
        // Given
        when(libroRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        LibroNotFoundException exception = assertThrows(LibroNotFoundException.class, () -> {
            libroService.obtenerLibroPorId(999L);
        });

        assertEquals("Libro no encontrado con ID: 999", exception.getMessage());
        verify(libroRepository).findById(999L);
    }

    @Test
    @DisplayName("Debería actualizar un libro exitosamente cuando ISBN no cambia")
    void testActualizarLibro_Success_MismoIsbn() {
        // Given
        LibroRequestDTO updateDTO = LibroRequestDTO.builder()
                .titulo("Cien Años de Soledad (Edición Especial)")
                .autor("Gabriel García Márquez")
                .isbn("978-0307474728") // Mismo ISBN
                .ejemplaresDisponibles(10)
                .build();

        when(libroRepository.findById(anyLong())).thenReturn(Optional.of(libro));
        when(libroRepository.save(any(Libro.class))).thenAnswer(invocation -> {
            Libro libroActualizado = invocation.getArgument(0);
            return libroActualizado;
        });

        // When
        LibroResponseDTO resultado = libroService.actualizarLibro(1L, updateDTO);

        // Then
        assertNotNull(resultado);
        assertEquals("Cien Años de Soledad (Edición Especial)", resultado.getTitulo());
        assertEquals(10, resultado.getEjemplaresDisponibles());

        verify(libroRepository).findById(1L);
        verify(libroRepository, never()).existsByIsbn(anyString()); // No valida porque ISBN no cambió
        verify(libroRepository).save(any(Libro.class));
    }

    @Test
    @DisplayName("Debería actualizar un libro exitosamente cuando ISBN cambia y es único")
    void testActualizarLibro_Success_NuevoIsbn() {
        // Given
        LibroRequestDTO updateDTO = LibroRequestDTO.builder()
                .titulo("Cien Años de Soledad")
                .autor("Gabriel García Márquez")
                .isbn("978-0307474729") // Nuevo ISBN
                .ejemplaresDisponibles(5)
                .build();

        when(libroRepository.findById(anyLong())).thenReturn(Optional.of(libro));
        when(libroRepository.existsByIsbn(anyString())).thenReturn(false); // Nuevo ISBN no existe
        when(libroRepository.save(any(Libro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        LibroResponseDTO resultado = libroService.actualizarLibro(1L, updateDTO);

        // Then
        assertNotNull(resultado);
        assertEquals("978-0307474729", resultado.getIsbn());

        verify(libroRepository).findById(1L);
        verify(libroRepository).existsByIsbn("978-0307474729");
        verify(libroRepository).save(any(Libro.class));
    }

    @Test
    @DisplayName("Debería lanzar IsbnDuplicadoException al actualizar con ISBN duplicado")
    void testActualizarLibro_IsbnDuplicado() {
        // Given
        LibroRequestDTO updateDTO = LibroRequestDTO.builder()
                .titulo("Cien Años de Soledad")
                .autor("Gabriel García Márquez")
                .isbn("978-8437604572") // ISBN que ya existe
                .ejemplaresDisponibles(5)
                .build();

        when(libroRepository.findById(anyLong())).thenReturn(Optional.of(libro));
        when(libroRepository.existsByIsbn(anyString())).thenReturn(true);

        // When & Then
        IsbnDuplicadoException exception = assertThrows(IsbnDuplicadoException.class, () -> {
            libroService.actualizarLibro(1L, updateDTO);
        });

        assertEquals("El ISBN ya está registrado: " + updateDTO.getIsbn(), exception.getMessage());
        verify(libroRepository).findById(1L);
        verify(libroRepository).existsByIsbn(updateDTO.getIsbn());
        verify(libroRepository, never()).save(any(Libro.class));
    }

    @Test
    @DisplayName("Debería lanzar LibroNotFoundException al actualizar libro inexistente")
    void testActualizarLibro_NotFound() {
        // Given
        when(libroRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        LibroNotFoundException exception = assertThrows(LibroNotFoundException.class, () -> {
            libroService.actualizarLibro(999L, libroRequestDTO);
        });

        assertEquals("Libro no encontrado con ID: 999", exception.getMessage());
        verify(libroRepository).findById(999L);
        verify(libroRepository, never()).existsByIsbn(anyString());
        verify(libroRepository, never()).save(any(Libro.class));
    }

    @Test
    @DisplayName("Debería eliminar un libro exitosamente")
    void testEliminarLibro_Success() {
        // Given
        when(libroRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(libroRepository).deleteById(anyLong());

        // When
        libroService.eliminarLibro(1L);

        // Then
        verify(libroRepository).existsById(1L);
        verify(libroRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Debería lanzar LibroNotFoundException al eliminar libro inexistente")
    void testEliminarLibro_NotFound() {
        // Given
        when(libroRepository.existsById(anyLong())).thenReturn(false);

        // When & Then
        LibroNotFoundException exception = assertThrows(LibroNotFoundException.class, () -> {
            libroService.eliminarLibro(999L);
        });

        assertEquals("Libro no encontrado con ID: 999", exception.getMessage());
        verify(libroRepository).existsById(999L);
        verify(libroRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Debería verificar que un libro existe (true)")
    void testExisteLibro_True() {
        // Given
        when(libroRepository.existsById(anyLong())).thenReturn(true);

        // When
        boolean existe = libroService.existeLibro(1L);

        // Then
        assertTrue(existe);
        verify(libroRepository).existsById(1L);
    }

    @Test
    @DisplayName("Debería verificar que un libro NO existe (false)")
    void testExisteLibro_False() {
        // Given
        when(libroRepository.existsById(anyLong())).thenReturn(false);

        // When
        boolean existe = libroService.existeLibro(999L);

        // Then
        assertFalse(existe);
        verify(libroRepository).existsById(999L);
    }

    @Test
    @DisplayName("Debería verificar que un libro está disponible (true)")
    void testLibroDisponible_True() {
        // Given
        when(libroRepository.findById(anyLong())).thenReturn(Optional.of(libro));

        // When
        boolean disponible = libroService.libroDisponible(1L);

        // Then
        assertTrue(disponible);
        verify(libroRepository).findById(1L);
    }

    @Test
    @DisplayName("Debería verificar que un libro NO está disponible (false) - cero ejemplares")
    void testLibroDisponible_False_CeroEjemplares() {
        // Given
        libro.setEjemplaresDisponibles(0);
        when(libroRepository.findById(anyLong())).thenReturn(Optional.of(libro));

        // When
        boolean disponible = libroService.libroDisponible(1L);

        // Then
        assertFalse(disponible);
        verify(libroRepository).findById(1L);
    }

    @Test
    @DisplayName("Debería verificar que un libro NO está disponible (false) - libro inexistente")
    void testLibroDisponible_False_LibroNoExiste() {
        // Given
        when(libroRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When
        boolean disponible = libroService.libroDisponible(999L);

        // Then
        assertFalse(disponible);
        verify(libroRepository).findById(999L);
    }

    @Test
    @DisplayName("Debería prestar un libro exitosamente (retorna true)")
    void testPrestarLibro_Success() {
        // Given
        int ejemplaresIniciales = libro.getEjemplaresDisponibles();
        when(libroRepository.findById(anyLong())).thenReturn(Optional.of(libro));
        when(libroRepository.save(any(Libro.class))).thenReturn(libro);

        // When
        boolean prestado = libroService.prestarLibro(1L);

        // Then
        assertTrue(prestado);
        assertEquals(ejemplaresIniciales - 1, libro.getEjemplaresDisponibles());
        verify(libroRepository).findById(1L);
        verify(libroRepository).save(libro);
    }

    @Test
    @DisplayName("Debería lanzar LibroNotFoundException al prestar libro inexistente")
    void testPrestarLibro_NotFound() {
        // Given
        when(libroRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        LibroNotFoundException exception = assertThrows(LibroNotFoundException.class, () -> {
            libroService.prestarLibro(999L);
        });

        assertEquals("Libro no encontrado con ID: 999", exception.getMessage());
        verify(libroRepository).findById(999L);
        verify(libroRepository, never()).save(any(Libro.class));
    }

    @Test
    @DisplayName("Debería lanzar LibroNoDisponibleException al prestar libro sin ejemplares disponibles")
    void testPrestarLibro_NoDisponible() {
        // Given
        libro.setEjemplaresDisponibles(0);
        when(libroRepository.findById(anyLong())).thenReturn(Optional.of(libro));

        // When & Then
        LibroNoDisponibleException exception = assertThrows(LibroNoDisponibleException.class, () -> {
            libroService.prestarLibro(1L);
        });

        assertEquals("El libro no tiene ejemplares disponibles: 1", exception.getMessage());
        verify(libroRepository).findById(1L);
        verify(libroRepository, never()).save(any(Libro.class));
    }

    @Test
    @DisplayName("Debería devolver un libro exitosamente")
    void testDevolverLibro_Success() {
        // Given
        int ejemplaresIniciales = libro.getEjemplaresDisponibles();
        when(libroRepository.findById(anyLong())).thenReturn(Optional.of(libro));
        when(libroRepository.save(any(Libro.class))).thenReturn(libro);

        // When
        libroService.devolverLibro(1L);

        // Then
        assertEquals(ejemplaresIniciales + 1, libro.getEjemplaresDisponibles());
        verify(libroRepository).findById(1L);
        verify(libroRepository).save(libro);
    }

    @Test
    @DisplayName("Debería lanzar LibroNotFoundException al devolver libro inexistente")
    void testDevolverLibro_NotFound() {
        // Given
        when(libroRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        LibroNotFoundException exception = assertThrows(LibroNotFoundException.class, () -> {
            libroService.devolverLibro(999L);
        });

        assertEquals("Libro no encontrado con ID: 999", exception.getMessage());
        verify(libroRepository).findById(999L);
        verify(libroRepository, never()).save(any(Libro.class));
    }

    @Test
    @DisplayName("Debería actualizar stock de un libro exitosamente")
    void testActualizarStock_Success() {
        // Given
        int nuevaCantidad = 15;
        when(libroRepository.findById(anyLong())).thenReturn(Optional.of(libro));
        when(libroRepository.save(any(Libro.class))).thenAnswer(invocation -> {
            Libro libroActualizado = invocation.getArgument(0);
            libroActualizado.setEjemplaresDisponibles(nuevaCantidad);
            return libroActualizado;
        });

        // When
        LibroResponseDTO resultado = libroService.actualizarStock(1L, nuevaCantidad);

        // Then
        assertNotNull(resultado);
        assertEquals(nuevaCantidad, resultado.getEjemplaresDisponibles());
        assertTrue(resultado.isDisponible());
        verify(libroRepository).findById(1L);
        verify(libroRepository).save(any(Libro.class));
    }

    @Test
    @DisplayName("Debería lanzar IllegalArgumentException al actualizar stock con cantidad negativa")
    void testActualizarStock_CantidadNegativa() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            libroService.actualizarStock(1L, -5);
        });

        assertEquals("La cantidad no puede ser negativa", exception.getMessage());
        verify(libroRepository, never()).findById(anyLong());
        verify(libroRepository, never()).save(any(Libro.class));
    }

    @Test
    @DisplayName("Debería lanzar LibroNotFoundException al actualizar stock de libro inexistente")
    void testActualizarStock_NotFound() {
        // Given
        when(libroRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        LibroNotFoundException exception = assertThrows(LibroNotFoundException.class, () -> {
            libroService.actualizarStock(999L, 10);
        });

        assertEquals("Libro no encontrado con ID: 999", exception.getMessage());
        verify(libroRepository).findById(999L);
        verify(libroRepository, never()).save(any(Libro.class));
    }

    @Test
    @DisplayName("Debería actualizar stock a cero (libro no disponible)")
    void testActualizarStock_ACero() {
        // Given
        int nuevaCantidad = 0;
        when(libroRepository.findById(anyLong())).thenReturn(Optional.of(libro));
        when(libroRepository.save(any(Libro.class))).thenAnswer(invocation -> {
            Libro libroActualizado = invocation.getArgument(0);
            libroActualizado.setEjemplaresDisponibles(nuevaCantidad);
            return libroActualizado;
        });

        // When
        LibroResponseDTO resultado = libroService.actualizarStock(1L, nuevaCantidad);

        // Then
        assertNotNull(resultado);
        assertEquals(0, resultado.getEjemplaresDisponibles());
        assertFalse(resultado.isDisponible());
        verify(libroRepository).findById(1L);
        verify(libroRepository).save(any(Libro.class));
    }

    @Test
    @DisplayName("Debería retornar lista vacía cuando no hay libros")
    void testObtenerTodosLibros_ListaVacia() {
        // Given
        when(libroRepository.findAll()).thenReturn(List.of());

        // When
        List<LibroResponseDTO> resultado = libroService.obtenerTodosLibros();

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(libroRepository).findAll();
    }

    @Test
    @DisplayName("Debería actualizar libro sin cambiar ejemplares disponibles")
    void testActualizarLibro_SinCambiarEjemplares() {
        // Given
        LibroRequestDTO updateDTO = LibroRequestDTO.builder()
                .titulo("Nuevo Título")
                .autor("Nuevo Autor")
                .isbn("978-0307474728")
                .ejemplaresDisponibles(5) // Misma cantidad
                .build();

        when(libroRepository.findById(anyLong())).thenReturn(Optional.of(libro));
        when(libroRepository.save(any(Libro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        LibroResponseDTO resultado = libroService.actualizarLibro(1L, updateDTO);

        // Then
        assertNotNull(resultado);
        assertEquals("Nuevo Título", resultado.getTitulo());
        assertEquals("Nuevo Autor", resultado.getAutor());
        assertEquals(5, resultado.getEjemplaresDisponibles());
        verify(libroRepository).findById(1L);
        verify(libroRepository, never()).existsByIsbn(anyString());
        verify(libroRepository).save(any(Libro.class));
    }

    @Test
    @DisplayName("Debería mantener disponibilidad false después de actualizar stock a cero")
    void testLibroDisponible_DespuesActualizarStockACero() {
        // Given
        libro.setEjemplaresDisponibles(0);
        when(libroRepository.findById(anyLong())).thenReturn(Optional.of(libro));

        // When
        boolean disponible = libroService.libroDisponible(1L);

        // Then
        assertFalse(disponible);
        verify(libroRepository).findById(1L);
    }
}