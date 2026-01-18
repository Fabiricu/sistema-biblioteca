package com.biblioteca.prestamos.controller;

import com.biblioteca.prestamos.controller.dto.PrestamoRequestDTO;
import com.biblioteca.prestamos.controller.dto.PrestamoResponseDTO;
import com.biblioteca.prestamos.controller.dto.DevolucionRequestDTO;
import com.biblioteca.prestamos.model.enums.EstadoPrestamo;
import com.biblioteca.prestamos.service.PrestamoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PrestamoController.class)
@DisplayName("Pruebas del Controlador de Préstamos")
class PrestamoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PrestamoService prestamoService;

    private PrestamoRequestDTO prestamoRequestDTO;
    private PrestamoResponseDTO prestamoResponseDTO;
    private DevolucionRequestDTO devolucionRequestDTO;

    @BeforeEach
    void setUp() {
        // Configurar DTOs de prueba
        prestamoRequestDTO = PrestamoRequestDTO.builder()
                .libroId(1L)
                .usuarioId(1L)
                .fechaDevolucionPrevista(LocalDate.now().plusDays(14))
                .observaciones("Préstamo de prueba")
                .build();

        prestamoResponseDTO = PrestamoResponseDTO.builder()
                .id(1L)
                .libroId(1L)
                .tituloLibro("Cien años de soledad")
                .usuarioId(1L)
                .fechaPrestamo(LocalDate.now())
                .fechaDevolucionPrevista(LocalDate.now().plusDays(14))
                .estado(EstadoPrestamo.ACTIVO)
                .diasRetraso(0)
                .observaciones("Préstamo de prueba")
                .vencido(false)
                .build();

        devolucionRequestDTO = DevolucionRequestDTO.builder()
                .observaciones("Devolución en buen estado")
                .libroPerdido(false)
                .build();
    }

    @Test
    @DisplayName("Debería crear un préstamo exitosamente")
    void crearPrestamo_Success() throws Exception {
        // Given
        when(prestamoService.crearPrestamo(any(PrestamoRequestDTO.class)))
                .thenReturn(prestamoResponseDTO);

        // When & Then
        mockMvc.perform(post("/api/prestamos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prestamoRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tituloLibro").value("Cien años de soledad"))
                .andExpect(jsonPath("$.usuarioId").value(1))
                .andExpect(jsonPath("$.estado").value("ACTIVO"))
                .andExpect(jsonPath("$.vencido").value(false));
    }

    @Test
    @DisplayName("Debería retornar error cuando los datos del préstamo son inválidos")
    void crearPrestamo_InvalidData() throws Exception {
        // Given - Datos inválidos (libroId negativo)
        PrestamoRequestDTO invalidRequest = PrestamoRequestDTO.builder()
                .libroId(-1L)  // Inválido: negativo
                .usuarioId(1L)
                .fechaDevolucionPrevista(LocalDate.now().minusDays(1))  // Inválido: fecha pasada
                .build();

        // When & Then
        mockMvc.perform(post("/api/prestamos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Debería obtener un préstamo por ID")
    void obtenerPrestamo_Success() throws Exception {
        // Given
        when(prestamoService.obtenerPrestamo(anyLong()))
                .thenReturn(prestamoResponseDTO);

        // When & Then
        mockMvc.perform(get("/api/prestamos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tituloLibro").value("Cien años de soledad"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    @Test
    @DisplayName("Debería obtener todos los préstamos de un usuario")
    void obtenerPrestamosPorUsuario_Success() throws Exception {
        // Given
        List<PrestamoResponseDTO> prestamos = Arrays.asList(
                prestamoResponseDTO,
                PrestamoResponseDTO.builder()
                        .id(2L)
                        .libroId(2L)
                        .tituloLibro("Rayuela")
                        .usuarioId(1L)
                        .fechaPrestamo(LocalDate.now())
                        .fechaDevolucionPrevista(LocalDate.now().plusDays(14))
                        .estado(EstadoPrestamo.ACTIVO)
                        .build()
        );

        when(prestamoService.obtenerPrestamosPorUsuario(anyLong()))
                .thenReturn(prestamos);

        // When & Then
        mockMvc.perform(get("/api/prestamos/usuario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].usuarioId").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].tituloLibro").value("Rayuela"));
    }

    @Test
    @DisplayName("Debería obtener todos los préstamos de un libro")
    void obtenerPrestamosPorLibro_Success() throws Exception {
        // Given
        List<PrestamoResponseDTO> prestamos = Arrays.asList(
                prestamoResponseDTO,
                PrestamoResponseDTO.builder()
                        .id(3L)
                        .libroId(1L)  // Mismo libro
                        .tituloLibro("Cien años de soledad")
                        .usuarioId(2L)  // Diferente usuario
                        .fechaPrestamo(LocalDate.now().minusDays(10))
                        .fechaDevolucionPrevista(LocalDate.now().plusDays(4))
                        .estado(EstadoPrestamo.DEVUELTO)
                        .build()
        );

        when(prestamoService.obtenerPrestamosPorLibro(anyLong()))
                .thenReturn(prestamos);

        // When & Then
        mockMvc.perform(get("/api/prestamos/libro/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].libroId").value(1))
                .andExpect(jsonPath("$[1].libroId").value(1))
                .andExpect(jsonPath("$[1].estado").value("DEVUELTO"));
    }

    @Test
    @DisplayName("Debería obtener préstamos activos")
    void obtenerPrestamosActivos_Success() throws Exception {
        // Given
        List<PrestamoResponseDTO> prestamosActivos = Arrays.asList(
                prestamoResponseDTO,
                PrestamoResponseDTO.builder()
                        .id(2L)
                        .libroId(3L)
                        .tituloLibro("El Aleph")
                        .usuarioId(2L)
                        .fechaPrestamo(LocalDate.now())
                        .fechaDevolucionPrevista(LocalDate.now().plusDays(14))
                        .estado(EstadoPrestamo.ACTIVO)
                        .build()
        );

        when(prestamoService.obtenerPrestamosActivos())
                .thenReturn(prestamosActivos);

        // When & Then
        mockMvc.perform(get("/api/prestamos/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].estado").value("ACTIVO"))
                .andExpect(jsonPath("$[1].estado").value("ACTIVO"));
    }

    @Test
    @DisplayName("Debería obtener préstamos vencidos")
    void obtenerPrestamosVencidos_Success() throws Exception {
        // Given
        PrestamoResponseDTO prestamoVencido = PrestamoResponseDTO.builder()
                .id(3L)
                .libroId(4L)
                .tituloLibro("1984")
                .usuarioId(3L)
                .fechaPrestamo(LocalDate.now().minusDays(20))
                .fechaDevolucionPrevista(LocalDate.now().minusDays(6))
                .estado(EstadoPrestamo.ACTIVO)
                .diasRetraso(6)
                .vencido(true)
                .build();

        when(prestamoService.obtenerPrestamosVencidos())
                .thenReturn(Arrays.asList(prestamoVencido));

        // When & Then
        mockMvc.perform(get("/api/prestamos/vencidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].vencido").value(true))
                .andExpect(jsonPath("$[0].diasRetraso").value(6));
    }

    @Test
    @DisplayName("Debería registrar devolución de un préstamo")
    void registrarDevolucion_Success() throws Exception {
        // Given
        PrestamoResponseDTO prestamoDevuelto = PrestamoResponseDTO.builder()
                .id(1L)
                .libroId(1L)
                .tituloLibro("Cien años de soledad")
                .usuarioId(1L)
                .fechaPrestamo(LocalDate.now().minusDays(10))
                .fechaDevolucionPrevista(LocalDate.now().plusDays(4))
                .fechaDevolucionReal(LocalDate.now())
                .estado(EstadoPrestamo.DEVUELTO)
                .observaciones("Devolución en buen estado")
                .vencido(false)
                .build();

        when(prestamoService.registrarDevolucion(anyLong(), any(DevolucionRequestDTO.class)))
                .thenReturn(prestamoDevuelto);

        // When & Then
        mockMvc.perform(put("/api/prestamos/1/devolucion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(devolucionRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estado").value("DEVUELTO"))
                .andExpect(jsonPath("$.fechaDevolucionReal").exists())
                .andExpect(jsonPath("$.observaciones").value("Devolución en buen estado"));
    }

    @Test
    @DisplayName("Debería verificar si un usuario tiene préstamos activos")
    void tieneUsuarioPrestamosActivos_Success() throws Exception {
        // Given
        when(prestamoService.tieneUsuarioPrestamosActivos(anyLong()))
                .thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/prestamos/usuario/1/activos"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("Debería verificar si un libro está prestado")
    void isLibroPrestado_Success() throws Exception {
        // Given
        when(prestamoService.isLibroPrestado(anyLong()))
                .thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/prestamos/libro/1/prestado"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("Debería contar préstamos activos de un usuario")
    void contarPrestamosActivosUsuario_Success() throws Exception {
        // Given
        when(prestamoService.contarPrestamosActivosUsuario(anyLong()))
                .thenReturn(3L);

        // When & Then
        mockMvc.perform(get("/api/prestamos/usuario/1/contar-activos"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }

    @Test
    @DisplayName("Debería retornar error 404 cuando no encuentra un préstamo")
    void obtenerPrestamo_NotFound() throws Exception {
        // Given
        when(prestamoService.obtenerPrestamo(anyLong()))
                .thenThrow(new com.biblioteca.prestamos.exception.PrestamoNotFoundException(999L));

        // When & Then
        mockMvc.perform(get("/api/prestamos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Debería registrar devolución de libro perdido")
    void registrarDevolucion_LibroPerdido() throws Exception {
        // Given
        DevolucionRequestDTO devolucionPerdida = DevolucionRequestDTO.builder()
                .observaciones("Libro perdido por el usuario")
                .libroPerdido(true)
                .build();

        PrestamoResponseDTO prestamoPerdido = PrestamoResponseDTO.builder()
                .id(1L)
                .libroId(1L)
                .tituloLibro("Libro Perdido")
                .usuarioId(1L)
                .fechaPrestamo(LocalDate.now().minusDays(30))
                .fechaDevolucionPrevista(LocalDate.now().minusDays(16))
                .fechaDevolucionReal(LocalDate.now())
                .estado(EstadoPrestamo.PERDIDO)
                .observaciones("Libro perdido por el usuario")
                .diasRetraso(16)
                .vencido(true)
                .build();

        when(prestamoService.registrarDevolucion(anyLong(), any(DevolucionRequestDTO.class)))
                .thenReturn(prestamoPerdido);

        // When & Then
        mockMvc.perform(put("/api/prestamos/1/devolucion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(devolucionPerdida)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PERDIDO"))
                .andExpect(jsonPath("$.observaciones").value("Libro perdido por el usuario"));
    }
}
