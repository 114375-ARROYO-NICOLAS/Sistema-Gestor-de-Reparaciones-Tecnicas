package com.sigret.controllers.tipoPersona;

import com.sigret.dtos.tipoPersona.TipoPersonaCreateDto;
import com.sigret.dtos.tipoPersona.TipoPersonaListDto;
import com.sigret.dtos.tipoPersona.TipoPersonaResponseDto;
import com.sigret.dtos.tipoPersona.TipoPersonaUpdateDto;
import com.sigret.services.TipoPersonaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-persona")
@Tag(name = "Tipos de Persona", description = "Endpoints para la gestión de tipos de persona")
@SecurityRequirement(name = "bearerAuth")
public class TipoPersonaController {

    @Autowired
    private TipoPersonaService tipoPersonaService;

    @PostMapping
    @Operation(summary = "Crear tipo de persona", description = "Crea un nuevo tipo de persona")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tipo de persona creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Ya existe un tipo de persona con esa descripción")
    })
    @PreAuthorize("hasRole('PROPIETARIO')")
    public ResponseEntity<TipoPersonaResponseDto> crearTipoPersona(@Valid @RequestBody TipoPersonaCreateDto tipoPersonaCreateDto) {
        TipoPersonaResponseDto tipoPersonaCreado = tipoPersonaService.crearTipoPersona(tipoPersonaCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(tipoPersonaCreado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de persona por ID", description = "Obtiene los detalles de un tipo de persona específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de persona encontrado"),
            @ApiResponse(responseCode = "404", description = "Tipo de persona no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<TipoPersonaResponseDto> obtenerTipoPersonaPorId(
            @Parameter(description = "ID del tipo de persona") @PathVariable Long id) {
        TipoPersonaResponseDto tipoPersona = tipoPersonaService.obtenerTipoPersonaPorId(id);
        return ResponseEntity.ok(tipoPersona);
    }

    @GetMapping
    @Operation(summary = "Listar tipos de persona", description = "Obtiene una lista de todos los tipos de persona (endpoint público)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tipos de persona obtenida exitosamente")
    })
    // Endpoint público - Sin @PreAuthorize para permitir acceso sin autenticación
    public ResponseEntity<List<TipoPersonaListDto>> obtenerTodosTiposPersona() {
        List<TipoPersonaListDto> tiposPersona = tipoPersonaService.obtenerTodosTiposPersona();
        return ResponseEntity.ok(tiposPersona);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de persona", description = "Actualiza los datos de un tipo de persona existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de persona actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Tipo de persona no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Ya existe un tipo de persona con esa descripción")
    })
    @PreAuthorize("hasRole('PROPIETARIO')")
    public ResponseEntity<TipoPersonaResponseDto> actualizarTipoPersona(
            @Parameter(description = "ID del tipo de persona") @PathVariable Long id,
            @Valid @RequestBody TipoPersonaUpdateDto tipoPersonaUpdateDto) {
        TipoPersonaResponseDto tipoPersonaActualizado = tipoPersonaService.actualizarTipoPersona(id, tipoPersonaUpdateDto);
        return ResponseEntity.ok(tipoPersonaActualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo de persona", description = "Elimina un tipo de persona del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de persona eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Tipo de persona no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO')")
    public ResponseEntity<Void> eliminarTipoPersona(
            @Parameter(description = "ID del tipo de persona") @PathVariable Long id) {
        tipoPersonaService.eliminarTipoPersona(id);
        return ResponseEntity.ok().build();
    }
}

