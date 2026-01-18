package com.biblioteca.libros.controller;

import com.biblioteca.libros.dto.LibroRequestDTO;
import com.biblioteca.libros.dto.LibroResponseDTO;
import com.biblioteca.libros.service.LibroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/libros")
@RequiredArgsConstructor
@Tag(
        name = "📚 Gestión de Libros",
        description = "API completa para la gestión de libros del sistema de Biblioteca Digital"
)
public class LibroController {

    private final LibroService libroService;

    // ==================== ENDPOINT 1: CREAR LIBRO ====================
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "📝 Crear nuevo libro",
            description = "Registra un nuevo libro en el sistema. El ISBN debe ser único.",
            operationId = "crearLibro"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "✅ Libro creado exitosamente",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LibroResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Libro creado",
                                    value = """
                        {
                          "id": 1,
                          "titulo": "Cien años de soledad",
                          "autor": "Gabriel García Márquez",
                          "isbn": "978-0307474728",
                          "ejemplaresDisponibles": 5,
                          "disponible": true
                        }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "❌ Datos de entrada inválidos",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "⚠️ ISBN ya registrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
            )
    })
    public ResponseEntity<LibroResponseDTO> crearLibro(
            @Valid @RequestBody LibroRequestDTO libroRequestDTO) {

        LibroResponseDTO libroCreado = libroService.crearLibro(libroRequestDTO);
        return new ResponseEntity<>(libroCreado, HttpStatus.CREATED);
    }

    // ==================== ENDPOINT 2: LISTAR TODOS LOS LIBROS ====================
    @GetMapping
    @Operation(
            summary = "📋 Listar todos los libros",
            description = "Obtiene la lista completa de libros registrados en el sistema.",
            operationId = "obtenerTodosLibros"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Lista de libros obtenida",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = LibroResponseDTO.class)),
                            examples = @ExampleObject(
                                    name = "Lista de libros",
                                    value = """
                        [
                          {
                            "id": 1,
                            "titulo": "Cien años de soledad",
                            "autor": "Gabriel García Márquez",
                            "isbn": "978-0307474728",
                            "ejemplaresDisponibles": 5,
                            "disponible": true
                          },
                          {
                            "id": 2,
                            "titulo": "1984",
                            "autor": "George Orwell",
                            "isbn": "978-0451524935",
                            "ejemplaresDisponibles": 0,
                            "disponible": false
                          }
                        ]
                    """
                            )
                    )
            )
    })
    public ResponseEntity<List<LibroResponseDTO>> obtenerTodosLibros() {
        List<LibroResponseDTO> libros = libroService.obtenerTodosLibros();
        return ResponseEntity.ok(libros);
    }

    // ==================== ENDPOINT 3: OBTENER LIBRO POR ID ====================
    @GetMapping("/{id}")
    @Operation(
            summary = "🔍 Obtener libro por ID",
            description = "Busca y retorna un libro específico por su ID.",
            operationId = "obtenerLibroPorId"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Libro encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LibroResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "❌ Libro no encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
            )
    })
    public ResponseEntity<LibroResponseDTO> obtenerLibroPorId(
            @Parameter(
                    description = "ID del libro a buscar",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id) {

        LibroResponseDTO libro = libroService.obtenerLibroPorId(id);
        return ResponseEntity.ok(libro);
    }

    // ==================== ENDPOINT 4: ACTUALIZAR LIBRO ====================
    @PutMapping("/{id}")
    @Operation(
            summary = "✏️ Actualizar libro",
            description = "Actualiza la información de un libro existente.",
            operationId = "actualizarLibro"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Libro actualizado exitosamente",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LibroResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "❌ Datos de entrada inválidos",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "❌ Libro no encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "⚠️ ISBN ya registrado (si se cambia el ISBN)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
            )
    })
    public ResponseEntity<LibroResponseDTO> actualizarLibro(
            @Parameter(
                    description = "ID del libro a actualizar",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id,

            @Valid @RequestBody LibroRequestDTO libroRequestDTO) {

        LibroResponseDTO libroActualizado = libroService.actualizarLibro(id, libroRequestDTO);
        return ResponseEntity.ok(libroActualizado);
    }

    // ==================== ENDPOINT 5: ELIMINAR LIBRO ====================
    @DeleteMapping("/{id}")
    @Operation(
            summary = "🗑️ Eliminar libro",
            description = "Elimina permanentemente un libro del sistema.",
            operationId = "eliminarLibro"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "✅ Libro eliminado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "❌ Libro no encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
            )
    })
    public ResponseEntity<Void> eliminarLibro(
            @Parameter(
                    description = "ID del libro a eliminar",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id) {

        libroService.eliminarLibro(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== ENDPOINT 6: VALIDAR EXISTENCIA ====================
    @GetMapping("/{id}/existe")
    @Operation(
            summary = "✅ Verificar existencia de libro",
            description = "Verifica si un libro existe en el sistema.",
            operationId = "existeLibro"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Respuesta booleana",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = "true")
                    )
            )
    })
    public ResponseEntity<Boolean> existeLibro(
            @Parameter(
                    description = "ID del libro a verificar",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id) {

        boolean existe = libroService.existeLibro(id);
        return ResponseEntity.ok(existe);
    }

    // ==================== ENDPOINT 7: VALIDAR DISPONIBILIDAD ====================
    @GetMapping("/{id}/disponible")
    @Operation(
            summary = "✅ Verificar disponibilidad de libro",
            description = "Verifica si un libro está disponible para préstamo.",
            operationId = "libroDisponible"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Respuesta booleana",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = "true")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "❌ Libro no encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
            )
    })
    public ResponseEntity<Boolean> libroDisponible(
            @Parameter(
                    description = "ID del libro a verificar",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id) {

        boolean disponible = libroService.libroDisponible(id);
        return ResponseEntity.ok(disponible);
    }

    // ==================== ENDPOINT 8: PRESTAR LIBRO ====================
    @PostMapping("/{id}/prestar")
    @Operation(
            summary = "📖 Prestar libro",
            description = "Presta un ejemplar del libro (reduce stock en 1).",
            operationId = "prestarLibro"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Libro prestado exitosamente",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LibroResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "❌ Libro no disponible",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "❌ Libro no encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
            )
    })
    public ResponseEntity<LibroResponseDTO> prestarLibro(
            @Parameter(
                    description = "ID del libro a prestar",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id) {

        boolean prestado = libroService.prestarLibro(id);
        if (prestado) {
            LibroResponseDTO libro = libroService.obtenerLibroPorId(id);
            return ResponseEntity.ok(libro);
        }
        return ResponseEntity.badRequest().build();
    }

    // ==================== ENDPOINT 9: DEVOLVER LIBRO ====================
    @PostMapping("/{id}/devolver")
    @Operation(
            summary = "📚 Devolver libro",
            description = "Devuelve un ejemplar del libro (aumenta stock en 1).",
            operationId = "devolverLibro"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Libro devuelto exitosamente",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LibroResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "❌ Libro no encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
            )
    })
    public ResponseEntity<LibroResponseDTO> devolverLibro(
            @Parameter(
                    description = "ID del libro a devolver",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id) {

        libroService.devolverLibro(id);
        LibroResponseDTO libro = libroService.obtenerLibroPorId(id);
        return ResponseEntity.ok(libro);
    }

    // ==================== ENDPOINT 10: ACTUALIZAR STOCK ====================
    @PatchMapping("/{id}/stock")
    @Operation(
            summary = "📊 Actualizar stock",
            description = "Actualiza la cantidad de ejemplares disponibles de un libro.",
            operationId = "actualizarStock"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Stock actualizado exitosamente",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LibroResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "❌ Cantidad inválida (negativa)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "❌ Libro no encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
            )
    })
    public ResponseEntity<LibroResponseDTO> actualizarStock(
            @Parameter(
                    description = "ID del libro",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id,

            @Parameter(
                    description = "Nueva cantidad de ejemplares",
                    required = true,
                    example = "10"
            )
            @RequestParam int cantidad) {

        LibroResponseDTO libroActualizado = libroService.actualizarStock(id, cantidad);
        return ResponseEntity.ok(libroActualizado);
    }

    // Metodo raiz
//    @GetMapping("/")
//    public ResponseEntity<String> home() {
//        return ResponseEntity.ok("Servicio de Libros funcionando correctamente. Accede a /api/libros para gestionar libros.");
//    }
}
