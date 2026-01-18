package com.biblioteca.libros.service;

import com.biblioteca.libros.dto.LibroRequestDTO;
import com.biblioteca.libros.dto.LibroResponseDTO;

import java.util.List;

public interface LibroService {

    LibroResponseDTO crearLibro(LibroRequestDTO libroRequestDTO);

    List<LibroResponseDTO> obtenerTodosLibros();

    LibroResponseDTO obtenerLibroPorId(Long id);

    LibroResponseDTO actualizarLibro(Long id, LibroRequestDTO libroRequestDTO);

    void eliminarLibro(Long id);

    boolean existeLibro(Long id);

    boolean libroDisponible(Long id);

    boolean prestarLibro(Long id);

    void devolverLibro(Long id);

    LibroResponseDTO actualizarStock(Long id, int nuevaCantidad);
}