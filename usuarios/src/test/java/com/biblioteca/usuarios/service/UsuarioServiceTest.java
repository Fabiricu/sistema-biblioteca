package com.biblioteca.usuarios.service;

import com.biblioteca.usuarios.dto.UsuarioRequestDTO;
import com.biblioteca.usuarios.dto.UsuarioResponseDTO;
import com.biblioteca.usuarios.exception.EmailDuplicadoException;
import com.biblioteca.usuarios.exception.UsuarioNotFoundException;
import com.biblioteca.usuarios.model.entity.Usuario;
import com.biblioteca.usuarios.repository.UsuarioRepository;
import com.biblioteca.usuarios.service.Impl.UsuarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private UsuarioRequestDTO requestDTO;
    private Usuario usuario;
    private UsuarioResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        requestDTO = new UsuarioRequestDTO();
        requestDTO.setNombreCompleto("Ana García");
        requestDTO.setEmail("ana@email.com");

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombreCompleto("Ana García");
        usuario.setEmail("ana@email.com");
        usuario.setActivo(true);

        responseDTO = new UsuarioResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setNombreCompleto("Ana García");
        responseDTO.setEmail("ana@email.com");
        responseDTO.setActivo(true);
    }

    @Test
    void crearUsuario_Success() {
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        UsuarioResponseDTO result = usuarioService.crearUsuario(requestDTO);

        assertNotNull(result);
        assertEquals("Ana García", result.getNombreCompleto());
        assertEquals("ana@email.com", result.getEmail());
        assertTrue(result.isActivo());

        verify(usuarioRepository, times(1)).existsByEmail(anyString());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }



    @Test
    void crearUsuario_EmailDuplicado_ThrowsException() {
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(EmailDuplicadoException.class, () -> {
            usuarioService.crearUsuario(requestDTO);
        });

        verify(usuarioRepository, times(1)).existsByEmail(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void obtenerTodosUsuarios_Success() {
        List<Usuario> usuarios = Arrays.asList(usuario);
        when(usuarioRepository.findAll()).thenReturn(usuarios);

        List<UsuarioResponseDTO> result = usuarioService.obtenerTodosUsuarios();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Ana García", result.get(0).getNombreCompleto());

        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    void obtenerUsuarioPorId_Success() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        UsuarioResponseDTO result = usuarioService.obtenerUsuarioPorId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Ana García", result.getNombreCompleto());

        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    void obtenerUsuarioPorId_NotFound_ThrowsException() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UsuarioNotFoundException.class, () -> {
            usuarioService.obtenerUsuarioPorId(999L);
        });

        verify(usuarioRepository, times(1)).findById(999L);
    }

    @Test
    void actualizarUsuario_Success() {
        // Configuración
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // ¡IMPORTANTE! Configurar existsByEmail para cuando el email es DIFERENTE
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);

        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Crear un request DTO con email DIFERENTE al existente
        UsuarioRequestDTO requestConEmailDiferente = new UsuarioRequestDTO();
        requestConEmailDiferente.setNombreCompleto("Ana García Actualizada");
        requestConEmailDiferente.setEmail("ana.nuevo@email.com"); // ¡Email diferente!

        UsuarioResponseDTO result = usuarioService.actualizarUsuario(1L, requestConEmailDiferente);

        // Verificaciones
        assertNotNull(result);
        assertEquals("Ana García Actualizada", result.getNombreCompleto());

        // Verificar que se llamó a existsByEmail (porque el email cambió)
        verify(usuarioRepository, times(1)).existsByEmail(anyString());
        verify(usuarioRepository, times(1)).findById(1L);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

//    @Test
//    void actualizarUsuario_Success() {
//        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
//        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
//        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
//
//        UsuarioResponseDTO result = usuarioService.actualizarUsuario(1L, requestDTO);
//
//        assertNotNull(result);
//        assertEquals("Ana García", result.getNombreCompleto());
//
//        verify(usuarioRepository, times(1)).findById(1L);
//        verify(usuarioRepository, times(1)).existsByEmail(anyString());
//        verify(usuarioRepository, times(1)).save(any(Usuario.class));
//    }

    @Test
    void desactivarUsuario_Success() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        usuarioService.desactivarUsuario(1L);

        assertFalse(usuario.isActivo());
        verify(usuarioRepository, times(1)).findById(1L);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void eliminarUsuario_Success() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        doNothing().when(usuarioRepository).deleteById(1L);

        usuarioService.eliminarUsuario(1L);

        verify(usuarioRepository, times(1)).existsById(1L);
        verify(usuarioRepository, times(1)).deleteById(1L);
    }

    @Test
    void existeUsuario_Success() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);

        boolean result = usuarioService.existeUsuario(1L);

        assertTrue(result);
        verify(usuarioRepository, times(1)).existsById(1L);
    }

    @Test
    void usuarioActivo_Success() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        boolean result = usuarioService.usuarioActivo(1L);

        assertTrue(result);
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    void usuarioActivo_NotFound_ReturnsFalse() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = usuarioService.usuarioActivo(999L);

        assertFalse(result);
        verify(usuarioRepository, times(1)).findById(999L);
    }
}
