package com.biblioteca.prestamos.service;

import com.biblioteca.prestamos.controller.dto.PrestamoRequestDTO;
import com.biblioteca.prestamos.controller.dto.PrestamoResponseDTO;
import com.biblioteca.prestamos.controller.dto.DevolucionRequestDTO;

import java.util.List;
import java.util.Map;

public interface PrestamoService {

    PrestamoResponseDTO crearPrestamo(PrestamoRequestDTO request);

    PrestamoResponseDTO obtenerPrestamo(Long id);

    List<PrestamoResponseDTO> obtenerPrestamosPorUsuario(Long usuarioId);

    List<PrestamoResponseDTO> obtenerPrestamosPorLibro(Long libroId);

    List<PrestamoResponseDTO> obtenerPrestamosActivos();

    List<PrestamoResponseDTO> obtenerPrestamosVencidos();

    PrestamoResponseDTO registrarDevolucion(Long prestamoId, DevolucionRequestDTO request);

    void actualizarEstadosAutomaticamente();

    boolean tieneUsuarioPrestamosActivos(Long usuarioId);

    boolean isLibroPrestado(Long libroId);

    long contarPrestamosActivosUsuario(Long usuarioId);

    // NUEVOS (agregar estos):
    List<PrestamoResponseDTO> obtenerTodosPrestamos();
    PrestamoResponseDTO actualizarPrestamo(Long id, PrestamoRequestDTO request);
    void eliminarPrestamo(Long id);
    Map<String, Object> obtenerEstadisticas();
    String testComunicacionConServiciosExternos();
}