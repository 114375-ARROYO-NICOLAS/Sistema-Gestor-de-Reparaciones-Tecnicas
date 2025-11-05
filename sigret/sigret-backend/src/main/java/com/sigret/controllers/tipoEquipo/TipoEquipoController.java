package com.sigret.controllers.tipoEquipo;

import com.sigret.dtos.tipoEquipo.TipoEquipoCreateDto;
import com.sigret.dtos.tipoEquipo.TipoEquipoListDto;
import com.sigret.dtos.tipoEquipo.TipoEquipoResponseDto;
import com.sigret.dtos.tipoEquipo.TipoEquipoUpdateDto;
import com.sigret.services.TipoEquipoService;
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
@RequestMapping("/api/tipos-equipo")
@Tag(name = "Gestión de Tipos de Equipo", description = "Endpoints para la gestión de tipos de equipo del sistema")
@SecurityRequirement(name = "bearerAuth")
public class TipoEquipoController {

    @Autowired
    private TipoEquipoService tipoEquipoService;

    @PostMapping
    @Operation(summary = "Crear tipo de equipo", description = "Crea un nuevo tipo de equipo en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tipo de equipo creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<TipoEquipoResponseDto> crearTipoEquipo(@Valid @RequestBody TipoEquipoCreateDto tipoEquipoCreateDto) {
        TipoEquipoResponseDto tipoEquipoCreado = tipoEquipoService.crearTipoEquipo(tipoEquipoCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(tipoEquipoCreado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de equipo por ID", description = "Obtiene los detalles de un tipo de equipo específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de equipo encontrado"),
            @ApiResponse(responseCode = "404", description = "Tipo de equipo no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<TipoEquipoResponseDto> obtenerTipoEquipoPorId(
            @Parameter(description = "ID del tipo de equipo") @PathVariable Long id) {
        TipoEquipoResponseDto tipoEquipo = tipoEquipoService.obtenerTipoEquipoPorId(id);
        return ResponseEntity.ok(tipoEquipo);
    }

    @GetMapping
    @Operation(summary = "Obtener todos los tipos de equipo", description = "Obtiene una lista completa de tipos de equipo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tipos de equipo obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<TipoEquipoListDto>> obtenerTodosLosTiposEquipo() {
        List<TipoEquipoListDto> tiposEquipo = tipoEquipoService.obtenerTodosLosTiposEquipo();
        return ResponseEntity.ok(tiposEquipo);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de equipo", description = "Actualiza los datos de un tipo de equipo existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de equipo actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Tipo de equipo no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<TipoEquipoResponseDto> actualizarTipoEquipo(
            @Parameter(description = "ID del tipo de equipo") @PathVariable Long id,
            @Valid @RequestBody TipoEquipoUpdateDto tipoEquipoUpdateDto) {
        TipoEquipoResponseDto tipoEquipoActualizado = tipoEquipoService.actualizarTipoEquipo(id, tipoEquipoUpdateDto);
        return ResponseEntity.ok(tipoEquipoActualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo de equipo", description = "Elimina un tipo de equipo del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de equipo eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Tipo de equipo no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO')")
    public ResponseEntity<Void> eliminarTipoEquipo(
            @Parameter(description = "ID del tipo de equipo") @PathVariable Long id) {
        tipoEquipoService.eliminarTipoEquipo(id);
        return ResponseEntity.ok().build();
    }
}
