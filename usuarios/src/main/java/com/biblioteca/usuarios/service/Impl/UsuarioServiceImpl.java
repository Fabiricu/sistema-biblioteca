package com.biblioteca.usuarios.service.Impl;

import com.biblioteca.usuarios.dto.UsuarioRequestDTO;
import com.biblioteca.usuarios.dto.UsuarioResponseDTO;
import com.biblioteca.usuarios.exception.EmailDuplicadoException;
import com.biblioteca.usuarios.exception.UsuarioNotFoundException;
import com.biblioteca.usuarios.model.entity.Usuario;
import com.biblioteca.usuarios.repository.UsuarioRepository;
import com.biblioteca.usuarios.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public UsuarioResponseDTO crearUsuario(UsuarioRequestDTO usuarioRequestDTO) {
        // Validar email único
        if (usuarioRepository.existsByEmail(usuarioRequestDTO.getEmail())) {
            throw new EmailDuplicadoException("El email ya está registrado: " + usuarioRequestDTO.getEmail());
        }

        Usuario usuario = new Usuario();
        usuario.setNombreCompleto(usuarioRequestDTO.getNombreCompleto());
        usuario.setEmail(usuarioRequestDTO.getEmail());
        usuario.setActivo(true);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return mapToResponseDTO(usuarioGuardado);
    }

    @Override
    public List<UsuarioResponseDTO> obtenerTodosUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UsuarioResponseDTO obtenerUsuarioPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + id));
        return mapToResponseDTO(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO actualizarUsuario(Long id, UsuarioRequestDTO usuarioRequestDTO) {
        // 1. Buscar el usuario existente
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + id));

        // 2. Validar si el email cambió y si es único
        // ¡ESTA VALIDACIÓN ES IMPORTANTE!
        if (!usuarioExistente.getEmail().equals(usuarioRequestDTO.getEmail())) {
            if (usuarioRepository.existsByEmail(usuarioRequestDTO.getEmail())) {
                throw new EmailDuplicadoException("El email ya está registrado: " + usuarioRequestDTO.getEmail());
            }
        }

        // 3. Actualizar los campos
        usuarioExistente.setNombreCompleto(usuarioRequestDTO.getNombreCompleto());
        usuarioExistente.setEmail(usuarioRequestDTO.getEmail());

        // 4. Guardar cambios
        Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);

        // 5. Retornar DTO
        return mapToResponseDTO(usuarioActualizado);
    }

//    @Override
//    @Transactional
//    public UsuarioResponseDTO actualizarUsuario(Long id, UsuarioRequestDTO usuarioRequestDTO) {
//
//        // 1. Buscar el usuario existente
//        Usuario usuarioExistente = usuarioRepository.findById(id)
//                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + id));
//
//        // 2. Validar si el email cambió y si es único
//        if (!usuarioExistente.getEmail().equals(usuarioRequestDTO.getEmail()) &&
//                usuarioRepository.existsByEmail(usuarioRequestDTO.getEmail())) {
//            throw new EmailDuplicadoException("El email ya está registrado: " + usuarioRequestDTO.getEmail());
//        }
//
//        // 3. Actualizar los campos
//        usuarioExistente.setNombreCompleto(usuarioRequestDTO.getNombreCompleto());
//        usuarioExistente.setEmail(usuarioRequestDTO.getEmail());
//
//        // 4. Guardar cambios
//        Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);
//
//        // 5. Retornar DTO
//        return mapToResponseDTO(usuarioActualizado);
//
//    }


    @Override
    @Transactional
    public void desactivarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + id));
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new UsuarioNotFoundException("Usuario no encontrado con ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    @Override
    public boolean existeUsuario(Long id) {
        return usuarioRepository.existsById(id);
    }

    @Override
    public boolean usuarioActivo(Long id) {
        return usuarioRepository.findById(id)
                .map(Usuario::isActivo)
                .orElse(false);
    }

    // Helper method
    private UsuarioResponseDTO mapToResponseDTO(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setNombreCompleto(usuario.getNombreCompleto());
        dto.setEmail(usuario.getEmail());
        dto.setActivo(usuario.isActivo());
        return dto;
    }
}