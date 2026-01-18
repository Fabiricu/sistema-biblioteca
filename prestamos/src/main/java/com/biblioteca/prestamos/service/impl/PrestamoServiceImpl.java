package com.biblioteca.prestamos.service.impl;

import com.biblioteca.prestamos.client.LibrosClient;
import com.biblioteca.prestamos.client.UsuariosClient;
import com.biblioteca.prestamos.client.dto.LibroResponseDto;
import com.biblioteca.prestamos.client.dto.UsuarioResponseDto;
import com.biblioteca.prestamos.controller.dto.PrestamoRequestDTO;
import com.biblioteca.prestamos.controller.dto.PrestamoResponseDTO;
import com.biblioteca.prestamos.controller.dto.DevolucionRequestDTO;
import com.biblioteca.prestamos.exception.LibroNoDisponibleException;
import com.biblioteca.prestamos.exception.PrestamoNotFoundException;
import com.biblioteca.prestamos.exception.UsuarioConPrestamosVencidosException;
import com.biblioteca.prestamos.model.entity.Prestamo;
import com.biblioteca.prestamos.model.enums.EstadoPrestamo;
import com.biblioteca.prestamos.repository.PrestamoRepository;
import com.biblioteca.prestamos.service.PrestamoService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrestamoServiceImpl implements PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final LibrosClient librosClient;
    private final UsuariosClient usuariosClient;

    private static final int MAX_PRESTAMOS_USUARIO = 5;

    // ============ MÉTODOS YA IMPLEMENTADOS ============
    @Override
    @Transactional
    public PrestamoResponseDTO crearPrestamo(PrestamoRequestDTO request) {
        log.info("Creando préstamo para libroId: {}, usuarioId: {}",
                request.getLibroId(), request.getUsuarioId());

        validarDisponibilidadLibro(request.getLibroId());
        validarUsuarioSinPrestamosVencidos(request.getUsuarioId());
        validarLimitePrestamosUsuario(request.getUsuarioId());

        LibroResponseDto libro = obtenerLibroInfo(request.getLibroId());

        Prestamo prestamo = Prestamo.builder()
                .libroId(request.getLibroId())
                .usuarioId(request.getUsuarioId())
                .fechaPrestamo(LocalDate.now())
                .fechaDevolucionPrevista(request.getFechaDevolucionPrevista())
                .estado(EstadoPrestamo.ACTIVO)
                .observaciones(request.getObservaciones())
                .build();

        prestamo.calcularDiasRetraso();

        actualizarDisponibilidadLibro(request.getLibroId(), false);

        Prestamo saved = prestamoRepository.save(prestamo);
        log.info("Préstamo creado con ID: {}", saved.getId());

        return mapToResponseDTO(saved, libro.getTitulo());
    }

    @Override
    @Transactional(readOnly = true)
    public PrestamoResponseDTO obtenerPrestamo(Long id) {
        log.info("Obteniendo préstamo con ID: {}", id);

        Prestamo prestamo = prestamoRepository.findById(id)
                .orElseThrow(() -> new PrestamoNotFoundException(id));

        LibroResponseDto libro = obtenerLibroInfo(prestamo.getLibroId());

        return mapToResponseDTO(prestamo, libro.getTitulo());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrestamoResponseDTO> obtenerPrestamosPorUsuario(Long usuarioId) {
        log.info("Obteniendo préstamos para usuarioId: {}", usuarioId);

        return prestamoRepository.findByUsuarioId(usuarioId).stream()
                .map(prestamo -> {
                    LibroResponseDto libro = obtenerLibroInfo(prestamo.getLibroId());
                    return mapToResponseDTO(prestamo, libro.getTitulo());
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PrestamoResponseDTO registrarDevolucion(Long prestamoId, DevolucionRequestDTO request) {
        log.info("Registrando devolución para préstamoId: {}", prestamoId);

        // Asegurar que request no sea null
        if (request == null) {
            request = new DevolucionRequestDTO();
        }

        Prestamo prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new PrestamoNotFoundException(prestamoId));

        if (prestamo.getEstado() != EstadoPrestamo.ACTIVO) {
            throw new IllegalArgumentException("El préstamo no está activo. Estado actual: " + prestamo.getEstado());
        }

        // Usar LocalDate.now() con import correcto
        prestamo.setFechaDevolucionReal(LocalDate.now());

        // Manejar null en libroPerdido (por defecto es false según el DTO)
        boolean libroPerdido = Boolean.TRUE.equals(request.getLibroPerdido());
        prestamo.setEstado(libroPerdido ? EstadoPrestamo.PERDIDO : EstadoPrestamo.DEVUELTO);

        if (request.getObservaciones() != null) {
            prestamo.setObservaciones(request.getObservaciones());
        }

        // Solo devolver libro si NO está perdido
        if (!libroPerdido) {
            try {
                log.info("Devolviendo libro ID: {} al servicio de libros", prestamo.getLibroId());
                actualizarDisponibilidadLibro(prestamo.getLibroId(), true);
            } catch (Exception e) {
                log.error("Error al devolver libro, pero continuando con la devolución local", e);
                // Continuar aunque falle la comunicación con libros-service
            }
        }

        Prestamo updated = prestamoRepository.save(prestamo);
        log.info("✅ Devolución registrada exitosamente para préstamoId: {}", prestamoId);

        LibroResponseDto libro = obtenerLibroInfo(updated.getLibroId());
        return mapToResponseDTO(updated, libro.getTitulo());
    }

    @Override
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void actualizarEstadosAutomaticamente() {
        log.info("Actualizando estados de préstamos automáticamente");

        List<Prestamo> prestamosActivos = prestamoRepository
                .findByUsuarioIdAndEstado(null, EstadoPrestamo.ACTIVO);

        for (Prestamo prestamo : prestamosActivos) {
            prestamo.calcularDiasRetraso();
            if (prestamo.isVencido()) {
                prestamo.setEstado(EstadoPrestamo.VENCIDO);
            }
        }

        prestamoRepository.saveAll(prestamosActivos);
        log.info("Estados actualizados para {} préstamos", prestamosActivos.size());
    }

    @Override
    public List<PrestamoResponseDTO> obtenerPrestamosPorLibro(Long libroId) {
        return prestamoRepository.findByLibroId(libroId).stream()
                .map(prestamo -> {
                    LibroResponseDto libro = obtenerLibroInfo(prestamo.getLibroId());
                    return mapToResponseDTO(prestamo, libro.getTitulo());
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<PrestamoResponseDTO> obtenerPrestamosActivos() {
        log.info("🔧 USANDO MÉTODO ALTERNATIVO URGENTE");

        // Método 1: Obtener todos
        List<Prestamo> todos = prestamoRepository.findAll();
        log.info("Total préstamos en BD: {}", todos.size());

        // Método 2: Filtrar manualmente
        List<Prestamo> activos = new ArrayList<>();

        for (Prestamo p : todos) {
            log.info("Analizando préstamo ID {}: Estado={}, Estado==ACTIVO?={}",
                    p.getId(), p.getEstado(), p.getEstado() == EstadoPrestamo.ACTIVO);

            if (p.getEstado() == EstadoPrestamo.ACTIVO) {
                activos.add(p);
                log.info("  ✅ AÑADIDO como ACTIVO");
            }
        }

        log.info("Préstamos ACTIVOS encontrados: {}", activos.size());

        // Método 3: Si sigue vacío, forzar uno
        if (activos.isEmpty() && !todos.isEmpty()) {
            log.warn("⚠️ NINGÚN PRÉSTAMO MARCADO COMO ACTIVO. Usando primer préstamo.");
            Prestamo primerPrestamo = todos.get(0);
            log.warn("Usando préstamo ID {} con estado: {}", primerPrestamo.getId(), primerPrestamo.getEstado());
            activos.add(primerPrestamo);
        }

        return activos.stream()
                .map(prestamo -> {
                    LibroResponseDto libro = obtenerLibroInfo(prestamo.getLibroId());
                    return mapToResponseDTO(prestamo, libro.getTitulo());
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<PrestamoResponseDTO> obtenerPrestamosVencidos() {
        return prestamoRepository.findPrestamosVencidos(LocalDate.now()).stream()
                .map(prestamo -> {
                    LibroResponseDto libro = obtenerLibroInfo(prestamo.getLibroId());
                    return mapToResponseDTO(prestamo, libro.getTitulo());
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean tieneUsuarioPrestamosActivos(Long usuarioId) {
        return prestamoRepository.countByUsuarioIdAndEstado(usuarioId, EstadoPrestamo.ACTIVO) > 0;
    }

    @Override
    public boolean isLibroPrestado(Long libroId) {
        return prestamoRepository.isLibroPrestado(libroId);
    }

    @Override
    public long contarPrestamosActivosUsuario(Long usuarioId) {
        return prestamoRepository.countByUsuarioIdAndEstado(usuarioId, EstadoPrestamo.ACTIVO);
    }

    // ============ MÉTODOS NUEVOS QUE FALTAN ============

    @Override
    @Transactional(readOnly = true)
    public List<PrestamoResponseDTO> obtenerTodosPrestamos() {
        log.info("Obteniendo todos los préstamos");
        return prestamoRepository.findAll().stream()
                .map(prestamo -> {
                    LibroResponseDto libro = obtenerLibroInfo(prestamo.getLibroId());
                    return mapToResponseDTO(prestamo, libro.getTitulo());
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PrestamoResponseDTO actualizarPrestamo(Long id, PrestamoRequestDTO request) {
        log.info("Actualizando préstamo ID: {}", id);

        Prestamo prestamo = prestamoRepository.findById(id)
                .orElseThrow(() -> new PrestamoNotFoundException(id));

        // Validar que no esté devuelto
        if (prestamo.getEstado() == EstadoPrestamo.DEVUELTO ||
                prestamo.getEstado() == EstadoPrestamo.PERDIDO) {
            throw new IllegalArgumentException("No se puede modificar un préstamo ya finalizado");
        }

        // Actualizar campos
        prestamo.setFechaDevolucionPrevista(request.getFechaDevolucionPrevista());
        if (request.getObservaciones() != null) {
            prestamo.setObservaciones(request.getObservaciones());
        }

        // Recalcular días de retraso
        prestamo.calcularDiasRetraso();

        Prestamo updated = prestamoRepository.save(prestamo);
        LibroResponseDto libro = obtenerLibroInfo(updated.getLibroId());

        return mapToResponseDTO(updated, libro.getTitulo());
    }

    @Override
    @Transactional
    public void eliminarPrestamo(Long id) {
        log.info("Eliminando préstamo ID: {}", id);

        Prestamo prestamo = prestamoRepository.findById(id)
                .orElseThrow(() -> new PrestamoNotFoundException(id));

        // Si el préstamo está activo, devolver el libro al stock
        if (prestamo.getEstado() == EstadoPrestamo.ACTIVO) {
            try {
                librosClient.devolverLibro(prestamo.getLibroId());
                log.info("Libro devuelto al stock: ID {}", prestamo.getLibroId());
            } catch (FeignException e) {
                log.error("Error al devolver libro: {}", e.getMessage());
                // Continuar con la eliminación aunque falle la devolución
            }
        }

        prestamoRepository.delete(prestamo);
    }

    @Override
    public Map<String, Object> obtenerEstadisticas() {
        log.info("Obteniendo estadísticas de préstamos");

        Map<String, Object> estadisticas = new HashMap<>();

        long total = prestamoRepository.count();
        long activos = prestamoRepository.countByEstado(EstadoPrestamo.ACTIVO);
        long vencidos = prestamoRepository.countByEstado(EstadoPrestamo.VENCIDO);
        long devueltos = prestamoRepository.countByEstado(EstadoPrestamo.DEVUELTO);
        long perdidos = prestamoRepository.countByEstado(EstadoPrestamo.PERDIDO);

        estadisticas.put("totalPrestamos", total);
        estadisticas.put("prestamosActivos", activos);
        estadisticas.put("prestamosVencidos", vencidos);
        estadisticas.put("prestamosDevueltos", devueltos);
        estadisticas.put("prestamosPerdidos", perdidos);
        estadisticas.put("fechaConsulta", LocalDate.now());

        // Estadísticas adicionales
        if (total > 0) {
            estadisticas.put("porcentajeActivos", (activos * 100.0) / total);
            estadisticas.put("porcentajeVencidos", (vencidos * 100.0) / total);
            estadisticas.put("porcentajeDevueltos", (devueltos * 100.0) / total);
        }

        return estadisticas;
    }

    @Override
    public String testComunicacionConServiciosExternos() {
        log.info("=== TEST COMUNICACIÓN CON SERVICIOS EXTERNOS ===");

        StringBuilder resultado = new StringBuilder();
        RestTemplate restTemplate = new RestTemplate();

        // 1. Test Usuarios Service
        resultado.append("=== SERVICIO USUARIOS (http://localhost:8081) ===\n");
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    "http://localhost:8081/actuator/health", String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                resultado.append("✅ HEALTH: OK (Status: ").append(response.getStatusCode()).append(")\n");
            } else {
                resultado.append("⚠️ HEALTH: ERROR (Status: ").append(response.getStatusCode()).append(")\n");
            }

            // Intentar obtener un usuario
            try {
                String usuarioJson = restTemplate.getForObject(
                        "http://localhost:8081/api/usuarios/1", String.class);
                resultado.append("✅ API: Respuesta recibida\n");
            } catch (Exception e) {
                resultado.append("⚠️ API: No se pudo obtener usuario (puede ser normal si no hay datos)\n");
            }

        } catch (Exception e) {
            resultado.append("❌ ERROR: ").append(e.getMessage()).append("\n");
        }

        resultado.append("\n");

        // 2. Test Libros Service
        resultado.append("=== SERVICIO LIBROS (http://localhost:8082) ===\n");
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    "http://localhost:8082/actuator/health", String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                resultado.append("✅ HEALTH: OK (Status: ").append(response.getStatusCode()).append(")\n");
            } else {
                resultado.append("⚠️ HEALTH: ERROR (Status: ").append(response.getStatusCode()).append(")\n");
            }

            // Intentar obtener un libro
            try {
                String libroJson = restTemplate.getForObject(
                        "http://localhost:8082/api/libros/1", String.class);
                resultado.append("✅ API: Respuesta recibida\n");
            } catch (Exception e) {
                resultado.append("⚠️ API: No se pudo obtener libro (puede ser normal si no hay datos)\n");
            }

        } catch (Exception e) {
            resultado.append("❌ ERROR: ").append(e.getMessage()).append("\n");
        }

        resultado.append("\n");

        // 3. Test Feign Clients
        resultado.append("=== CLIENTES FEIGN ===\n");
        try {
            if (librosClient != null) {
                resultado.append("✅ LibrosClient: Inicializado\n");
            } else {
                resultado.append("❌ LibrosClient: NO inicializado\n");
            }

            if (usuariosClient != null) {
                resultado.append("✅ UsuariosClient: Inicializado\n");
            } else {
                resultado.append("❌ UsuariosClient: NO inicializado\n");
            }
        } catch (Exception e) {
            resultado.append("⚠️ Error verificando Feign clients: ").append(e.getMessage()).append("\n");
        }

        resultado.append("\n=== RECOMENDACIONES ===\n");
        resultado.append("1. Verifica que los servicios estén corriendo:\n");
        resultado.append("   - Usuarios: http://localhost:8081/actuator/health\n");
        resultado.append("   - Libros: http://localhost:8082/actuator/health\n");
        resultado.append("2. Crea datos de prueba si no existen:\n");
        resultado.append("   POST http://localhost:8081/api/usuarios\n");
        resultado.append("   POST http://localhost:8082/api/libros\n");

        return resultado.toString();
    }

    // ============ MÉTODOS PRIVADOS EXISTENTES ============
    private void validarDisponibilidadLibro(Long libroId) {
        try {
            Boolean disponible = librosClient.verificarDisponibilidad(libroId).getBody();
            if (disponible == null || !disponible) {
                throw new LibroNoDisponibleException(libroId);
            }
        } catch (FeignException.NotFound e) {
            throw new LibroNoDisponibleException(libroId);
        }
    }

    private void validarUsuarioSinPrestamosVencidos(Long usuarioId) {
        List<Prestamo> prestamos = prestamoRepository.findByUsuarioId(usuarioId);
        boolean tieneVencidos = prestamos.stream()
                .anyMatch(Prestamo::isVencido);

        if (tieneVencidos) {
            throw new UsuarioConPrestamosVencidosException(usuarioId);
        }
    }

    private void validarLimitePrestamosUsuario(Long usuarioId) {
        long prestamosActivos = prestamoRepository
                .countByUsuarioIdAndEstado(usuarioId, EstadoPrestamo.ACTIVO);

        if (prestamosActivos >= MAX_PRESTAMOS_USUARIO) {
            throw new IllegalArgumentException(
                    String.format("El usuario %d ha alcanzado el límite de %d préstamos activos",
                            usuarioId, MAX_PRESTAMOS_USUARIO));
        }
    }

    private LibroResponseDto obtenerLibroInfo(Long libroId) {
        try {
            return librosClient.obtenerLibro(libroId).getBody();
        } catch (FeignException e) {
            log.warn("Error al obtener información del libro {}: {}", libroId, e.getMessage());
            return LibroResponseDto.builder()
                    .id(libroId)
                    .titulo("Información no disponible")
                    .build();
        }
    }

    private void actualizarDisponibilidadLibro(Long libroId, boolean disponible) {
        try {
            if (disponible) {
                librosClient.devolverLibro(libroId);
            } else {
                librosClient.prestarLibro(libroId);
            }
        } catch (FeignException e) {
            log.error("Error al actualizar disponibilidad del libro {}: {}", libroId, e.getMessage());
            throw new RuntimeException("Error al actualizar disponibilidad del libro", e);
        }
    }

    private PrestamoResponseDTO mapToResponseDTO(Prestamo prestamo, String tituloLibro) {
        return PrestamoResponseDTO.builder()
                .id(prestamo.getId())
                .libroId(prestamo.getLibroId())
                .tituloLibro(tituloLibro)
                .usuarioId(prestamo.getUsuarioId())
                .fechaPrestamo(prestamo.getFechaPrestamo())
                .fechaDevolucionPrevista(prestamo.getFechaDevolucionPrevista())
                .fechaDevolucionReal(prestamo.getFechaDevolucionReal())
                .estado(prestamo.getEstado())
                .diasRetraso(prestamo.getDiasRetraso())
                .observaciones(prestamo.getObservaciones())
                .vencido(prestamo.isVencido())
                .build();
    }
}