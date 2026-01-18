package com.biblioteca.libros.service.impl;

import com.biblioteca.libros.dto.LibroRequestDTO;
import com.biblioteca.libros.dto.LibroResponseDTO;
import com.biblioteca.libros.exception.IsbnDuplicadoException;
import com.biblioteca.libros.exception.LibroNotFoundException;
import com.biblioteca.libros.exception.LibroNoDisponibleException;
import com.biblioteca.libros.model.entity.Libro;
import com.biblioteca.libros.repository.LibroRepository;
import com.biblioteca.libros.service.LibroService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LibroServiceImpl implements LibroService {

    private final LibroRepository libroRepository;

    @Override
    @Transactional
    public LibroResponseDTO crearLibro(LibroRequestDTO libroRequestDTO) {
        // Validar ISBN único
        if (libroRepository.existsByIsbn(libroRequestDTO.getIsbn())) {
            throw new IsbnDuplicadoException("El ISBN ya está registrado: " + libroRequestDTO.getIsbn());
        }

        Libro libro = Libro.builder()
                .titulo(libroRequestDTO.getTitulo())
                .autor(libroRequestDTO.getAutor())
                .isbn(libroRequestDTO.getIsbn())
                .ejemplaresDisponibles(libroRequestDTO.getEjemplaresDisponibles())
                .build();

        Libro libroGuardado = libroRepository.save(libro);
        return mapToResponseDTO(libroGuardado);
    }

    @Override
    public List<LibroResponseDTO> obtenerTodosLibros() {
        return libroRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LibroResponseDTO obtenerLibroPorId(Long id) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new LibroNotFoundException("Libro no encontrado con ID: " + id));
        return mapToResponseDTO(libro);
    }

    @Override
    @Transactional
    public LibroResponseDTO actualizarLibro(Long id, LibroRequestDTO libroRequestDTO) {
        // 1. Buscar el libro existente
        Libro libroExistente = libroRepository.findById(id)
                .orElseThrow(() -> new LibroNotFoundException("Libro no encontrado con ID: " + id));

        // 2. Validar si el ISBN cambió y si es único
        if (!libroExistente.getIsbn().equals(libroRequestDTO.getIsbn())) {
            if (libroRepository.existsByIsbn(libroRequestDTO.getIsbn())) {
                throw new IsbnDuplicadoException("El ISBN ya está registrado: " + libroRequestDTO.getIsbn());
            }
        }

        // 3. Actualizar los campos
        libroExistente.setTitulo(libroRequestDTO.getTitulo());
        libroExistente.setAutor(libroRequestDTO.getAutor());
        libroExistente.setIsbn(libroRequestDTO.getIsbn());
        libroExistente.setEjemplaresDisponibles(libroRequestDTO.getEjemplaresDisponibles());

        // 4. Guardar cambios
        Libro libroActualizado = libroRepository.save(libroExistente);

        // 5. Retornar DTO
        return mapToResponseDTO(libroActualizado);
    }

    @Override
    @Transactional
    public void eliminarLibro(Long id) {
        if (!libroRepository.existsById(id)) {
            throw new LibroNotFoundException("Libro no encontrado con ID: " + id);
        }
        libroRepository.deleteById(id);
    }

    @Override
    public boolean existeLibro(Long id) {
        return libroRepository.existsById(id);
    }

    @Override
    public boolean libroDisponible(Long id) {
        return libroRepository.findById(id)
                .map(Libro::estaDisponible)
                .orElse(false);
    }

    @Override
    @Transactional
    public boolean prestarLibro(Long id) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new LibroNotFoundException("Libro no encontrado con ID: " + id));

        if (!libro.estaDisponible()) {
            throw new LibroNoDisponibleException("El libro no tiene ejemplares disponibles: " + id);
        }

        boolean prestado = libro.prestarEjemplar();
        if (prestado) {
            libroRepository.save(libro);
        }
        return prestado;
    }

    @Override
    @Transactional
    public void devolverLibro(Long id) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new LibroNotFoundException("Libro no encontrado con ID: " + id));

        libro.devolverEjemplar();
        libroRepository.save(libro);
    }

    @Override
    @Transactional
    public LibroResponseDTO actualizarStock(Long id, int nuevaCantidad) {
        if (nuevaCantidad < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }

        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new LibroNotFoundException("Libro no encontrado con ID: " + id));

        libro.setEjemplaresDisponibles(nuevaCantidad);
        Libro libroActualizado = libroRepository.save(libro);

        return mapToResponseDTO(libroActualizado);
    }

    // Helper method
    private LibroResponseDTO mapToResponseDTO(Libro libro) {
        LibroResponseDTO dto = new LibroResponseDTO();
        dto.setId(libro.getId());
        dto.setTitulo(libro.getTitulo());
        dto.setAutor(libro.getAutor());
        dto.setIsbn(libro.getIsbn());
        dto.setEjemplaresDisponibles(libro.getEjemplaresDisponibles());
        dto.setDisponible(libro.estaDisponible());
        return dto;
    }
}