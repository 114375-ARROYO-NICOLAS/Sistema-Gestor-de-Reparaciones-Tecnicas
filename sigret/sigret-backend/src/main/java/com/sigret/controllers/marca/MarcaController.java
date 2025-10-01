package com.sigret.controllers.marca;

import com.sigret.dtos.marca.MarcaCreateDto;
import com.sigret.dtos.marca.MarcaListDto;
import com.sigret.dtos.marca.MarcaResponseDto;
import com.sigret.dtos.marca.MarcaUpdateDto;
import com.sigret.services.MarcaService;
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
@RequestMapping("/api/marcas")
@Tag(name = "Gestión de Marcas", description = "Endpoints para la gestión de marcas del sistema")
@SecurityRequirement(name = "bearerAuth")
public class MarcaController {

    @Autowired
    private MarcaService marcaService;

    @PostMapping
    @Operation(summary = "Crear marca", description = "Crea una nueva marca en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Marca creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Marca ya existe")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<MarcaResponseDto> crearMarca(@Valid @RequestBody MarcaCreateDto marcaCreateDto) {
        MarcaResponseDto marcaCreada = marcaService.crearMarca(marcaCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(marcaCreada);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener marca por ID", description = "Obtiene los detalles de una marca específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Marca encontrada"),
            @ApiResponse(responseCode = "404", description = "Marca no encontrada")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<MarcaResponseDto> obtenerMarcaPorId(
            @Parameter(description = "ID de la marca") @PathVariable Long id) {
        MarcaResponseDto marca = marcaService.obtenerMarcaPorId(id);
        return ResponseEntity.ok(marca);
    }

    @GetMapping
    @Operation(summary = "Listar marcas", description = "Obtiene una lista paginada de marcas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de marcas obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<Page<MarcaListDto>> obtenerMarcas(Pageable pageable) {
        Page<MarcaListDto> marcas = marcaService.obtenerMarcas(pageable);
        return ResponseEntity.ok(marcas);
    }

    @GetMapping("/todos")
    @Operation(summary = "Obtener todas las marcas", description = "Obtiene una lista completa de marcas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de marcas obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<MarcaListDto>> obtenerTodasLasMarcas() {
        List<MarcaListDto> marcas = marcaService.obtenerTodasLasMarcas();
        return ResponseEntity.ok(marcas);
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar marcas", description = "Busca marcas por descripción")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Búsqueda realizada exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<MarcaListDto>> buscarMarcas(
            @Parameter(description = "Término de búsqueda") @RequestParam String termino) {
        List<MarcaListDto> marcas = marcaService.buscarMarcas(termino);
        return ResponseEntity.ok(marcas);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar marca", description = "Actualiza los datos de una marca existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Marca actualizada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Marca no encontrada"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Marca ya existe")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<MarcaResponseDto> actualizarMarca(
            @Parameter(description = "ID de la marca") @PathVariable Long id,
            @Valid @RequestBody MarcaUpdateDto marcaUpdateDto) {
        MarcaResponseDto marcaActualizada = marcaService.actualizarMarca(id, marcaUpdateDto);
        return ResponseEntity.ok(marcaActualizada);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar marca", description = "Elimina una marca del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Marca eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Marca no encontrada")
    })
    @PreAuthorize("hasRole('PROPIETARIO')")
    public ResponseEntity<Void> eliminarMarca(
            @Parameter(description = "ID de la marca") @PathVariable Long id) {
        marcaService.eliminarMarca(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/verificar-descripcion")
    @Operation(summary = "Verificar descripción", description = "Verifica si una descripción de marca ya existe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verificación completada")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<Boolean> verificarDescripcion(
            @Parameter(description = "Descripción a verificar") @RequestParam String descripcion) {
        boolean existe = marcaService.existeMarcaConDescripcion(descripcion);
        return ResponseEntity.ok(existe);
    }
}
