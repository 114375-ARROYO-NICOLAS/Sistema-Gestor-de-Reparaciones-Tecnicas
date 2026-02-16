package com.sigret.controllers.publico;

import com.sigret.dtos.presupuesto.DetallePresupuestoDto;
import com.sigret.dtos.presupuesto.PresupuestoPublicoDto;
import com.sigret.entities.DetallePresupuesto;
import com.sigret.entities.Presupuesto;
import com.sigret.entities.PresupuestoToken;
import com.sigret.services.PresupuestoService;
import com.sigret.services.PresupuestoTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sigret.enums.EstadoPresupuesto;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/presupuestos")
@CrossOrigin(origins = "*")
@Tag(name = "API Pública - Presupuestos", description = "Endpoints públicos para que los clientes visualicen y respondan presupuestos")
@RequiredArgsConstructor
public class PresupuestoPublicoController {

    private final PresupuestoTokenService tokenService;
    private final PresupuestoService presupuestoService;

    @GetMapping("/token/{token}")
    @Operation(summary = "Obtener presupuesto por token", description = "Obtiene los datos públicos de un presupuesto usando el token del email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Presupuesto encontrado"),
            @ApiResponse(responseCode = "400", description = "Token inválido o expirado"),
            @ApiResponse(responseCode = "404", description = "Presupuesto no encontrado")
    })
    public ResponseEntity<PresupuestoPublicoDto> obtenerPresupuestoPorToken(
            @Parameter(description = "Token del presupuesto") @PathVariable String token) {

        PresupuestoToken presupuestoToken = tokenService.validarToken(token);
        Presupuesto presupuesto = presupuestoToken.getPresupuesto();

        PresupuestoPublicoDto dto = convertirAPresupuestoPublicoDto(presupuesto);

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/aprobar/{token}")
    @Operation(summary = "Aprobar presupuesto", description = "Aprueba un presupuesto usando el token de aprobación")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Presupuesto aprobado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Token inválido, expirado o acción incorrecta"),
            @ApiResponse(responseCode = "404", description = "Presupuesto no encontrado")
    })
    public ResponseEntity<Map<String, String>> aprobarPresupuesto(
            @Parameter(description = "Token de aprobación") @PathVariable String token,
            HttpServletRequest request) {

        PresupuestoToken presupuestoToken = tokenService.validarToken(token);

        if (!"APROBAR".equals(presupuestoToken.getTipoAccion())) {
            throw new RuntimeException("Este token no es válido para aprobar el presupuesto");
        }

        Presupuesto presupuesto = presupuestoToken.getPresupuesto();

        // Validar que el presupuesto no haya sido ya respondido
        if (presupuesto.getEstado() == EstadoPresupuesto.APROBADO ||
                presupuesto.getEstado() == EstadoPresupuesto.RECHAZADO) {
            throw new RuntimeException("Este presupuesto ya fue respondido.");
        }

        // Validar que el presupuesto no esté vencido
        if (presupuesto.getEstado() == EstadoPresupuesto.VENCIDO ||
                (presupuesto.getFechaVencimiento() != null && presupuesto.getFechaVencimiento().isBefore(LocalDate.now()))) {
            throw new RuntimeException("Este presupuesto ha vencido. Por favor contacte al técnico.");
        }

        Long presupuestoId = presupuesto.getId();
        String tipoPrecio = presupuestoToken.getTipoPrecio(); // "ORIGINAL" o "ALTERNATIVO"
        String ip = obtenerIpCliente(request);

        presupuestoService.aprobarPresupuesto(presupuestoId, tipoPrecio);
        tokenService.marcarTokenComoUsado(token, ip);
        tokenService.invalidarTokensAnteriores(presupuestoId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Presupuesto aprobado exitosamente");
        response.put("numeroPresupuesto", presupuestoToken.getPresupuesto().getNumeroPresupuesto());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/rechazar/{token}")
    @Operation(summary = "Rechazar presupuesto", description = "Rechaza un presupuesto usando el token de rechazo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Presupuesto rechazado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Token inválido, expirado o acción incorrecta"),
            @ApiResponse(responseCode = "404", description = "Presupuesto no encontrado")
    })
    public ResponseEntity<Map<String, String>> rechazarPresupuesto(
            @Parameter(description = "Token de rechazo") @PathVariable String token,
            HttpServletRequest request) {

        PresupuestoToken presupuestoToken = tokenService.validarToken(token);

        if (!"RECHAZAR".equals(presupuestoToken.getTipoAccion())) {
            throw new RuntimeException("Este token no es válido para rechazar el presupuesto");
        }

        Presupuesto presupuesto = presupuestoToken.getPresupuesto();

        // Validar que el presupuesto no haya sido ya respondido
        if (presupuesto.getEstado() == EstadoPresupuesto.APROBADO ||
                presupuesto.getEstado() == EstadoPresupuesto.RECHAZADO) {
            throw new RuntimeException("Este presupuesto ya fue respondido.");
        }

        // Validar que el presupuesto no esté vencido
        if (presupuesto.getEstado() == EstadoPresupuesto.VENCIDO ||
                (presupuesto.getFechaVencimiento() != null && presupuesto.getFechaVencimiento().isBefore(LocalDate.now()))) {
            throw new RuntimeException("Este presupuesto ha vencido. Por favor contacte al técnico.");
        }

        Long presupuestoId = presupuesto.getId();
        String ip = obtenerIpCliente(request);

        presupuestoService.rechazarPresupuesto(presupuestoId);
        tokenService.marcarTokenComoUsado(token, ip);
        tokenService.invalidarTokensAnteriores(presupuestoId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Presupuesto rechazado exitosamente");
        response.put("numeroPresupuesto", presupuestoToken.getPresupuesto().getNumeroPresupuesto());

        return ResponseEntity.ok(response);
    }

    private PresupuestoPublicoDto convertirAPresupuestoPublicoDto(Presupuesto presupuesto) {
        PresupuestoPublicoDto dto = new PresupuestoPublicoDto();
        dto.setNumeroPresupuesto(presupuesto.getNumeroPresupuesto());
        dto.setNombreCliente(presupuesto.getServicio().getCliente().getNombreCompleto());
        dto.setEquipoDescripcion(presupuesto.getServicio().getEquipo().getDescripcionCompleta());
        dto.setFallaReportada(presupuesto.getServicio().getFallaReportada());
        dto.setDiagnostico(presupuesto.getDiagnostico());
        dto.setMontoTotalOriginal(presupuesto.getMontoTotalOriginal());
        dto.setMontoTotalAlternativo(presupuesto.getMontoTotalAlternativo());
        dto.setManoObra(presupuesto.getManoObra());
        dto.setMostrarOriginal(presupuesto.getMostrarOriginal());
        dto.setMostrarAlternativo(presupuesto.getMostrarAlternativo());
        dto.setEstado(presupuesto.getEstado().name());
        dto.setFechaCreacion(presupuesto.getFechaCreacion().toLocalDate());
        dto.setFechaVencimiento(presupuesto.getFechaVencimiento());
        dto.setVencido(presupuesto.getEstado() == EstadoPresupuesto.VENCIDO ||
                (presupuesto.getFechaVencimiento() != null && presupuesto.getFechaVencimiento().isBefore(LocalDate.now())));

        List<DetallePresupuestoDto> detallesDto = presupuesto.getDetallePresupuestos().stream()
                .map(this::convertirADetalleDto)
                .collect(Collectors.toList());
        dto.setDetalles(detallesDto);

        return dto;
    }

    private DetallePresupuestoDto convertirADetalleDto(DetallePresupuesto detalle) {
        DetallePresupuestoDto dto = new DetallePresupuestoDto();
        dto.setId(detalle.getId());
        dto.setItem(detalle.getItem());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioOriginal(detalle.getPrecioOriginal());
        dto.setPrecioAlternativo(detalle.getPrecioAlternativo());
        return dto;
    }

    private String obtenerIpCliente(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}