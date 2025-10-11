package com.sigret.controllers.cliente;

import com.sigret.dtos.cliente.ClienteCreateDto;
import com.sigret.dtos.cliente.ClienteListDto;
import com.sigret.dtos.cliente.ClienteResponseDto;
import com.sigret.dtos.cliente.ClienteUpdateDto;
import com.sigret.services.ClienteService;
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
@RequestMapping("/api/clientes")
@Tag(name = "Gestión de Clientes", description = "Endpoints para la gestión de clientes del sistema")
@SecurityRequirement(name = "bearerAuth")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @PostMapping
    @Operation(summary = "Crear cliente", description = "Crea un nuevo cliente en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Documento ya existe")
    })
    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ADMINISTRATIVO', 'TECNICO')")
    public ResponseEntity<ClienteResponseDto> crearCliente(@Valid @RequestBody ClienteCreateDto clienteCreateDto) {
        ClienteResponseDto clienteCreado = clienteService.crearCliente(clienteCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteCreado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cliente por ID", description = "Obtiene los detalles de un cliente específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ADMINISTRATIVO', 'TECNICO')")
    public ResponseEntity<ClienteResponseDto> obtenerClientePorId(
            @Parameter(description = "ID del cliente") @PathVariable Long id) {
        ClienteResponseDto cliente = clienteService.obtenerClientePorId(id);
        return ResponseEntity.ok(cliente);
    }

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Obtiene una lista paginada de clientes activos con filtros opcionales")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de clientes obtenida exitosamente")
    })
    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ADMINISTRATIVO', 'TECNICO')")
    public ResponseEntity<Page<ClienteListDto>> obtenerClientes(
            Pageable pageable,
            @Parameter(description = "Filtro de búsqueda (nombre, apellido, razón social o documento)") 
            @RequestParam(required = false) String filtro) {
        Page<ClienteListDto> clientes = clienteService.obtenerClientes(pageable, filtro);
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/todos")
    @Operation(summary = "Obtener todos los clientes", description = "Obtiene una lista completa de clientes activos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de clientes obtenida exitosamente")
    })
    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ADMINISTRATIVO', 'TECNICO')")
    public ResponseEntity<List<ClienteListDto>> obtenerTodosLosClientes() {
        List<ClienteListDto> clientes = clienteService.obtenerTodosLosClientes();
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/autocompletado")
    @Operation(summary = "Autocompletado de clientes", description = "Busca clientes por nombre o documento para autocompletado (máximo 10 resultados)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Búsqueda realizada exitosamente")
    })
    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ADMINISTRATIVO', 'TECNICO')")
    public ResponseEntity<List<ClienteListDto>> autocompletadoClientes(
            @Parameter(description = "Término de búsqueda") @RequestParam String termino,
            @Parameter(description = "Límite de resultados (por defecto 10)") @RequestParam(defaultValue = "10") int limite) {
        List<ClienteListDto> clientes = clienteService.buscarClientesAutocompletado(termino, limite);
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/documento/{documento}")
    @Operation(summary = "Obtener cliente por documento", description = "Obtiene un cliente activo por su número de documento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ADMINISTRATIVO', 'TECNICO')")
    public ResponseEntity<ClienteResponseDto> obtenerClientePorDocumento(
            @Parameter(description = "Número de documento") @PathVariable String documento) {
        ClienteResponseDto cliente = clienteService.obtenerClientePorDocumento(documento);
        return ResponseEntity.ok(cliente);
    }

    @GetMapping("/{id}/con-equipos")
    @Operation(summary = "Obtener cliente con equipos", description = "Obtiene un cliente con sus equipos asociados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente con equipos encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ADMINISTRATIVO', 'TECNICO')")
    public ResponseEntity<ClienteResponseDto> obtenerClienteConEquipos(
            @Parameter(description = "ID del cliente") @PathVariable Long id) {
        ClienteResponseDto cliente = clienteService.obtenerClienteConEquipos(id);
        return ResponseEntity.ok(cliente);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cliente", description = "Actualiza los datos de un cliente existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ADMINISTRATIVO', 'TECNICO')")
    public ResponseEntity<ClienteResponseDto> actualizarCliente(
            @Parameter(description = "ID del cliente") @PathVariable Long id,
            @Valid @RequestBody ClienteUpdateDto clienteUpdateDto) {
        ClienteResponseDto clienteActualizado = clienteService.actualizarCliente(id, clienteUpdateDto);
        return ResponseEntity.ok(clienteActualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar cliente (baja lógica)", description = "Realiza una baja lógica del cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ADMINISTRATIVO')")
    public ResponseEntity<Void> eliminarCliente(
            @Parameter(description = "ID del cliente") @PathVariable Long id) {
        clienteService.eliminarCliente(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/reactivar")
    @Operation(summary = "Reactivar cliente", description = "Reactiva un cliente previamente desactivado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente reactivado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @PreAuthorize("hasAnyRole('PROPIETARIO', 'ADMINISTRATIVO')")
    public ResponseEntity<Void> reactivarCliente(
            @Parameter(description = "ID del cliente") @PathVariable Long id) {
        clienteService.reactivarCliente(id);
        return ResponseEntity.ok().build();
    }
}
