package com.sigret.controllers.servicio;

import com.sigret.dtos.servicio.ItemServicioOriginalDto;
import com.sigret.dtos.servicio.ServicioCreateDto;
import com.sigret.dtos.servicio.ServicioListDto;
import com.sigret.dtos.servicio.ServicioResponseDto;
import com.sigret.dtos.servicio.ServicioUpdateDto;
import com.sigret.enums.EstadoServicio;
import com.sigret.services.PdfService;
import com.sigret.services.ServicioService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/servicios")
@Tag(name = "Gestión de Servicios", description = "Endpoints para la gestión de servicios del sistema")
@SecurityRequirement(name = "bearerAuth")
public class ServicioController {

    @Autowired
    private ServicioService servicioService;

    @Autowired
    private PdfService pdfService;

    @GetMapping("/estados")
    @Operation(summary = "Obtener estados disponibles", description = "Retorna todos los estados posibles de un servicio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de estados obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<Map<String, String>>> obtenerEstados() {
        List<Map<String, String>> estados = Arrays.stream(EstadoServicio.values())
                .map(e -> Map.of("value", e.name(), "label", e.getDescripcion()))
                .toList();
        return ResponseEntity.ok(estados);
    }

    @PostMapping
    @Operation(summary = "Crear servicio", description = "Crea un nuevo servicio en el sistema con número automático")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Servicio creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<ServicioResponseDto> crearServicio(@Valid @RequestBody ServicioCreateDto servicioCreateDto) {
        ServicioResponseDto servicioCreado = servicioService.crearServicio(servicioCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioCreado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener servicio por ID", description = "Obtiene los detalles de un servicio específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicio encontrado"),
            @ApiResponse(responseCode = "404", description = "Servicio no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<ServicioResponseDto> obtenerServicioPorId(
            @Parameter(description = "ID del servicio") @PathVariable Long id) {
        ServicioResponseDto servicio = servicioService.obtenerServicioPorId(id);
        return ResponseEntity.ok(servicio);
    }

    @GetMapping
    @Operation(summary = "Listar servicios", description = "Obtiene una lista paginada de servicios")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de servicios obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<Page<ServicioListDto>> obtenerServicios(Pageable pageable) {
        Page<ServicioListDto> servicios = servicioService.obtenerServicios(pageable);
        return ResponseEntity.ok(servicios);
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Obtener servicios por estado", description = "Obtiene servicios filtrados por estado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de servicios obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<ServicioListDto>> obtenerServiciosPorEstado(
            @Parameter(description = "Estado del servicio") @PathVariable EstadoServicio estado) {
        List<ServicioListDto> servicios = servicioService.obtenerServiciosPorEstado(estado);
        return ResponseEntity.ok(servicios);
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Obtener servicios por cliente", description = "Obtiene servicios de un cliente específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de servicios obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<ServicioListDto>> obtenerServiciosPorCliente(
            @Parameter(description = "ID del cliente") @PathVariable Long clienteId) {
        List<ServicioListDto> servicios = servicioService.obtenerServiciosPorCliente(clienteId);
        return ResponseEntity.ok(servicios);
    }

    @GetMapping("/fechas")
    @Operation(summary = "Obtener servicios por fechas", description = "Obtiene servicios en un rango de fechas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de servicios obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<ServicioListDto>> obtenerServiciosPorFechas(
            @Parameter(description = "Fecha de inicio") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @Parameter(description = "Fecha de fin") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        List<ServicioListDto> servicios = servicioService.obtenerServiciosPorFechas(fechaInicio, fechaFin);
        return ResponseEntity.ok(servicios);
    }

    @GetMapping("/numero/{numeroServicio}")
    @Operation(summary = "Obtener servicio por número", description = "Obtiene un servicio por su número de servicio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicio encontrado"),
            @ApiResponse(responseCode = "404", description = "Servicio no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<ServicioResponseDto> obtenerServicioPorNumero(
            @Parameter(description = "Número de servicio") @PathVariable String numeroServicio) {
        ServicioResponseDto servicio = servicioService.obtenerServicioPorNumero(numeroServicio);
        return ResponseEntity.ok(servicio);
    }

    @GetMapping("/garantias")
    @Operation(summary = "Obtener servicios de garantía", description = "Obtiene servicios que son de garantía")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de servicios de garantía obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<ServicioListDto>> obtenerServiciosGarantia() {
        List<ServicioListDto> servicios = servicioService.obtenerServiciosGarantia();
        return ResponseEntity.ok(servicios);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar servicio", description = "Actualiza los datos de un servicio existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicio actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Servicio no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<ServicioResponseDto> actualizarServicio(
            @Parameter(description = "ID del servicio") @PathVariable Long id,
            @Valid @RequestBody ServicioUpdateDto servicioUpdateDto) {
        ServicioResponseDto servicioActualizado = servicioService.actualizarServicio(id, servicioUpdateDto);
        return ResponseEntity.ok(servicioActualizado);
    }

    @PatchMapping("/{id}/cambiar-estado")
    @Operation(summary = "Cambiar estado", description = "Cambia el estado de un servicio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado cambiado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Servicio no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<ServicioResponseDto> cambiarEstadoServicio(
            @Parameter(description = "ID del servicio") @PathVariable Long id,
            @Parameter(description = "Nuevo estado") @RequestParam EstadoServicio nuevoEstado) {
        ServicioResponseDto servicioActualizado = servicioService.cambiarEstadoServicio(id, nuevoEstado);
        return ResponseEntity.ok(servicioActualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar servicio", description = "Desactiva un servicio del sistema (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicio eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Servicio no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO')")
    public ResponseEntity<Void> eliminarServicio(
            @Parameter(description = "ID del servicio") @PathVariable Long id) {
        servicioService.eliminarServicio(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/eliminados")
    @Operation(summary = "Listar servicios eliminados", description = "Obtiene una lista paginada de servicios eliminados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de servicios eliminados obtenida exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO')")
    public ResponseEntity<Page<ServicioListDto>> obtenerServiciosEliminados(Pageable pageable) {
        Page<ServicioListDto> servicios = servicioService.obtenerServiciosEliminados(pageable);
        return ResponseEntity.ok(servicios);
    }

    @PatchMapping("/{id}/restaurar")
    @Operation(summary = "Restaurar servicio", description = "Restaura un servicio eliminado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicio restaurado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Servicio no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO')")
    public ResponseEntity<ServicioResponseDto> restaurarServicio(
            @Parameter(description = "ID del servicio") @PathVariable Long id) {
        ServicioResponseDto servicioRestaurado = servicioService.restaurarServicio(id);
        return ResponseEntity.ok(servicioRestaurado);
    }

    @GetMapping("/generar-numero")
    @Operation(summary = "Generar número de servicio", description = "Genera un número de servicio automático")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Número generado exitosamente")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<String> generarNumeroServicio() {
        String numero = servicioService.generarNumeroServicio();
        return ResponseEntity.ok(numero);
    }

    @PostMapping("/garantia/{servicioOriginalId}")
    @Operation(summary = "Crear servicio de garantía", description = "Crea un servicio de garantía basado en un servicio original")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Servicio de garantía creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Servicio original no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<ServicioResponseDto> crearServicioGarantia(
            @Parameter(description = "ID del servicio original") @PathVariable Long servicioOriginalId,
            @Valid @RequestBody ServicioCreateDto servicioGarantiaDto) {
        ServicioResponseDto servicioGarantiaCreado = servicioService.crearServicioGarantia(servicioOriginalId, servicioGarantiaDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioGarantiaCreado);
    }

    @GetMapping("/{id}/items-servicio-original")
    @Operation(summary = "Obtener items del servicio original", description = "Obtiene los repuestos usados en la reparación del servicio original (para evaluación de garantía)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Items obtenidos exitosamente"),
            @ApiResponse(responseCode = "404", description = "Servicio no encontrado o no es garantía")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<List<ItemServicioOriginalDto>> obtenerItemsServicioOriginal(
            @Parameter(description = "ID del servicio de garantía") @PathVariable Long id) {
        List<ItemServicioOriginalDto> items = servicioService.obtenerItemsServicioOriginal(id);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Descargar PDF del servicio", description = "Genera y descarga el PDF con la información del servicio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF generado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Servicio no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<byte[]> descargarPdfServicio(
            @Parameter(description = "ID del servicio") @PathVariable Long id) {
        byte[] pdfBytes = pdfService.generarPdfServicio(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "servicio-" + id + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @PostMapping("/{id}/pdf/enviar-email")
    @Operation(summary = "Enviar PDF por email", description = "Genera el PDF y lo envía por email al cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF enviado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Servicio no encontrado"),
            @ApiResponse(responseCode = "400", description = "Cliente no tiene email registrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<Void> enviarPdfPorEmail(
            @Parameter(description = "ID del servicio") @PathVariable Long id) {
        pdfService.enviarPdfPorEmail(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/finalizar")
    @Operation(summary = "Finalizar servicio", description = "Registra la firma de conformidad y cambia el estado a FINALIZADO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicio finalizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Servicio no encontrado"),
            @ApiResponse(responseCode = "400", description = "El servicio no está en estado TERMINADO o falta la firma")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<ServicioResponseDto> finalizarServicio(
            @Parameter(description = "ID del servicio") @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String firmaConformidad = body.get("firmaConformidad");
        ServicioResponseDto servicioFinalizado = servicioService.finalizarServicio(id, firmaConformidad);
        return ResponseEntity.ok(servicioFinalizado);
    }

    @GetMapping("/{id}/pdf-final")
    @Operation(summary = "Descargar PDF final del servicio", description = "Genera y descarga el PDF final con presupuesto, orden de trabajo y firma de conformidad")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF final generado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Servicio no encontrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO') or hasRole('TECNICO')")
    public ResponseEntity<byte[]> descargarPdfFinal(
            @Parameter(description = "ID del servicio") @PathVariable Long id) {
        byte[] pdfBytes = pdfService.generarPdfFinal(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "servicio-final-" + id + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @PostMapping("/{id}/pdf-final/enviar-email")
    @Operation(summary = "Enviar PDF final por email", description = "Genera el PDF final y lo envía por email al cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF final enviado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Servicio no encontrado"),
            @ApiResponse(responseCode = "400", description = "Cliente no tiene email registrado")
    })
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('ADMINISTRATIVO')")
    public ResponseEntity<Void> enviarPdfFinalPorEmail(
            @Parameter(description = "ID del servicio") @PathVariable Long id) {
        pdfService.enviarPdfFinalPorEmail(id);
        return ResponseEntity.ok().build();
    }
}
