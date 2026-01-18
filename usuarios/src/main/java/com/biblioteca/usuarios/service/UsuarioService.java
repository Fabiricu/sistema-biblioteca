package com.biblioteca.usuarios.service;

import com.biblioteca.usuarios.dto.UsuarioRequestDTO;
import com.biblioteca.usuarios.dto.UsuarioResponseDTO;
import java.util.List;

public interface UsuarioService {
    UsuarioResponseDTO crearUsuario(UsuarioRequestDTO usuarioRequestDTO);
    List<UsuarioResponseDTO> obtenerTodosUsuarios();
    UsuarioResponseDTO obtenerUsuarioPorId(Long id);
    UsuarioResponseDTO actualizarUsuario(Long id, UsuarioRequestDTO usuarioRequestDTO);
    void desactivarUsuario(Long id);
    void eliminarUsuario(Long id);
    boolean existeUsuario(Long id);
    boolean usuarioActivo(Long id);
}
