package com.sigret.controllers.equipo;

import com.sigret.dtos.equipo.EquipoCreateDto;
import com.sigret.dtos.equipo.EquipoListDto;
import com.sigret.dtos.equipo.EquipoResponseDto;
import com.sigret.dtos.equipo.EquipoUpdateDto;
import com.sigret.services.EquipoService;
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
@RequestMapping("/api/equipos")
@Tag(name = "Gestión de Equipos", description = "Endpoints para la gestión de equipos del sistema")
@SecurityRequirement(name = "bearerAuth")
public class EquipoController {

    @Autowired
    private EquipoService equipoService;

    @PostMapping
    @Operation(summary = "Crear equipo", description = "Crea un nuevo equipo en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Equipo creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Número de serie ya existe")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<EquipoResponseDto> crearEquipo(@Valid @RequestBody EquipoCreateDto equipoCreateDto) {
        EquipoResponseDto equipoCreado = equipoService.crearEquipo(equipoCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(equipoCreado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener equipo por ID", description = "Obtiene los detalles de un equipo específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Equipo encontrado"),
            @ApiResponse(responseCode = "404", description = "Equipo no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<EquipoResponseDto> obtenerEquipoPorId(
            @Parameter(description = "ID del equipo") @PathVariable Long id) {
        EquipoResponseDto equipo = equipoService.obtenerEquipoPorId(id);
        return ResponseEntity.ok(equipo);
    }

    @GetMapping
    @Operation(summary = "Listar equipos", description = "Obtiene una lista paginada de equipos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de equipos obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<Page<EquipoListDto>> obtenerEquipos(Pageable pageable) {
        Page<EquipoListDto> equipos = equipoService.obtenerEquipos(pageable);
        return ResponseEntity.ok(equipos);
    }

    @GetMapping("/todos")
    @Operation(summary = "Obtener todos los equipos", description = "Obtiene una lista completa de equipos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de equipos obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<EquipoListDto>> obtenerTodosLosEquipos() {
        List<EquipoListDto> equipos = equipoService.obtenerTodosLosEquipos();
        return ResponseEntity.ok(equipos);
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar equipos", description = "Busca equipos por descripción o número de serie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Búsqueda realizada exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<EquipoListDto>> buscarEquipos(
            @Parameter(description = "Término de búsqueda") @RequestParam String termino) {
        List<EquipoListDto> equipos = equipoService.buscarEquipos(termino);
        return ResponseEntity.ok(equipos);
    }

    @GetMapping("/marca/{marcaId}")
    @Operation(summary = "Obtener equipos por marca", description = "Obtiene equipos filtrados por marca")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de equipos obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<EquipoListDto>> obtenerEquiposPorMarca(
            @Parameter(description = "ID de la marca") @PathVariable Long marcaId) {
        List<EquipoListDto> equipos = equipoService.obtenerEquiposPorMarca(marcaId);
        return ResponseEntity.ok(equipos);
    }

    @GetMapping("/tipo/{tipoEquipoId}")
    @Operation(summary = "Obtener equipos por tipo", description = "Obtiene equipos filtrados por tipo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de equipos obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<EquipoListDto>> obtenerEquiposPorTipo(
            @Parameter(description = "ID del tipo de equipo") @PathVariable Long tipoEquipoId) {
        List<EquipoListDto> equipos = equipoService.obtenerEquiposPorTipo(tipoEquipoId);
        return ResponseEntity.ok(equipos);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar equipo", description = "Actualiza los datos de un equipo existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Equipo actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Equipo no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<EquipoResponseDto> actualizarEquipo(
            @Parameter(description = "ID del equipo") @PathVariable Long id,
            @Valid @RequestBody EquipoUpdateDto equipoUpdateDto) {
        EquipoResponseDto equipoActualizado = equipoService.actualizarEquipo(id, equipoUpdateDto);
        return ResponseEntity.ok(equipoActualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar equipo", description = "Elimina un equipo del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Equipo eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Equipo no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO')")
    public ResponseEntity<Void> eliminarEquipo(
            @Parameter(description = "ID del equipo") @PathVariable Long id) {
        equipoService.eliminarEquipo(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/verificar-numero-serie")
    @Operation(summary = "Verificar número de serie", description = "Verifica si un número de serie ya existe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verificación completada")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<Boolean> verificarNumeroSerie(
            @Parameter(description = "Número de serie") @RequestParam String numeroSerie) {
        boolean existe = equipoService.existeEquipoConNumeroSerie(numeroSerie);
        return ResponseEntity.ok(existe);
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Obtener equipos de un cliente", description = "Obtiene todos los equipos asociados a un cliente específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de equipos obtenida exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<EquipoListDto>> obtenerEquiposPorCliente(
            @Parameter(description = "ID del cliente") @PathVariable Long clienteId) {
        List<EquipoListDto> equipos = equipoService.obtenerEquiposPorCliente(clienteId);
        return ResponseEntity.ok(equipos);
    }

    @PostMapping("/{equipoId}/cliente/{clienteId}")
    @Operation(summary = "Asociar equipo a cliente", description = "Asocia un equipo existente a un cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Equipo asociado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Equipo o cliente no encontrado"),
            @ApiResponse(responseCode = "409", description = "El equipo ya está asociado a este cliente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<Void> asociarEquipoACliente(
            @Parameter(description = "ID del equipo") @PathVariable Long equipoId,
            @Parameter(description = "ID del cliente") @PathVariable Long clienteId) {
        equipoService.asociarEquipoACliente(equipoId, clienteId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{equipoId}/cliente/{clienteId}")
    @Operation(summary = "Desasociar equipo de cliente", description = "Desasocia un equipo de un cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Equipo desasociado exitosamente"),
            @ApiResponse(responseCode = "404", description = "No existe asociación entre el equipo y el cliente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<Void> desasociarEquipoDeCliente(
            @Parameter(description = "ID del equipo") @PathVariable Long equipoId,
            @Parameter(description = "ID del cliente") @PathVariable Long clienteId) {
        equipoService.desasociarEquipoDeCliente(equipoId, clienteId);
        return ResponseEntity.ok().build();
    }
}
