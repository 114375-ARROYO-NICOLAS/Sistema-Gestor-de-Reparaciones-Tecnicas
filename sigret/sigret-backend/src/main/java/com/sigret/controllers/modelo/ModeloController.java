package com.sigret.controllers.modelo;

import com.sigret.dtos.modelo.ModeloCreateDto;
import com.sigret.dtos.modelo.ModeloListDto;
import com.sigret.dtos.modelo.ModeloResponseDto;
import com.sigret.dtos.modelo.ModeloUpdateDto;
import com.sigret.services.ModeloService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modelos")
@Tag(name = "Gestión de Modelos", description = "Endpoints para la gestión de modelos del sistema")
@SecurityRequirement(name = "bearerAuth")
public class ModeloController {

    @Autowired
    private ModeloService modeloService;

    @PostMapping
    @Operation(summary = "Crear modelo", description = "Crea un nuevo modelo en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Modelo creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Modelo ya existe para esta marca")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<ModeloResponseDto> crearModelo(@Valid @RequestBody ModeloCreateDto modeloCreateDto) {
        ModeloResponseDto modeloCreado = modeloService.crearModelo(modeloCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(modeloCreado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener modelo por ID", description = "Obtiene los detalles de un modelo específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Modelo encontrado"),
            @ApiResponse(responseCode = "404", description = "Modelo no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<ModeloResponseDto> obtenerModeloPorId(
            @Parameter(description = "ID del modelo") @PathVariable Long id) {
        ModeloResponseDto modelo = modeloService.obtenerModeloPorId(id);
        return ResponseEntity.ok(modelo);
    }

    @GetMapping
    @Operation(summary = "Listar modelos", description = "Obtiene una lista paginada de modelos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de modelos obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<Page<ModeloListDto>> obtenerModelos(Pageable pageable) {
        Page<ModeloListDto> modelos = modeloService.obtenerModelos(pageable);
        return ResponseEntity.ok(modelos);
    }

    @GetMapping("/todos")
    @Operation(summary = "Obtener todos los modelos", description = "Obtiene una lista completa de modelos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de modelos obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<ModeloListDto>> obtenerTodosLosModelos() {
        List<ModeloListDto> modelos = modeloService.obtenerTodosLosModelos();
        return ResponseEntity.ok(modelos);
    }

    @GetMapping("/marca/{marcaId}")
    @Operation(summary = "Obtener modelos por marca", description = "Obtiene modelos filtrados por marca")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de modelos obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<ModeloListDto>> obtenerModelosPorMarca(
            @Parameter(description = "ID de la marca") @PathVariable Long marcaId) {
        List<ModeloListDto> modelos = modeloService.obtenerModelosPorMarca(marcaId);
        return ResponseEntity.ok(modelos);
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar modelos", description = "Busca modelos por descripción")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Búsqueda realizada exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<ModeloListDto>> buscarModelos(
            @Parameter(description = "Término de búsqueda") @RequestParam String termino) {
        List<ModeloListDto> modelos = modeloService.buscarModelos(termino);
        return ResponseEntity.ok(modelos);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar modelo", description = "Actualiza los datos de un modelo existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Modelo actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Modelo no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Modelo ya existe para esta marca")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<ModeloResponseDto> actualizarModelo(
            @Parameter(description = "ID del modelo") @PathVariable Long id,
            @Valid @RequestBody ModeloUpdateDto modeloUpdateDto) {
        ModeloResponseDto modeloActualizado = modeloService.actualizarModelo(id, modeloUpdateDto);
        return ResponseEntity.ok(modeloActualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar modelo", description = "Elimina un modelo del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Modelo eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Modelo no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO')")
    public ResponseEntity<Void> eliminarModelo(
            @Parameter(description = "ID del modelo") @PathVariable Long id) {
        modeloService.eliminarModelo(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/verificar-descripcion")
    @Operation(summary = "Verificar descripción", description = "Verifica si una descripción de modelo ya existe para una marca")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verificación completada")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<Boolean> verificarDescripcion(
            @Parameter(description = "Descripción a verificar") @RequestParam String descripcion,
            @Parameter(description = "ID de la marca") @RequestParam Long marcaId) {
        boolean existe = modeloService.existeModeloConDescripcionYMarca(descripcion, marcaId);
        return ResponseEntity.ok(existe);
    }
}
