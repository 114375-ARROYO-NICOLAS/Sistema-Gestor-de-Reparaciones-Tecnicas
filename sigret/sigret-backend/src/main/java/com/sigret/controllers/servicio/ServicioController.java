package com.sigret.controllers.servicio;

import com.sigret.dtos.servicio.ServicioCreateDto;
import com.sigret.dtos.servicio.ServicioListDto;
import com.sigret.dtos.servicio.ServicioResponseDto;
import com.sigret.dtos.servicio.ServicioUpdateDto;
import com.sigret.enums.EstadoServicio;
import com.sigret.services.ServicioService;
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
@RequestMapping("/api/servicios")
@Tag(name = "Gestión de Servicios", description = "Endpoints para la gestión de servicios del sistema")
@SecurityRequirement(name = "bearerAuth")
public class ServicioController {

    @Autowired
    private ServicioService servicioService;

    @PostMapping
    @Operation(summary = "Crear servicio", description = "Crea un nuevo servicio en el sistema con número automático")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Servicio creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<ServicioResponseDto> crearServicio(@Valid @RequestBody ServicioCreateDto servicioCreateDto) {
        ServicioResponseDto servicioCreado = servicioService.crearServicio(servicioCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioCreado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener servicio por ID", description = "Obtiene los detalles de un servicio específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicio encontrado"),
            @ApiResponse(responseCode = "404", description = "Servicio no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<ServicioResponseDto> obtenerServicioPorId(
            @Parameter(description = "ID del servicio") @PathVariable Long id) {
        ServicioResponseDto servicio = servicioService.obtenerServicioPorId(id);
        return ResponseEntity.ok(servicio);
    }

    @GetMapping
    @Operation(summary = "Listar servicios", description = "Obtiene una lista paginada de servicios")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de servicios obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<Page<ServicioListDto>> obtenerServicios(Pageable pageable) {
        Page<ServicioListDto> servicios = servicioService.obtenerServicios(pageable);
        return ResponseEntity.ok(servicios);
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Obtener servicios por estado", description = "Obtiene servicios filtrados por estado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de servicios obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<ServicioListDto>> obtenerServiciosPorEstado(
            @Parameter(description = "Estado del servicio") @PathVariable EstadoServicio estado) {
        List<ServicioListDto> servicios = servicioService.obtenerServiciosPorEstado(estado);
        return ResponseEntity.ok(servicios);
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Obtener servicios por cliente", description = "Obtiene servicios de un cliente específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de servicios obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<ServicioListDto>> obtenerServiciosPorCliente(
            @Parameter(description = "ID del cliente") @PathVariable Long clienteId) {
        List<ServicioListDto> servicios = servicioService.obtenerServiciosPorCliente(clienteId);
        return ResponseEntity.ok(servicios);
    }

    @GetMapping("/fechas")
    @Operation(summary = "Obtener servicios por fechas", description = "Obtiene servicios en un rango de fechas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de servicios obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<ServicioListDto>> obtenerServiciosPorFechas(
            @Parameter(description = "Fecha de inicio") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @Parameter(description = "Fecha de fin") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        List<ServicioListDto> servicios = servicioService.obtenerServiciosPorFechas(fechaInicio, fechaFin);
        return ResponseEntity.ok(servicios);
    }

    @GetMapping("/numero/{numeroServicio}")
    @Operation(summary = "Obtener servicio por número", description = "Obtiene un servicio por su número de servicio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicio encontrado"),
            @ApiResponse(responseCode = "404", description = "Servicio no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<ServicioResponseDto> obtenerServicioPorNumero(
            @Parameter(description = "Número de servicio") @PathVariable String numeroServicio) {
        ServicioResponseDto servicio = servicioService.obtenerServicioPorNumero(numeroServicio);
        return ResponseEntity.ok(servicio);
    }

    @GetMapping("/garantias")
    @Operation(summary = "Obtener servicios de garantía", description = "Obtiene servicios que son de garantía")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de servicios de garantía obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<ServicioListDto>> obtenerServiciosGarantia() {
        List<ServicioListDto> servicios = servicioService.obtenerServiciosGarantia();
        return ResponseEntity.ok(servicios);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar servicio", description = "Actualiza los datos de un servicio existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicio actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Servicio no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<ServicioResponseDto> actualizarServicio(
            @Parameter(description = "ID del servicio") @PathVariable Long id,
            @Valid @RequestBody ServicioUpdateDto servicioUpdateDto) {
        ServicioResponseDto servicioActualizado = servicioService.actualizarServicio(id, servicioUpdateDto);
        return ResponseEntity.ok(servicioActualizado);
    }

    @PatchMapping("/{id}/cambiar-estado")
    @Operation(summary = "Cambiar estado", description = "Cambia el estado de un servicio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado cambiado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Servicio no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<ServicioResponseDto> cambiarEstadoServicio(
            @Parameter(description = "ID del servicio") @PathVariable Long id,
            @Parameter(description = "Nuevo estado") @RequestParam EstadoServicio nuevoEstado) {
        ServicioResponseDto servicioActualizado = servicioService.cambiarEstadoServicio(id, nuevoEstado);
        return ResponseEntity.ok(servicioActualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar servicio", description = "Elimina un servicio del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicio eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Servicio no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO')")
    public ResponseEntity<Void> eliminarServicio(
            @Parameter(description = "ID del servicio") @PathVariable Long id) {
        servicioService.eliminarServicio(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/generar-numero")
    @Operation(summary = "Generar número de servicio", description = "Genera un número de servicio automático")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Número generado exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<String> generarNumeroServicio() {
        String numero = servicioService.generarNumeroServicio();
        return ResponseEntity.ok(numero);
    }

    @PostMapping("/garantia/{servicioOriginalId}")
    @Operation(summary = "Crear servicio de garantía", description = "Crea un servicio de garantía basado en un servicio original")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Servicio de garantía creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Servicio original no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<ServicioResponseDto> crearServicioGarantia(
            @Parameter(description = "ID del servicio original") @PathVariable Long servicioOriginalId,
            @Valid @RequestBody ServicioCreateDto servicioGarantiaDto) {
        ServicioResponseDto servicioGarantiaCreado = servicioService.crearServicioGarantia(servicioOriginalId, servicioGarantiaDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioGarantiaCreado);
    }
}
