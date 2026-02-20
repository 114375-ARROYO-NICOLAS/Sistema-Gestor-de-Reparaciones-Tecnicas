package com.sigret.controllers.dashboard;

import com.sigret.dtos.dashboard.DashboardEstadisticasDto;
import com.sigret.services.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Endpoints para estadísticas del dashboard")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/estadisticas")
    @Operation(summary = "Obtener estadísticas del dashboard",
               description = "Retorna todas las estadísticas agregadas para el dashboard principal")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO')")
    public ResponseEntity<DashboardEstadisticasDto> obtenerEstadisticas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        return ResponseEntity.ok(dashboardService.obtenerEstadisticas(fechaDesde, fechaHasta));
    }
}
