package com.sigret.controllers.repuesto;

import com.sigret.dtos.repuesto.RepuestoDto;
import com.sigret.entities.Repuesto;
import com.sigret.repositories.RepuestoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/repuestos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RepuestoController {

    private final RepuestoRepository repuestoRepository;

    @GetMapping("/buscar")
    public ResponseEntity<List<RepuestoDto>> buscar(@RequestParam(required = false) String query) {
        List<Repuesto> repuestos;

        if (query == null || query.trim().isEmpty()) {
            repuestos = repuestoRepository.findAll();
        } else {
            String searchQuery = query.toLowerCase();
            repuestos = repuestoRepository.findAll().stream()
                    .filter(r -> r.getDescripcion().toLowerCase().contains(searchQuery) ||
                            (r.getTipoEquipo() != null &&
                                    r.getTipoEquipo().getDescripcion().toLowerCase().contains(searchQuery)))
                    .collect(Collectors.toList());
        }

        List<RepuestoDto> dtos = repuestos.stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
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
