package com.sigret.controllers.presupuesto;

import com.sigret.dtos.presupuesto.PresupuestoCreateDto;
import com.sigret.dtos.presupuesto.PresupuestoListDto;
import com.sigret.dtos.presupuesto.PresupuestoResponseDto;
import com.sigret.dtos.presupuesto.PresupuestoUpdateDto;
import com.sigret.enums.EstadoPresupuesto;
import com.sigret.services.PresupuestoService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/presupuestos")
@Tag(name = "Gestión de Presupuestos", description = "Endpoints para la gestión de presupuestos del sistema")
@SecurityRequirement(name = "bearerAuth")
public class PresupuestoController {

    @Autowired
    private PresupuestoService presupuestoService;

    @PostMapping
    @Operation(summary = "Crear presupuesto", description = "Crea un nuevo presupuesto en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Presupuesto creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<PresupuestoResponseDto> crearPresupuesto(@Valid @RequestBody PresupuestoCreateDto presupuestoCreateDto) {
        PresupuestoResponseDto presupuestoCreado = presupuestoService.crearPresupuesto(presupuestoCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(presupuestoCreado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener presupuesto por ID", description = "Obtiene los detalles de un presupuesto específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Presupuesto encontrado"),
            @ApiResponse(responseCode = "404", description = "Presupuesto no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<PresupuestoResponseDto> obtenerPresupuestoPorId(
            @Parameter(description = "ID del presupuesto") @PathVariable Long id) {
        PresupuestoResponseDto presupuesto = presupuestoService.obtenerPresupuestoPorId(id);
        return ResponseEntity.ok(presupuesto);
    }

    @GetMapping
    @Operation(summary = "Listar presupuestos", description = "Obtiene una lista paginada de presupuestos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de presupuestos obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<Page<PresupuestoListDto>> obtenerPresupuestos(Pageable pageable) {
        Page<PresupuestoListDto> presupuestos = presupuestoService.obtenerPresupuestos(pageable);
        return ResponseEntity.ok(presupuestos);
    }

    @GetMapping("/servicio/{servicioId}")
    @Operation(summary = "Obtener presupuestos por servicio", description = "Obtiene presupuestos de un servicio específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de presupuestos obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<PresupuestoListDto>> obtenerPresupuestosPorServicio(
            @Parameter(description = "ID del servicio") @PathVariable Long servicioId) {
        List<PresupuestoListDto> presupuestos = presupuestoService.obtenerPresupuestosPorServicio(servicioId);
        return ResponseEntity.ok(presupuestos);
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Obtener presupuestos por estado", description = "Obtiene presupuestos filtrados por estado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de presupuestos obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<PresupuestoListDto>> obtenerPresupuestosPorEstado(
            @Parameter(description = "Estado del presupuesto") @PathVariable EstadoPresupuesto estado) {
        List<PresupuestoListDto> presupuestos = presupuestoService.obtenerPresupuestosPorEstado(estado);
        return ResponseEntity.ok(presupuestos);
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Obtener presupuestos por cliente", description = "Obtiene presupuestos de un cliente específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de presupuestos obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<PresupuestoListDto>> obtenerPresupuestosPorCliente(
            @Parameter(description = "ID del cliente") @PathVariable Long clienteId) {
        List<PresupuestoListDto> presupuestos = presupuestoService.obtenerPresupuestosPorCliente(clienteId);
        return ResponseEntity.ok(presupuestos);
    }

    @GetMapping("/fechas")
    @Operation(summary = "Obtener presupuestos por fechas", description = "Obtiene presupuestos en un rango de fechas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de presupuestos obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<PresupuestoListDto>> obtenerPresupuestosPorFechas(
            @Parameter(description = "Fecha de inicio") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @Parameter(description = "Fecha de fin") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        List<PresupuestoListDto> presupuestos = presupuestoService.obtenerPresupuestosPorFechas(fechaInicio, fechaFin);
        return ResponseEntity.ok(presupuestos);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar presupuesto", description = "Actualiza los datos de un presupuesto existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Presupuesto actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Presupuesto no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<PresupuestoResponseDto> actualizarPresupuesto(
            @Parameter(description = "ID del presupuesto") @PathVariable Long id,
            @Valid @RequestBody PresupuestoUpdateDto presupuestoUpdateDto) {
        PresupuestoResponseDto presupuestoActualizado = presupuestoService.actualizarPresupuesto(id, presupuestoUpdateDto);
        return ResponseEntity.ok(presupuestoActualizado);
    }

    @PatchMapping("/{id}/cambiar-estado")
    @Operation(summary = "Cambiar estado", description = "Cambia el estado de un presupuesto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado cambiado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Presupuesto no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<PresupuestoResponseDto> cambiarEstadoPresupuesto(
            @Parameter(description = "ID del presupuesto") @PathVariable Long id,
            @Parameter(description = "Nuevo estado") @RequestParam EstadoPresupuesto nuevoEstado) {
        PresupuestoResponseDto presupuestoActualizado = presupuestoService.cambiarEstadoPresupuesto(id, nuevoEstado);
        return ResponseEntity.ok(presupuestoActualizado);
    }

    @PatchMapping("/{id}/aprobar")
    @Operation(summary = "Aprobar presupuesto", description = "Aprueba un presupuesto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Presupuesto aprobado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Presupuesto no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<PresupuestoResponseDto> aprobarPresupuesto(
            @Parameter(description = "ID del presupuesto") @PathVariable Long id) {
        PresupuestoResponseDto presupuestoActualizado = presupuestoService.aprobarPresupuesto(id);
        return ResponseEntity.ok(presupuestoActualizado);
    }

    @PatchMapping("/{id}/rechazar")
    @Operation(summary = "Rechazar presupuesto", description = "Rechaza un presupuesto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Presupuesto rechazado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Presupuesto no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<PresupuestoResponseDto> rechazarPresupuesto(
            @Parameter(description = "ID del presupuesto") @PathVariable Long id) {
        PresupuestoResponseDto presupuestoActualizado = presupuestoService.rechazarPresupuesto(id);
        return ResponseEntity.ok(presupuestoActualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar presupuesto", description = "Elimina un presupuesto del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Presupuesto eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Presupuesto no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO')")
    public ResponseEntity<Void> eliminarPresupuesto(
            @Parameter(description = "ID del presupuesto") @PathVariable Long id) {
        presupuestoService.eliminarPresupuesto(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/generar-numero")
    @Operation(summary = "Generar número de presupuesto", description = "Genera un número de presupuesto automático")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Número generado exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<String> generarNumeroPresupuesto() {
        String numero = presupuestoService.generarNumeroPresupuesto();
        return ResponseEntity.ok(numero);
    }
}
