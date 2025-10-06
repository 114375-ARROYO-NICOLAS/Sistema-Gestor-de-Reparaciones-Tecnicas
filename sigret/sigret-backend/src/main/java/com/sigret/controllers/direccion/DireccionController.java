package com.sigret.controllers.direccion;

import com.sigret.dtos.direccion.DireccionCreateDto;
import com.sigret.dtos.direccion.DireccionListDto;
import com.sigret.dtos.direccion.DireccionResponseDto;
import com.sigret.dtos.direccion.DireccionUpdateDto;
import com.sigret.services.DireccionService;
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
@RequestMapping("/api/direcciones")
@Tag(name = "Gestión de Direcciones", description = "Endpoints para la gestión de direcciones de personas")
@SecurityRequirement(name = "bearerAuth")
public class DireccionController {

    @Autowired
    private DireccionService direccionService;

    @PostMapping
    @Operation(summary = "Crear dirección", description = "Crea una nueva dirección para una persona")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Dirección creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Persona no encontrada")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<DireccionResponseDto> crearDireccion(@Valid @RequestBody DireccionCreateDto direccionCreateDto) {
        DireccionResponseDto direccionCreada = direccionService.crearDireccion(direccionCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(direccionCreada);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener dirección por ID", description = "Obtiene los detalles de una dirección específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dirección encontrada"),
            @ApiResponse(responseCode = "404", description = "Dirección no encontrada")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<DireccionResponseDto> obtenerDireccionPorId(
            @Parameter(description = "ID de la dirección") @PathVariable Long id) {
        DireccionResponseDto direccion = direccionService.obtenerDireccionPorId(id);
        return ResponseEntity.ok(direccion);
    }

    @GetMapping("/persona/{personaId}")
    @Operation(summary = "Obtener direcciones por persona", description = "Obtiene todas las direcciones de una persona")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Direcciones obtenidas exitosamente"),
            @ApiResponse(responseCode = "404", description = "Persona no encontrada")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<DireccionListDto>> obtenerDireccionesPorPersona(
            @Parameter(description = "ID de la persona") @PathVariable Long personaId) {
        List<DireccionListDto> direcciones = direccionService.obtenerDireccionesPorPersona(personaId);
        return ResponseEntity.ok(direcciones);
    }

    @GetMapping("/persona/{personaId}/principal")
    @Operation(summary = "Obtener dirección principal", description = "Obtiene la dirección principal de una persona")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dirección principal encontrada"),
            @ApiResponse(responseCode = "404", description = "Dirección principal no encontrada")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<DireccionResponseDto> obtenerDireccionPrincipal(
            @Parameter(description = "ID de la persona") @PathVariable Long personaId) {
        DireccionResponseDto direccion = direccionService.obtenerDireccionPrincipalPorPersona(personaId);
        return ResponseEntity.ok(direccion);
    }

    @GetMapping
    @Operation(summary = "Listar direcciones", description = "Obtiene una lista paginada de direcciones")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de direcciones obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<Page<DireccionListDto>> obtenerDirecciones(Pageable pageable) {
        Page<DireccionListDto> direcciones = direccionService.obtenerDirecciones(pageable);
        return ResponseEntity.ok(direcciones);
    }

    @GetMapping("/buscar/ciudad")
    @Operation(summary = "Buscar por ciudad", description = "Busca direcciones por ciudad")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Búsqueda realizada exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<List<DireccionListDto>> buscarPorCiudad(
            @Parameter(description = "Nombre de la ciudad") @RequestParam String ciudad) {
        List<DireccionListDto> direcciones = direccionService.buscarPorCiudad(ciudad);
        return ResponseEntity.ok(direcciones);
    }

    @GetMapping("/buscar/provincia")
    @Operation(summary = "Buscar por provincia", description = "Busca direcciones por provincia")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Búsqueda realizada exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<List<DireccionListDto>> buscarPorProvincia(
            @Parameter(description = "Nombre de la provincia") @RequestParam String provincia) {
        List<DireccionListDto> direcciones = direccionService.buscarPorProvincia(provincia);
        return ResponseEntity.ok(direcciones);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar dirección", description = "Actualiza los datos de una dirección existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dirección actualizada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Dirección no encontrada"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<DireccionResponseDto> actualizarDireccion(
            @Parameter(description = "ID de la dirección") @PathVariable Long id,
            @Valid @RequestBody DireccionUpdateDto direccionUpdateDto) {
        DireccionResponseDto direccionActualizada = direccionService.actualizarDireccion(id, direccionUpdateDto);
        return ResponseEntity.ok(direccionActualizada);
    }

    @PatchMapping("/{id}/marcar-principal")
    @Operation(summary = "Marcar como principal", description = "Marca una dirección como principal")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dirección marcada como principal exitosamente"),
            @ApiResponse(responseCode = "404", description = "Dirección no encontrada")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<DireccionResponseDto> marcarComoPrincipal(
            @Parameter(description = "ID de la dirección") @PathVariable Long id) {
        DireccionResponseDto direccion = direccionService.marcarComoPrincipal(id);
        return ResponseEntity.ok(direccion);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar dirección", description = "Elimina una dirección del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dirección eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Dirección no encontrada")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<Void> eliminarDireccion(
            @Parameter(description = "ID de la dirección") @PathVariable Long id) {
        direccionService.eliminarDireccion(id);
        return ResponseEntity.ok().build();
    }
}

