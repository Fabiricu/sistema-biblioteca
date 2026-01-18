package com.biblioteca.prestamos.controller;

import com.biblioteca.prestamos.controller.dto.PrestamoRequestDTO;
import com.biblioteca.prestamos.controller.dto.PrestamoResponseDTO;
import com.biblioteca.prestamos.controller.dto.DevolucionRequestDTO;
import com.biblioteca.prestamos.model.entity.Prestamo;
import com.biblioteca.prestamos.service.PrestamoService;
import com.biblioteca.prestamos.repository.PrestamoRepository;
import com.biblioteca.prestamos.model.enums.EstadoPrestamo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;  // ← AGREGAR ESTE IMPORT

@Slf4j
@RestController
@RequestMapping("/api/prestamos")
@RequiredArgsConstructor
@Tag(name = "Préstamos", description = "API para gestión de préstamos de libros. Se comunica con los servicios de Libros y Usuarios.")
public class PrestamoController {

    private final PrestamoService prestamoService;



    // ==================== ENDPOINT 1: CREAR PRÉSTAMO ====================
    @Operation(summary = "Crear un nuevo préstamo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Préstamo creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado"),
            @ApiResponse(responseCode = "409", description = "Libro no disponible o usuario con préstamos vencidos")
    })
    @PostMapping
    public ResponseEntity<PrestamoResponseDTO> crearPrestamo(
            @Valid @RequestBody PrestamoRequestDTO request) {
        log.info("📝 Creando préstamo - Usuario: {}, Libro: {}",
                request.getUsuarioId(), request.getLibroId());
        PrestamoResponseDTO response = prestamoService.crearPrestamo(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ==================== ENDPOINT 2: LISTAR TODOS LOS PRÉSTAMOS ====================
    @Operation(summary = "Obtener todos los préstamos")
    @ApiResponse(responseCode = "200", description = "Lista de todos los préstamos")
    @GetMapping
    public ResponseEntity<List<PrestamoResponseDTO>> obtenerTodosPrestamos() {
        log.info("📋 Obteniendo todos los préstamos");
        List<PrestamoResponseDTO> response = prestamoService.obtenerTodosPrestamos();
        return ResponseEntity.ok(response);
    }

    // ==================== ENDPOINT 3: OBTENER PRÉSTAMO POR ID ====================
    @Operation(summary = "Obtener un préstamo por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Préstamo encontrado"),
            @ApiResponse(responseCode = "404", description = "Préstamo no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PrestamoResponseDTO> obtenerPrestamo(
            @Parameter(description = "ID del préstamo") @PathVariable Long id) {
        log.info("🔍 Obteniendo préstamo ID: {}", id);
        PrestamoResponseDTO response = prestamoService.obtenerPrestamo(id);
        return ResponseEntity.ok(response);
    }



    // ==================== ENDPOINT 4: ACTUALIZAR PRÉSTAMO ====================
    @Operation(summary = "Actualizar un préstamo existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Préstamo actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Préstamo no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PrestamoResponseDTO> actualizarPrestamo(
            @Parameter(description = "ID del préstamo") @PathVariable Long id,
            @Valid @RequestBody PrestamoRequestDTO request) {
        log.info("✏️ Actualizando préstamo ID: {}", id);
        PrestamoResponseDTO response = prestamoService.actualizarPrestamo(id, request);
        return ResponseEntity.ok(response);
    }

    // ==================== ENDPOINT 5: ELIMINAR PRÉSTAMO ====================
    @Operation(summary = "Eliminar un préstamo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Préstamo eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Préstamo no encontrado"),
            @ApiResponse(responseCode = "400", description = "No se puede eliminar un préstamo activo")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPrestamo(
            @Parameter(description = "ID del préstamo") @PathVariable Long id) {
        log.info("🗑️ Eliminando préstamo ID: {}", id);
        prestamoService.eliminarPrestamo(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== ENDPOINT 6: REGISTRAR DEVOLUCIÓN ====================
    @Operation(summary = "Registrar devolución de un préstamo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Devolución registrada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Préstamo no encontrado"),
            @ApiResponse(responseCode = "400", description = "El préstamo no está activo")
    })
    @PostMapping("/{id}/devolucion")
    public ResponseEntity<PrestamoResponseDTO> registrarDevolucion(
            @Parameter(description = "ID del préstamo") @PathVariable Long id,
            @Valid @RequestBody DevolucionRequestDTO request) {
        log.info("📚 Registrando devolución para préstamo ID: {}", id);
        PrestamoResponseDTO response = prestamoService.registrarDevolucion(id, request);
        return ResponseEntity.ok(response);
    }

    // ==================== ENDPOINT 7: OBTENER PRÉSTAMOS POR USUARIO ====================
    @Operation(summary = "Obtener préstamos por usuario")
    @ApiResponse(responseCode = "200", description = "Lista de préstamos del usuario")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<PrestamoResponseDTO>> obtenerPrestamosPorUsuario(
            @Parameter(description = "ID del usuario") @PathVariable Long usuarioId) {
        log.info("👤 Obteniendo préstamos para usuario ID: {}", usuarioId);
        List<PrestamoResponseDTO> response = prestamoService.obtenerPrestamosPorUsuario(usuarioId);
        return ResponseEntity.ok(response);
    }

    // ==================== ENDPOINT 8: OBTENER PRÉSTAMOS POR LIBRO ====================
    @Operation(summary = "Obtener préstamos por libro")
    @ApiResponse(responseCode = "200", description = "Lista de préstamos del libro")
    @GetMapping("/libro/{libroId}")
    public ResponseEntity<List<PrestamoResponseDTO>> obtenerPrestamosPorLibro(
            @Parameter(description = "ID del libro") @PathVariable Long libroId) {
        log.info("📚 Obteniendo préstamos para libro ID: {}", libroId);
        List<PrestamoResponseDTO> response = prestamoService.obtenerPrestamosPorLibro(libroId);
        return ResponseEntity.ok(response);
    }

    // ==================== ENDPOINT 9: OBTENER PRÉSTAMOS ACTIVOS ====================
    @Operation(summary = "Obtener préstamos activos")
    @ApiResponse(responseCode = "200", description = "Lista de préstamos activos")
    @GetMapping("/activos")
    public ResponseEntity<List<PrestamoResponseDTO>> obtenerPrestamosActivos() {
        log.info("✅ Obteniendo préstamos activos");
        List<PrestamoResponseDTO> response = prestamoService.obtenerPrestamosActivos();
        return ResponseEntity.ok(response);
    }

    // ==================== ENDPOINT 10: OBTENER PRÉSTAMOS VENCIDOS ====================
    @Operation(summary = "Obtener préstamos vencidos")
    @ApiResponse(responseCode = "200", description = "Lista de préstamos vencidos")
    @GetMapping("/vencidos")
    public ResponseEntity<List<PrestamoResponseDTO>> obtenerPrestamosVencidos() {
        log.info("⏰ Obteniendo préstamos vencidos");
        List<PrestamoResponseDTO> response = prestamoService.obtenerPrestamosVencidos();
        return ResponseEntity.ok(response);
    }

    // ==================== ENDPOINT 11: VERIFICAR USUARIO CON PRÉSTAMOS ACTIVOS ====================
    @Operation(summary = "Verificar si un usuario tiene préstamos activos")
    @ApiResponse(responseCode = "200", description = "Resultado de la verificación")
    @GetMapping("/usuario/{usuarioId}/activos")
    public ResponseEntity<Boolean> tieneUsuarioPrestamosActivos(
            @Parameter(description = "ID del usuario") @PathVariable Long usuarioId) {
        log.info("🔍 Verificando si usuario ID: {} tiene préstamos activos", usuarioId);
        boolean tieneActivos = prestamoService.tieneUsuarioPrestamosActivos(usuarioId);
        return ResponseEntity.ok(tieneActivos);
    }

    // ==================== ENDPOINT 12: VERIFICAR LIBRO PRESTADO ====================
    @Operation(summary = "Verificar si un libro está prestado")
    @ApiResponse(responseCode = "200", description = "Resultado de la verificación")
    @GetMapping("/libro/{libroId}/prestado")
    public ResponseEntity<Boolean> isLibroPrestado(
            @Parameter(description = "ID del libro") @PathVariable Long libroId) {
        log.info("🔍 Verificando si libro ID: {} está prestado", libroId);
        boolean prestado = prestamoService.isLibroPrestado(libroId);
        return ResponseEntity.ok(prestado);
    }

    // ==================== ENDPOINT 13: CONTAR PRÉSTAMOS ACTIVOS DE USUARIO ====================
    @Operation(summary = "Contar préstamos activos de un usuario")
    @ApiResponse(responseCode = "200", description = "Número de préstamos activos")
    @GetMapping("/usuario/{usuarioId}/contar-activos")
    public ResponseEntity<Long> contarPrestamosActivosUsuario(
            @Parameter(description = "ID del usuario") @PathVariable Long usuarioId) {
        log.info("🔢 Contando préstamos activos para usuario ID: {}", usuarioId);
        long count = prestamoService.contarPrestamosActivosUsuario(usuarioId);
        return ResponseEntity.ok(count);
    }

    // ==================== ENDPOINT 14: ESTADÍSTICAS ====================
    @Operation(summary = "Obtener estadísticas de préstamos")
    @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas")
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        log.info("📊 Obteniendo estadísticas de préstamos");
        Map<String, Object> estadisticas = prestamoService.obtenerEstadisticas();
        return ResponseEntity.ok(estadisticas);
    }

    // ==================== ENDPOINT 15: TEST COMUNICACIÓN (OCULTO) ====================
    @Operation(summary = "Probar comunicación con servicios externos",
            description = "Verifica que el servicio pueda comunicarse con Libros y Usuarios",
            hidden = true)  // Oculta en Swagger UI
    @GetMapping("/test-comunicacion")
    public ResponseEntity<String> testComunicacion() {
        log.info("🔧 Probando comunicación con servicios externos...");
        try {
            String resultado = prestamoService.testComunicacionConServiciosExternos();
            return ResponseEntity.ok("✅ Comunicación exitosa:\n" + resultado);
        } catch (Exception e) {
            log.error("❌ Error en comunicación: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("❌ Error en comunicación:\n" + e.getMessage());
        }
    }
}