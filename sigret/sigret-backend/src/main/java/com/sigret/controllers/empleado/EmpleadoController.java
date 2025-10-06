package com.sigret.controllers.empleado;

import com.sigret.dtos.empleado.EmpleadoCreateDto;
import com.sigret.dtos.empleado.EmpleadoListDto;
import com.sigret.dtos.empleado.EmpleadoResponseDto;
import com.sigret.dtos.empleado.EmpleadoUpdateDto;
import com.sigret.services.EmpleadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
//import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empleados")
@Tag(name = "Gestión de Empleados", description = "Endpoints para la gestión de empleados del sistema")
@SecurityRequirement(name = "bearerAuth")
public class EmpleadoController {

    @Autowired
    private EmpleadoService empleadoService;

    @PostMapping
    @Operation(summary = "Crear empleado", description = "Crea un nuevo empleado y automáticamente crea su usuario. Por defecto, el username y password será el documento del empleado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Empleado creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Documento o username ya existe")
    })
    @PreAuthorize("hasRole('PROPIETARIO')")
    public ResponseEntity<EmpleadoResponseDto> crearEmpleado(@Valid @RequestBody EmpleadoCreateDto empleadoCreateDto) {
        EmpleadoResponseDto empleadoCreado = empleadoService.crearEmpleado(empleadoCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(empleadoCreado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener empleado por ID", description = "Obtiene los detalles de un empleado específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Empleado encontrado"),
            @ApiResponse(responseCode = "404", description = "Empleado no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<EmpleadoResponseDto> obtenerEmpleadoPorId(
            @Parameter(description = "ID del empleado") @PathVariable Long id) {
        EmpleadoResponseDto empleado = empleadoService.obtenerEmpleadoPorId(id);
        return ResponseEntity.ok(empleado);
    }

    @GetMapping
    @Operation(summary = "Listar empleados con filtros", 
               description = "Obtiene una lista paginada de empleados con filtros opcionales por estado (activo/inactivo) y búsqueda por nombre, apellido o documento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de empleados obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<Page<EmpleadoListDto>> obtenerEmpleados(
            @Parameter(description = "Filtro por estado activo (true/false). Si no se proporciona, trae todos") 
            @RequestParam(required = false) Boolean activo,
            @Parameter(description = "Búsqueda por nombre, apellido, razón social o documento") 
            @RequestParam(required = false) String busqueda,
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(required = false, defaultValue = "10") int size,
            @Parameter(description = "Campo de ordenamiento", example = "id")
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @Parameter(description = "Dirección de ordenamiento (ASC/DESC)", example = "DESC")
            @RequestParam(required = false, defaultValue = "DESC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<EmpleadoListDto> empleados = empleadoService.obtenerEmpleadosConFiltros(activo, busqueda, pageable);
        return ResponseEntity.ok(empleados);
    }

    @GetMapping("/activos")
    @Operation(summary = "Listar empleados activos", description = "Obtiene una lista de empleados activos (sin paginación)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de empleados activos obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<List<EmpleadoListDto>> obtenerEmpleadosActivos() {
        List<EmpleadoListDto> empleados = empleadoService.obtenerEmpleadosActivos();
        return ResponseEntity.ok(empleados);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar empleado", description = "Actualiza los datos de un empleado existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Empleado actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Empleado no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PreAuthorize("hasRole('PROPIETARIO')")
    public ResponseEntity<EmpleadoResponseDto> actualizarEmpleado(
            @Parameter(description = "ID del empleado") @PathVariable Long id,
            @Valid @RequestBody EmpleadoUpdateDto empleadoUpdateDto) {
        EmpleadoResponseDto empleadoActualizado = empleadoService.actualizarEmpleado(id, empleadoUpdateDto);
        return ResponseEntity.ok(empleadoActualizado);
    }

    @PatchMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar empleado", description = "Desactiva un empleado y su usuario asociado (baja lógica)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Empleado desactivado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Empleado no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO')")
    public ResponseEntity<Void> desactivarEmpleado(
            @Parameter(description = "ID del empleado") @PathVariable Long id) {
        empleadoService.desactivarEmpleado(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/activar")
    @Operation(summary = "Activar empleado", description = "Activa un empleado y su usuario asociado previamente desactivado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Empleado activado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Empleado no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO')")
    public ResponseEntity<Void> activarEmpleado(
            @Parameter(description = "ID del empleado") @PathVariable Long id) {
        empleadoService.activarEmpleado(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar empleado", description = "Elimina permanentemente un empleado del sistema (hard delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Empleado eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Empleado no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO')")
    public ResponseEntity<Void> eliminarEmpleado(
            @Parameter(description = "ID del empleado") @PathVariable Long id) {
        empleadoService.eliminarEmpleado(id);
        return ResponseEntity.ok().build();
    }
}

