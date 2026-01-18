package com.biblioteca.prestamos.service;

import com.biblioteca.prestamos.client.LibrosClient;
import com.biblioteca.prestamos.client.dto.LibroResponseDto;
import com.biblioteca.prestamos.controller.dto.PrestamoRequestDTO;
import com.biblioteca.prestamos.controller.dto.PrestamoResponseDTO;
import com.biblioteca.prestamos.controller.dto.DevolucionRequestDTO;
import com.biblioteca.prestamos.exception.LibroNoDisponibleException;
import com.biblioteca.prestamos.model.entity.Prestamo;
import com.biblioteca.prestamos.model.enums.EstadoPrestamo;
import com.biblioteca.prestamos.repository.PrestamoRepository;
import com.biblioteca.prestamos.service.impl.PrestamoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrestamoServiceTest {

    @Mock
    private PrestamoRepository prestamoRepository;

    @Mock
    private LibrosClient librosClient;

    @InjectMocks
    private PrestamoServiceImpl prestamoService;

    private PrestamoRequestDTO prestamoRequestDTO;
    private LibroResponseDto libroResponseDto;
    private Prestamo prestamo;

    @BeforeEach
    void setUp() {
        prestamoRequestDTO = PrestamoRequestDTO.builder()
                .libroId(1L)
                .usuarioId(1L)
                .fechaDevolucionPrevista(LocalDate.now().plusDays(14))
                .observaciones("Préstamo de prueba")
                .build();

        libroResponseDto = LibroResponseDto.builder()
                .id(1L)
                .titulo("Libro de Prueba")
                .autor("Autor de Prueba")
                .isbn("1234567890")
                .ejemplaresDisponibles(5)
                .disponible(true)
                .build();

        prestamo = Prestamo.builder()
                .id(1L)
                .libroId(1L)
                .usuarioId(1L)
                .fechaPrestamo(LocalDate.now())
                .fechaDevolucionPrevista(LocalDate.now().plusDays(14))
                .estado(EstadoPrestamo.ACTIVO)
                .observaciones("Préstamo de prueba")
                .build();
    }

    @Test
    void crearPrestamo_Success() {
        when(librosClient.verificarDisponibilidad(anyLong()))
                .thenReturn(ResponseEntity.ok(true));
        when(librosClient.obtenerLibro(anyLong()))
                .thenReturn(ResponseEntity.ok(libroResponseDto));
        when(prestamoRepository.countByUsuarioIdAndEstado(anyLong(), any()))
                .thenReturn(0L);
        when(prestamoRepository.save(any(Prestamo.class)))
                .thenReturn(prestamo);

        PrestamoResponseDTO result = prestamoService.crearPrestamo(prestamoRequestDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Libro de Prueba", result.getTituloLibro());
        verify(librosClient).prestarLibro(anyLong());
        verify(prestamoRepository).save(any(Prestamo.class));
    }

    @Test
    void crearPrestamo_LibroNoDisponible_ThrowsException() {
        when(librosClient.verificarDisponibilidad(anyLong()))
                .thenReturn(ResponseEntity.ok(false));

        assertThrows(LibroNoDisponibleException.class, () -> {
            prestamoService.crearPrestamo(prestamoRequestDTO);
        });
    }

    @Test
    void obtenerPrestamo_Success() {
        when(prestamoRepository.findById(anyLong()))
                .thenReturn(Optional.of(prestamo));
        when(librosClient.obtenerLibro(anyLong()))
                .thenReturn(ResponseEntity.ok(libroResponseDto));

        PrestamoResponseDTO result = prestamoService.obtenerPrestamo(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Libro de Prueba", result.getTituloLibro());
    }

    @Test
    void registrarDevolucion_Success() {
        DevolucionRequestDTO devolucionRequest = DevolucionRequestDTO.builder()
                .observaciones("Devolución en buen estado")
                .libroPerdido(false)
                .build();

        when(prestamoRepository.findById(anyLong()))
                .thenReturn(Optional.of(prestamo));
        when(prestamoRepository.save(any(Prestamo.class)))
                .thenReturn(prestamo);
        when(librosClient.obtenerLibro(anyLong()))
                .thenReturn(ResponseEntity.ok(libroResponseDto));

        PrestamoResponseDTO result = prestamoService.registrarDevolucion(1L, devolucionRequest);

        assertNotNull(result);
        assertEquals(EstadoPrestamo.DEVUELTO, result.getEstado());
        assertNotNull(result.getFechaDevolucionReal());
        verify(librosClient).devolverLibro(anyLong());
    }

    @Test
    void isLibroPrestado_ReturnsTrue() {
        when(prestamoRepository.isLibroPrestado(anyLong()))
                .thenReturn(true);

        boolean result = prestamoService.isLibroPrestado(1L);

        assertTrue(result);
    }

    @Test
    void contarPrestamosActivosUsuario_ReturnsCount() {
        when(prestamoRepository.countByUsuarioIdAndEstado(anyLong(), any()))
                .thenReturn(3L);

        long result = prestamoService.contarPrestamosActivosUsuario(1L);

        assertEquals(3L, result);
    }
}
