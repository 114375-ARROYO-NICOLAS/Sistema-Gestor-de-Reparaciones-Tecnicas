package com.sigret.controllers.tipoDocumento;

import com.sigret.dtos.tipoDocumento.TipoDocumentoCreateDto;
import com.sigret.dtos.tipoDocumento.TipoDocumentoListDto;
import com.sigret.dtos.tipoDocumento.TipoDocumentoResponseDto;
import com.sigret.dtos.tipoDocumento.TipoDocumentoUpdateDto;
import com.sigret.services.TipoDocumentoService;
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
@RequestMapping("/api/tipos-documento")
@Tag(name = "Tipos de Documento", description = "Endpoints para la gestión de tipos de documento")
@SecurityRequirement(name = "bearerAuth")
public class TipoDocumentoController {

    @Autowired
    private TipoDocumentoService tipoDocumentoService;

    @PostMapping
    @Operation(summary = "Crear tipo de documento", description = "Crea un nuevo tipo de documento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tipo de documento creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Ya existe un tipo de documento con esa descripción")
    })
    @PreAuthorize("hasRole('PROPIETARIO')")
    public ResponseEntity<TipoDocumentoResponseDto> crearTipoDocumento(@Valid @RequestBody TipoDocumentoCreateDto tipoDocumentoCreateDto) {
        TipoDocumentoResponseDto tipoDocumentoCreado = tipoDocumentoService.crearTipoDocumento(tipoDocumentoCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(tipoDocumentoCreado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de documento por ID", description = "Obtiene los detalles de un tipo de documento específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de documento encontrado"),
            @ApiResponse(responseCode = "404", description = "Tipo de documento no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<TipoDocumentoResponseDto> obtenerTipoDocumentoPorId(
            @Parameter(description = "ID del tipo de documento") @PathVariable Long id) {
        TipoDocumentoResponseDto tipoDocumento = tipoDocumentoService.obtenerTipoDocumentoPorId(id);
        return ResponseEntity.ok(tipoDocumento);
    }

    @GetMapping
    @Operation(summary = "Listar tipos de documento", description = "Obtiene una lista de todos los tipos de documento (endpoint público)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tipos de documento obtenida exitosamente")
    })
    // Endpoint público - Sin @PreAuthorize para permitir acceso sin autenticación
    public ResponseEntity<List<TipoDocumentoListDto>> obtenerTodosTiposDocumento() {
        List<TipoDocumentoListDto> tiposDocumento = tipoDocumentoService.obtenerTodosTiposDocumento();
        return ResponseEntity.ok(tiposDocumento);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de documento", description = "Actualiza los datos de un tipo de documento existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de documento actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Tipo de documento no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Ya existe un tipo de documento con esa descripción")
    })
    @PreAuthorize("hasRole('PROPIETARIO')")
    public ResponseEntity<TipoDocumentoResponseDto> actualizarTipoDocumento(
            @Parameter(description = "ID del tipo de documento") @PathVariable Long id,
            @Valid @RequestBody TipoDocumentoUpdateDto tipoDocumentoUpdateDto) {
        TipoDocumentoResponseDto tipoDocumentoActualizado = tipoDocumentoService.actualizarTipoDocumento(id, tipoDocumentoUpdateDto);
        return ResponseEntity.ok(tipoDocumentoActualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo de documento", description = "Elimina un tipo de documento del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de documento eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Tipo de documento no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO')")
    public ResponseEntity<Void> eliminarTipoDocumento(
            @Parameter(description = "ID del tipo de documento") @PathVariable Long id) {
        tipoDocumentoService.eliminarTipoDocumento(id);
        return ResponseEntity.ok().build();
    }
}

