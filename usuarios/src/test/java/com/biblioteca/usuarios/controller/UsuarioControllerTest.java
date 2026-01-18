package com.biblioteca.usuarios.controller;

import com.biblioteca.usuarios.dto.UsuarioRequestDTO;
import com.biblioteca.usuarios.dto.UsuarioResponseDTO;
import com.biblioteca.usuarios.service.UsuarioService;
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

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private UsuarioRequestDTO requestDTO;
    private UsuarioResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController).build();
        objectMapper = new ObjectMapper();

        requestDTO = new UsuarioRequestDTO();
        requestDTO.setNombreCompleto("Carlos Pérez");
        requestDTO.setEmail("carlos@email.com");

        responseDTO = new UsuarioResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setNombreCompleto("Carlos Pérez");
        responseDTO.setEmail("carlos@email.com");
        responseDTO.setActivo(true);
    }

    @Test
    void crearUsuario_Success() throws Exception {
        when(usuarioService.crearUsuario(any(UsuarioRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombreCompleto").value("Carlos Pérez"))
                .andExpect(jsonPath("$.email").value("carlos@email.com"))
                .andExpect(jsonPath("$.activo").value(true));

        verify(usuarioService, times(1)).crearUsuario(any(UsuarioRequestDTO.class));
    }

    @Test
    void obtenerTodosUsuarios_Success() throws Exception {
        List<UsuarioResponseDTO> usuarios = Arrays.asList(responseDTO);
        when(usuarioService.obtenerTodosUsuarios()).thenReturn(usuarios);

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombreCompleto").value("Carlos Pérez"))
                .andExpect(jsonPath("$[0].email").value("carlos@email.com"));

        verify(usuarioService, times(1)).obtenerTodosUsuarios();
    }

    @Test
    void obtenerUsuarioPorId_Success() throws Exception {
        when(usuarioService.obtenerUsuarioPorId(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombreCompleto").value("Carlos Pérez"));

        verify(usuarioService, times(1)).obtenerUsuarioPorId(1L);
    }

    @Test
    void actualizarUsuario_Success() throws Exception {
        when(usuarioService.actualizarUsuario(eq(1L), any(UsuarioRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(usuarioService, times(1)).actualizarUsuario(eq(1L), any(UsuarioRequestDTO.class));
    }

    @Test
    void desactivarUsuario_Success() throws Exception {
        doNothing().when(usuarioService).desactivarUsuario(1L);

        mockMvc.perform(patch("/api/usuarios/1/desactivar"))
                .andExpect(status().isNoContent());

        verify(usuarioService, times(1)).desactivarUsuario(1L);
    }

    @Test
    void eliminarUsuario_Success() throws Exception {
        doNothing().when(usuarioService).eliminarUsuario(1L);

        mockMvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isNoContent());

        verify(usuarioService, times(1)).eliminarUsuario(1L);
    }

    @Test
    void existeUsuario_Success() throws Exception {
        when(usuarioService.existeUsuario(1L)).thenReturn(true);

        mockMvc.perform(get("/api/usuarios/1/existe"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(usuarioService, times(1)).existeUsuario(1L);
    }

    @Test
    void usuarioActivo_Success() throws Exception {
        when(usuarioService.usuarioActivo(1L)).thenReturn(true);

        mockMvc.perform(get("/api/usuarios/1/activo"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(usuarioService, times(1)).usuarioActivo(1L);
    }
}
