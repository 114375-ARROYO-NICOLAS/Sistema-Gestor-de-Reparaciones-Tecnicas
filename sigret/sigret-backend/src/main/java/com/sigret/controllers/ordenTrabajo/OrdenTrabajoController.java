package com.sigret.controllers.ordenTrabajo;

import com.sigret.dtos.ordenTrabajo.OrdenTrabajoCreateDto;
import com.sigret.dtos.ordenTrabajo.OrdenTrabajoListDto;
import com.sigret.dtos.ordenTrabajo.OrdenTrabajoResponseDto;
import com.sigret.dtos.ordenTrabajo.OrdenTrabajoUpdateDto;
import com.sigret.enums.EstadoOrdenTrabajo;
import com.sigret.services.OrdenTrabajoService;
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
@RequestMapping("/api/ordenes-trabajo")
@Tag(name = "Gestión de Órdenes de Trabajo", description = "Endpoints para la gestión de órdenes de trabajo del sistema")
@SecurityRequirement(name = "bearerAuth")
public class OrdenTrabajoController {

    @Autowired
    private OrdenTrabajoService ordenTrabajoService;

    @PostMapping
    @Operation(summary = "Crear orden de trabajo", description = "Crea una nueva orden de trabajo en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Orden de trabajo creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<OrdenTrabajoResponseDto> crearOrdenTrabajo(@Valid @RequestBody OrdenTrabajoCreateDto ordenTrabajoCreateDto) {
        OrdenTrabajoResponseDto ordenCreada = ordenTrabajoService.crearOrdenTrabajo(ordenTrabajoCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ordenCreada);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener orden de trabajo por ID", description = "Obtiene los detalles de una orden de trabajo específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden de trabajo encontrada"),
            @ApiResponse(responseCode = "404", description = "Orden de trabajo no encontrada")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<OrdenTrabajoResponseDto> obtenerOrdenTrabajoPorId(
            @Parameter(description = "ID de la orden de trabajo") @PathVariable Long id) {
        OrdenTrabajoResponseDto orden = ordenTrabajoService.obtenerOrdenTrabajoPorId(id);
        return ResponseEntity.ok(orden);
    }

    @GetMapping
    @Operation(summary = "Listar órdenes de trabajo", description = "Obtiene una lista paginada de órdenes de trabajo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de órdenes de trabajo obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<Page<OrdenTrabajoListDto>> obtenerOrdenesTrabajo(Pageable pageable) {
        Page<OrdenTrabajoListDto> ordenes = ordenTrabajoService.obtenerOrdenesTrabajo(pageable);
        return ResponseEntity.ok(ordenes);
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Obtener órdenes por estado", description = "Obtiene órdenes de trabajo filtradas por estado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de órdenes obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<OrdenTrabajoListDto>> obtenerOrdenesTrabajoPorEstado(
            @Parameter(description = "Estado de la orden") @PathVariable EstadoOrdenTrabajo estado) {
        List<OrdenTrabajoListDto> ordenes = ordenTrabajoService.obtenerOrdenesTrabajoPorEstado(estado);
        return ResponseEntity.ok(ordenes);
    }

    @GetMapping("/empleado/{empleadoId}")
    @Operation(summary = "Obtener órdenes por empleado", description = "Obtiene órdenes de trabajo asignadas a un empleado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de órdenes obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<OrdenTrabajoListDto>> obtenerOrdenesTrabajoPorEmpleado(
            @Parameter(description = "ID del empleado") @PathVariable Long empleadoId) {
        List<OrdenTrabajoListDto> ordenes = ordenTrabajoService.obtenerOrdenesTrabajoPorEmpleado(empleadoId);
        return ResponseEntity.ok(ordenes);
    }

    @GetMapping("/servicio/{servicioId}")
    @Operation(summary = "Obtener órdenes por servicio", description = "Obtiene órdenes de trabajo de un servicio específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de órdenes obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<OrdenTrabajoListDto>> obtenerOrdenesTrabajoPorServicio(
            @Parameter(description = "ID del servicio") @PathVariable Long servicioId) {
        List<OrdenTrabajoListDto> ordenes = ordenTrabajoService.obtenerOrdenesTrabajoPorServicio(servicioId);
        return ResponseEntity.ok(ordenes);
    }

    @GetMapping("/fechas")
    @Operation(summary = "Obtener órdenes por fechas", description = "Obtiene órdenes de trabajo en un rango de fechas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de órdenes obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<OrdenTrabajoListDto>> obtenerOrdenesTrabajoPorFechas(
            @Parameter(description = "Fecha de inicio") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @Parameter(description = "Fecha de fin") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        List<OrdenTrabajoListDto> ordenes = ordenTrabajoService.obtenerOrdenesTrabajoPorFechas(fechaInicio, fechaFin);
        return ResponseEntity.ok(ordenes);
    }

    @GetMapping("/sin-costo")
    @Operation(summary = "Obtener órdenes sin costo", description = "Obtiene órdenes de trabajo sin costo (garantías)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de órdenes obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<OrdenTrabajoListDto>> obtenerOrdenesTrabajoSinCosto() {
        List<OrdenTrabajoListDto> ordenes = ordenTrabajoService.obtenerOrdenesTrabajoSinCosto();
        return ResponseEntity.ok(ordenes);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar orden de trabajo", description = "Actualiza los datos de una orden de trabajo existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden de trabajo actualizada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Orden de trabajo no encontrada"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<OrdenTrabajoResponseDto> actualizarOrdenTrabajo(
            @Parameter(description = "ID de la orden de trabajo") @PathVariable Long id,
            @Valid @RequestBody OrdenTrabajoUpdateDto ordenTrabajoUpdateDto) {
        OrdenTrabajoResponseDto ordenActualizada = ordenTrabajoService.actualizarOrdenTrabajo(id, ordenTrabajoUpdateDto);
        return ResponseEntity.ok(ordenActualizada);
    }

    @PatchMapping("/{id}/cambiar-estado")
    @Operation(summary = "Cambiar estado", description = "Cambia el estado de una orden de trabajo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado cambiado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Orden de trabajo no encontrada")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<OrdenTrabajoResponseDto> cambiarEstadoOrdenTrabajo(
            @Parameter(description = "ID de la orden de trabajo") @PathVariable Long id,
            @Parameter(description = "Nuevo estado") @RequestParam EstadoOrdenTrabajo nuevoEstado) {
        OrdenTrabajoResponseDto ordenActualizada = ordenTrabajoService.cambiarEstadoOrdenTrabajo(id, nuevoEstado);
        return ResponseEntity.ok(ordenActualizada);
    }

    @PatchMapping("/{id}/asignar-empleado")
    @Operation(summary = "Asignar empleado", description = "Asigna un empleado a una orden de trabajo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Empleado asignado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Orden de trabajo o empleado no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<OrdenTrabajoResponseDto> asignarEmpleado(
            @Parameter(description = "ID de la orden de trabajo") @PathVariable Long id,
            @Parameter(description = "ID del empleado") @RequestParam Long empleadoId) {
        OrdenTrabajoResponseDto ordenActualizada = ordenTrabajoService.asignarEmpleado(id, empleadoId);
        return ResponseEntity.ok(ordenActualizada);
    }

    @PatchMapping("/{id}/iniciar")
    @Operation(summary = "Iniciar orden de trabajo", description = "Inicia una orden de trabajo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden de trabajo iniciada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Orden de trabajo no encontrada")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<OrdenTrabajoResponseDto> iniciarOrdenTrabajo(
            @Parameter(description = "ID de la orden de trabajo") @PathVariable Long id) {
        OrdenTrabajoResponseDto ordenActualizada = ordenTrabajoService.iniciarOrdenTrabajo(id);
        return ResponseEntity.ok(ordenActualizada);
    }

    @PatchMapping("/{id}/finalizar")
    @Operation(summary = "Finalizar orden de trabajo", description = "Finaliza una orden de trabajo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden de trabajo finalizada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Orden de trabajo no encontrada")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<OrdenTrabajoResponseDto> finalizarOrdenTrabajo(
            @Parameter(description = "ID de la orden de trabajo") @PathVariable Long id) {
        OrdenTrabajoResponseDto ordenActualizada = ordenTrabajoService.finalizarOrdenTrabajo(id);
        return ResponseEntity.ok(ordenActualizada);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar orden de trabajo", description = "Elimina una orden de trabajo del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden de trabajo eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Orden de trabajo no encontrada")
    })
    @PreAuthorize("hasRole('PROPIETARIO')")
    public ResponseEntity<Void> eliminarOrdenTrabajo(
            @Parameter(description = "ID de la orden de trabajo") @PathVariable Long id) {
        ordenTrabajoService.eliminarOrdenTrabajo(id);
        return ResponseEntity.ok().build();
    }
}
