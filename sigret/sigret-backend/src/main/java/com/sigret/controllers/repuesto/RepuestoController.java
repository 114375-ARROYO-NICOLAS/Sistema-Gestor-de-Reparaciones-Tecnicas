package com.sigret.controllers.repuesto;

import com.sigret.dtos.repuesto.RepuestoCreateDto;
import com.sigret.dtos.repuesto.RepuestoDto;
import com.sigret.dtos.repuesto.RepuestoListDto;
import com.sigret.dtos.repuesto.RepuestoResponseDto;
import com.sigret.dtos.repuesto.RepuestoUpdateDto;
import com.sigret.entities.Repuesto;
import com.sigret.repositories.RepuestoRepository;
import com.sigret.services.RepuestoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/repuestos")
@RequiredArgsConstructor
@Tag(name = "Gestión de Repuestos", description = "Endpoints para la gestión de repuestos del sistema")
@SecurityRequirement(name = "bearerAuth")
public class RepuestoController {

    private final RepuestoRepository repuestoRepository;
    private final RepuestoService repuestoService;

    @PostMapping
    @Operation(summary = "Crear repuesto", description = "Crea un nuevo repuesto en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Repuesto creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Repuesto ya existe")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<RepuestoResponseDto> crearRepuesto(@Valid @RequestBody RepuestoCreateDto repuestoCreateDto) {
        RepuestoResponseDto repuestoCreado = repuestoService.crearRepuesto(repuestoCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(repuestoCreado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener repuesto por ID", description = "Obtiene los detalles de un repuesto específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Repuesto encontrado"),
            @ApiResponse(responseCode = "404", description = "Repuesto no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<RepuestoResponseDto> obtenerRepuestoPorId(
            @Parameter(description = "ID del repuesto") @PathVariable Long id) {
        RepuestoResponseDto repuesto = repuestoService.obtenerRepuestoPorId(id);
        return ResponseEntity.ok(repuesto);
    }

    @GetMapping
    @Operation(summary = "Obtener todos los repuestos", description = "Obtiene una lista completa de repuestos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de repuestos obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<RepuestoListDto>> obtenerTodosLosRepuestos() {
        List<RepuestoListDto> repuestos = repuestoService.obtenerTodosLosRepuestos();
        return ResponseEntity.ok(repuestos);
    }

    @GetMapping("/tipo-equipo/{tipoEquipoId}")
    @Operation(summary = "Obtener repuestos por tipo de equipo", description = "Obtiene repuestos filtrados por tipo de equipo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de repuestos obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<RepuestoListDto>> obtenerRepuestosPorTipoEquipo(
            @Parameter(description = "ID del tipo de equipo") @PathVariable Long tipoEquipoId) {
        List<RepuestoListDto> repuestos = repuestoService.obtenerRepuestosPorTipoEquipo(tipoEquipoId);
        return ResponseEntity.ok(repuestos);
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar repuestos", description = "Busca repuestos por descripción o tipo de equipo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Búsqueda realizada exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<RepuestoDto>> buscar(@RequestParam(required = false) String query) {
        List<Repuesto> repuestos;

        if (query == null || query.trim().isEmpty()) {
            repuestos = repuestoRepository.findAll();
        } else {
            repuestos = repuestoRepository.buscarPorTermino(query);
        }

        List<RepuestoDto> dtos = repuestos.stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar repuesto", description = "Actualiza los datos de un repuesto existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Repuesto actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Repuesto no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Repuesto ya existe")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<RepuestoResponseDto> actualizarRepuesto(
            @Parameter(description = "ID del repuesto") @PathVariable Long id,
            @Valid @RequestBody RepuestoUpdateDto repuestoUpdateDto) {
        RepuestoResponseDto repuestoActualizado = repuestoService.actualizarRepuesto(id, repuestoUpdateDto);
        return ResponseEntity.ok(repuestoActualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar repuesto", description = "Elimina un repuesto del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Repuesto eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Repuesto no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO')")
    public ResponseEntity<Void> eliminarRepuesto(
            @Parameter(description = "ID del repuesto") @PathVariable Long id) {
        repuestoService.eliminarRepuesto(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/verificar")
    @Operation(summary = "Verificar existencia", description = "Verifica si un repuesto ya existe con la misma descripción y tipo de equipo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verificación completada")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<Boolean> verificarExistencia(
            @Parameter(description = "Descripción del repuesto") @RequestParam String descripcion,
            @Parameter(description = "ID del tipo de equipo") @RequestParam Long tipoEquipoId) {
        boolean existe = repuestoService.existeRepuesto(descripcion, tipoEquipoId);
        return ResponseEntity.ok(existe);
    }

    private RepuestoDto convertirADto(Repuesto repuesto) {
        RepuestoDto dto = new RepuestoDto();
        dto.setId(repuesto.getId());
        dto.setDescripcion(repuesto.getDescripcion());
        dto.setTipoEquipo(repuesto.getTipoEquipo() != null ?
                repuesto.getTipoEquipo().getDescripcion() : null);
        dto.setDescripcionCompleta(repuesto.getDescripcionCompleta());
        return dto;
    }
}
