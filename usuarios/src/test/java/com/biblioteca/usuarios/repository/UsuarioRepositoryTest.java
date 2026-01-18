package com.biblioteca.usuarios.repository;

import com.biblioteca.usuarios.model.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void findByEmail_WhenUserExists_ReturnsUser() {
        // Given
        Usuario usuario = Usuario.builder()
                .nombreCompleto("Test User")
                .email("test@email.com")
                .activo(true)
                .build();

        usuarioRepository.save(usuario);

        // When
        Optional<Usuario> found = usuarioRepository.findByEmail("test@email.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@email.com");
        assertThat(found.get().getNombreCompleto()).isEqualTo("Test User");
    }

    @Test
    void findByEmail_WhenUserNotExists_ReturnsEmpty() {
        // When
        Optional<Usuario> found = usuarioRepository.findByEmail("nonexistent@email.com");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void existsByEmail_WhenEmailExists_ReturnsTrue() {
        // Given
        Usuario usuario = Usuario.builder()
                .nombreCompleto("Test User")
                .email("exists@email.com")
                .activo(true)
                .build();

        usuarioRepository.save(usuario);

        // When
        boolean exists = usuarioRepository.existsByEmail("exists@email.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_WhenEmailNotExists_ReturnsFalse() {
        // When
        boolean exists = usuarioRepository.existsByEmail("notexists@email.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void saveUser_Success() {
        // Given
        Usuario usuario = Usuario.builder()
                .nombreCompleto("New User")
                .email("new@email.com")
                .activo(true)
                .build();

        // When
        Usuario saved = usuarioRepository.save(usuario);

        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("new@email.com");
    }

    @Test
    void findAll_ReturnsAllUsers() {
        // Given
        Usuario usuario1 = Usuario.builder()
                .nombreCompleto("User 1")
                .email("user1@email.com")
                .activo(true)
                .build();

        Usuario usuario2 = Usuario.builder()
                .nombreCompleto("User 2")
                .email("user2@email.com")
                .activo(true)
                .build();

        usuarioRepository.save(usuario1);
        usuarioRepository.save(usuario2);

        // When
        long count = usuarioRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }
}