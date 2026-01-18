package com.biblioteca.libros.controller;

import com.biblioteca.libros.dto.LibroRequestDTO;
import com.biblioteca.libros.dto.LibroResponseDTO;
import com.biblioteca.libros.service.LibroService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LibroControllerTest {

    @Mock
    private LibroService libroService;

    @InjectMocks
    private LibroController libroController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private LibroRequestDTO requestDTO;
    private LibroResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(libroController).build();
        objectMapper = new ObjectMapper();

        requestDTO = new LibroRequestDTO();
        requestDTO.setTitulo("Cien años de soledad");
        requestDTO.setAutor("Gabriel García Márquez");
        requestDTO.setIsbn("978-0307474728");
        requestDTO.setEjemplaresDisponibles(5);

        responseDTO = new LibroResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setTitulo("Cien años de soledad");
        responseDTO.setAutor("Gabriel García Márquez");
        responseDTO.setIsbn("978-0307474728");
        responseDTO.setEjemplaresDisponibles(5);
        responseDTO.setDisponible(true);
    }

    @Test
    void crearLibro_Success() throws Exception {
        when(libroService.crearLibro(any(LibroRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/libros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Cien años de soledad"))
                .andExpect(jsonPath("$.isbn").value("978-0307474728"));

        verify(libroService, times(1)).crearLibro(any(LibroRequestDTO.class));
    }

    @Test
    void obtenerLibroPorId_Success() throws Exception {
        when(libroService.obtenerLibroPorId(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/libros/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(libroService, times(1)).obtenerLibroPorId(1L);
    }

    @Test
    void existeLibro_Success() throws Exception {
        when(libroService.existeLibro(1L)).thenReturn(true);

        mockMvc.perform(get("/api/libros/1/existe"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(libroService, times(1)).existeLibro(1L);
    }

    @Test
    void libroDisponible_Success() throws Exception {
        when(libroService.libroDisponible(1L)).thenReturn(true);

        mockMvc.perform(get("/api/libros/1/disponible"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(libroService, times(1)).libroDisponible(1L);
    }
}