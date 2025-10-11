package com.sigret.controllers.tipoEmpleado;

import com.sigret.dtos.tipoEmpleado.TipoEmpleadoCreateDto;
import com.sigret.dtos.tipoEmpleado.TipoEmpleadoListDto;
import com.sigret.dtos.tipoEmpleado.TipoEmpleadoResponseDto;
import com.sigret.dtos.tipoEmpleado.TipoEmpleadoUpdateDto;
import com.sigret.services.TipoEmpleadoService;
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
@RequestMapping("/api/tipos-empleado")
@Tag(name = "Tipos de Empleado", description = "Endpoints para la gestión de tipos de empleado")
@SecurityRequirement(name = "bearerAuth")
public class TipoEmpleadoController {

    @Autowired
    private TipoEmpleadoService tipoEmpleadoService;

    @PostMapping
    @Operation(summary = "Crear tipo de empleado", description = "Crea un nuevo tipo de empleado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tipo de empleado creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Ya existe un tipo de empleado con esa descripción")
    })
    @PreAuthorize("hasRole('PROPIETARIO')")
    public ResponseEntity<TipoEmpleadoResponseDto> crearTipoEmpleado(@Valid @RequestBody TipoEmpleadoCreateDto tipoEmpleadoCreateDto) {
        TipoEmpleadoResponseDto tipoEmpleadoCreado = tipoEmpleadoService.crearTipoEmpleado(tipoEmpleadoCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(tipoEmpleadoCreado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de empleado por ID", description = "Obtiene los detalles de un tipo de empleado específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de empleado encontrado"),
            @ApiResponse(responseCode = "404", description = "Tipo de empleado no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<TipoEmpleadoResponseDto> obtenerTipoEmpleadoPorId(
            @Parameter(description = "ID del tipo de empleado") @PathVariable Long id) {
        TipoEmpleadoResponseDto tipoEmpleado = tipoEmpleadoService.obtenerTipoEmpleadoPorId(id);
        return ResponseEntity.ok(tipoEmpleado);
    }

    @GetMapping
    @Operation(summary = "Listar tipos de empleado", description = "Obtiene una lista de todos los tipos de empleado (endpoint público)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tipos de empleado obtenida exitosamente")
    })
    // Endpoint público - Sin @PreAuthorize para permitir acceso sin autenticación
    public ResponseEntity<List<TipoEmpleadoListDto>> obtenerTodosTiposEmpleado() {
        List<TipoEmpleadoListDto> tiposEmpleado = tipoEmpleadoService.obtenerTodosTiposEmpleado();
        return ResponseEntity.ok(tiposEmpleado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de empleado", description = "Actualiza los datos de un tipo de empleado existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de empleado actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Tipo de empleado no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Ya existe un tipo de empleado con esa descripción")
    })
    @PreAuthorize("hasRole('PROPIETARIO')")
    public ResponseEntity<TipoEmpleadoResponseDto> actualizarTipoEmpleado(
            @Parameter(description = "ID del tipo de empleado") @PathVariable Long id,
            @Valid @RequestBody TipoEmpleadoUpdateDto tipoEmpleadoUpdateDto) {
        TipoEmpleadoResponseDto tipoEmpleadoActualizado = tipoEmpleadoService.actualizarTipoEmpleado(id, tipoEmpleadoUpdateDto);
        return ResponseEntity.ok(tipoEmpleadoActualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo de empleado", description = "Elimina un tipo de empleado del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de empleado eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Tipo de empleado no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO')")
    public ResponseEntity<Void> eliminarTipoEmpleado(
            @Parameter(description = "ID del tipo de empleado") @PathVariable Long id) {
        tipoEmpleadoService.eliminarTipoEmpleado(id);
        return ResponseEntity.ok().build();
    }
}

