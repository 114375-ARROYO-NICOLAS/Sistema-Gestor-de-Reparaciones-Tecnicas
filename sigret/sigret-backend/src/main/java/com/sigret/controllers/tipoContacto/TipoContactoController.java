package com.sigret.controllers.tipoContacto;

import com.sigret.dtos.tipoContacto.TipoContactoListDto;
import com.sigret.entities.TipoContacto;
import com.sigret.repositories.TipoContactoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tipos-contacto")
@Tag(name = "Tipos de Contacto", description = "Endpoints para consultar tipos de contacto (catálogo público)")
public class TipoContactoController {

    @Autowired
    private TipoContactoRepository tipoContactoRepository;

    @GetMapping
    @Operation(summary = "Listar tipos de contacto", description = "Obtiene una lista de todos los tipos de contacto disponibles (endpoint público)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tipos de contacto obtenida exitosamente")
    })
    public ResponseEntity<List<TipoContactoListDto>> obtenerTodosTiposContacto() {
        List<TipoContacto> tiposContacto = tipoContactoRepository.findAll();
        List<TipoContactoListDto> dtos = tiposContacto.stream()
                .map(tipo -> new TipoContactoListDto(tipo.getId(), tipo.getDescripcion()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de contacto por ID", description = "Obtiene un tipo de contacto específico por su ID (endpoint público)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de contacto encontrado"),
            @ApiResponse(responseCode = "404", description = "Tipo de contacto no encontrado")
    })
    public ResponseEntity<TipoContactoListDto> obtenerTipoContactoPorId(@PathVariable Long id) {
        return tipoContactoRepository.findById(id)
                .map(tipo -> new TipoContactoListDto(tipo.getId(), tipo.getDescripcion()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

