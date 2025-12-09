package com.sigret.services.impl;

import com.sigret.dtos.presupuesto.DetallePresupuestoDto;
import com.sigret.dtos.presupuesto.PresupuestoCreateDto;
import com.sigret.dtos.presupuesto.PresupuestoEventDto;
import com.sigret.dtos.presupuesto.PresupuestoListDto;
import com.sigret.dtos.presupuesto.PresupuestoResponseDto;
import com.sigret.dtos.presupuesto.PresupuestoUpdateDto;
import com.sigret.entities.DetallePresupuesto;
import com.sigret.entities.Empleado;
import com.sigret.entities.OrdenTrabajo;
import com.sigret.entities.Presupuesto;
import com.sigret.entities.Servicio;
import com.sigret.enums.EstadoPresupuesto;
import com.sigret.enums.EstadoServicio;
import com.sigret.exception.PresupuestoNotFoundException;
import com.sigret.exception.UsuarioSinEmpleadoException;
import com.sigret.entities.Usuario;
import com.sigret.repositories.EmpleadoRepository;
import com.sigret.repositories.OrdenTrabajoRepository;
import com.sigret.repositories.PresupuestoRepository;
import com.sigret.repositories.ServicioRepository;
import com.sigret.repositories.UsuarioRepository;
import com.sigret.services.PresupuestoService;
import com.sigret.services.WebSocketNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PresupuestoServiceImpl implements PresupuestoService {

    private static final Logger log = LoggerFactory.getLogger(PresupuestoServiceImpl.class);

    @Autowired
    private PresupuestoRepository presupuestoRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private OrdenTrabajoRepository ordenTrabajoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private WebSocketNotificationService notificationService;

    @Override
    public PresupuestoResponseDto crearPresupuesto(PresupuestoCreateDto presupuestoCreateDto) {
        log.info("Iniciando creación de presupuesto para servicio ID: {}", presupuestoCreateDto.getServicioId());
        
        // Validar que el servicio existe
        Servicio servicio = servicioRepository.findById(presupuestoCreateDto.getServicioId())
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + presupuestoCreateDto.getServicioId()));

        // Validar que el empleado existe
        Empleado empleado = empleadoRepository.findById(presupuestoCreateDto.getEmpleadoId())
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + presupuestoCreateDto.getEmpleadoId()));

        // Generar número de presupuesto automáticamente si no viene del DTO
        String numeroPresupuesto = presupuestoCreateDto.getNumeroPresupuesto();
        if (numeroPresupuesto == null || numeroPresupuesto.trim().isEmpty()) {
            numeroPresupuesto = generarNumeroPresupuesto();
        }
        
        if (numeroPresupuesto == null || numeroPresupuesto.trim().isEmpty()) {
            throw new RuntimeException("Error al generar el número de presupuesto");
        }
        
        // Crear el presupuesto
        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setNumeroPresupuesto(numeroPresupuesto);
        
        presupuesto.setServicio(servicio);
        presupuesto.setEmpleado(empleado);
        presupuesto.setDiagnostico(presupuestoCreateDto.getDiagnostico());
        
        // Configurar montos
        presupuesto.setManoObra(presupuestoCreateDto.getManoObra() != null ? presupuestoCreateDto.getManoObra() : BigDecimal.ZERO);
        presupuesto.setMontoRepuestosOriginal(presupuestoCreateDto.getMontoRepuestosOriginal() != null ? presupuestoCreateDto.getMontoRepuestosOriginal() : BigDecimal.ZERO);
        presupuesto.setMontoRepuestosAlternativo(presupuestoCreateDto.getMontoRepuestosAlternativo());
        presupuesto.setMostrarOriginal(presupuestoCreateDto.getMostrarOriginal() != null ? presupuestoCreateDto.getMostrarOriginal() : true);
        presupuesto.setMostrarAlternativo(presupuestoCreateDto.getMostrarAlternativo() != null ? presupuestoCreateDto.getMostrarAlternativo() : false);
        
        // Fechas
        presupuesto.setFechaVencimiento(presupuestoCreateDto.getFechaVencimiento());
        presupuesto.setFechaSolicitud(presupuestoCreateDto.getFechaSolicitud());
        presupuesto.setFechaPactada(presupuestoCreateDto.getFechaPactada());
        presupuesto.setEstado(presupuestoCreateDto.getEstado() != null ? presupuestoCreateDto.getEstado() : EstadoPresupuesto.PENDIENTE);
        presupuesto.setFechaCreacion(LocalDateTime.now());

        // Crear detalles de presupuesto
        if (presupuestoCreateDto.getDetalles() != null && !presupuestoCreateDto.getDetalles().isEmpty()) {
            for (DetallePresupuestoDto detalleDto : presupuestoCreateDto.getDetalles()) {
                DetallePresupuesto detalle = new DetallePresupuesto();
                detalle.setPresupuesto(presupuesto);
                detalle.setItem(detalleDto.getItem());
                detalle.setCantidad(detalleDto.getCantidad());
                detalle.setPrecioOriginal(detalleDto.getPrecioOriginal());
                detalle.setPrecioAlternativo(detalleDto.getPrecioAlternativo());
                
                presupuesto.getDetallePresupuestos().add(detalle);
            }
            
            // Calcular montos de repuestos automáticamente sumando los detalles
            BigDecimal montoOriginal = BigDecimal.ZERO;
            BigDecimal montoAlternativo = BigDecimal.ZERO;
            
            for (DetallePresupuesto detalle : presupuesto.getDetallePresupuestos()) {
                montoOriginal = montoOriginal.add(detalle.getSubtotalOriginal());
                if (detalle.getPrecioAlternativo() != null) {
                    montoAlternativo = montoAlternativo.add(detalle.getSubtotalAlternativo());
                }
            }
            
            presupuesto.setMontoRepuestosOriginal(montoOriginal);
            if (montoAlternativo.compareTo(BigDecimal.ZERO) > 0) {
                presupuesto.setMontoRepuestosAlternativo(montoAlternativo);
            }
        }
        
        // Calcular totales
        presupuesto.calcularTotales();

        // Verificar que el número de presupuesto no sea null antes de guardar
        log.error("DEBUG - Número de presupuesto antes de guardar: {}", presupuesto.getNumeroPresupuesto());
        if (presupuesto.getNumeroPresupuesto() == null || presupuesto.getNumeroPresupuesto().isEmpty()) {
            log.error("CRÍTICO: El número de presupuesto es null o vacío antes de guardar");
            throw new RuntimeException("El número de presupuesto es null o vacío antes de guardar");
        }

        // Guardar presupuesto (con cascade guardará los detalles)
        Presupuesto presupuestoGuardado = presupuestoRepository.save(presupuesto);

        // Cambiar estado del servicio a PRESUPUESTADO
        servicio.setEstado(EstadoServicio.PRESUPUESTADO);
        servicioRepository.save(servicio);

        // Notificar vía WebSocket
        PresupuestoEventDto evento = new PresupuestoEventDto();
        evento.setTipoEvento("CREADO");
        evento.setPresupuestoId(presupuestoGuardado.getId());
        evento.setServicioId(servicio.getId());
        evento.setNumeroServicio(servicio.getNumeroServicio());
        evento.setPresupuesto(convertirAPresupuestoListDto(presupuestoGuardado));
        notificationService.notificarPresupuesto(evento);

        return convertirAPresupuestoResponseDto(presupuestoGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public PresupuestoResponseDto obtenerPresupuestoPorId(Long id) {
        Presupuesto presupuesto = presupuestoRepository.findById(id)
                .orElseThrow(() -> new PresupuestoNotFoundException("Presupuesto no encontrado con ID: " + id));

        return convertirAPresupuestoResponseDto(presupuesto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PresupuestoListDto> obtenerPresupuestos(Pageable pageable) {
        Page<Presupuesto> presupuestos = presupuestoRepository.findAll(pageable);
        return presupuestos.map(this::convertirAPresupuestoListDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PresupuestoListDto> obtenerPresupuestosPorServicio(Long servicioId) {
        List<Presupuesto> presupuestos = presupuestoRepository.findByServicioId(servicioId);
        return presupuestos.stream()
                .map(this::convertirAPresupuestoListDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PresupuestoListDto> obtenerPresupuestosPorEstado(EstadoPresupuesto estado) {
        List<Presupuesto> presupuestos = presupuestoRepository.findByEstado(estado);
        return presupuestos.stream()
                .map(this::convertirAPresupuestoListDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PresupuestoListDto> obtenerPresupuestosPorCliente(Long clienteId) {
        List<Presupuesto> presupuestos = presupuestoRepository.findByClienteId(clienteId);
        return presupuestos.stream()
                .map(this::convertirAPresupuestoListDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PresupuestoListDto> obtenerPresupuestosPorFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Presupuesto> presupuestos = presupuestoRepository.findByFechaCreacionBetween(fechaInicio, fechaFin);
        return presupuestos.stream()
                .map(this::convertirAPresupuestoListDto)
                .collect(Collectors.toList());
    }

    @Override
    public PresupuestoResponseDto actualizarPresupuesto(Long id, PresupuestoUpdateDto presupuestoUpdateDto) {
        Presupuesto presupuesto = presupuestoRepository.findById(id)
                .orElseThrow(() -> new PresupuestoNotFoundException("Presupuesto no encontrado con ID: " + id));

        // Actualizar diagnóstico
        if (presupuestoUpdateDto.getDiagnostico() != null) {
            presupuesto.setDiagnostico(presupuestoUpdateDto.getDiagnostico());
        }

        // Actualizar detalles
        if (presupuestoUpdateDto.getDetalles() != null) {
            // Limpiar detalles existentes
            presupuesto.getDetallePresupuestos().clear();

            // Agregar nuevos detalles
            for (DetallePresupuestoDto detalleDto : presupuestoUpdateDto.getDetalles()) {
                DetallePresupuesto detalle = new DetallePresupuesto();
                detalle.setItem(detalleDto.getItem());
                detalle.setCantidad(detalleDto.getCantidad());
                detalle.setPrecioOriginal(detalleDto.getPrecioOriginal());
                detalle.setPrecioAlternativo(detalleDto.getPrecioAlternativo());
                detalle.setPresupuesto(presupuesto);
                presupuesto.getDetallePresupuestos().add(detalle);
            }
        }

        // Actualizar mano de obra
        if (presupuestoUpdateDto.getManoObra() != null) {
            presupuesto.setManoObra(presupuestoUpdateDto.getManoObra());
        }

        // Actualizar campos si se proporcionan
        if (presupuestoUpdateDto.getMontoTotal() != null) {
            presupuesto.setMontoTotalOriginal(presupuestoUpdateDto.getMontoTotal());
        }

        if (presupuestoUpdateDto.getFechaVencimiento() != null) {
            presupuesto.setFechaVencimiento(presupuestoUpdateDto.getFechaVencimiento());
        }

        if (presupuestoUpdateDto.getEstado() != null) {
            presupuesto.setEstado(presupuestoUpdateDto.getEstado());
        }

        if (presupuestoUpdateDto.getObservaciones() != null) {
            // Note: Presupuesto entity doesn't have observaciones field
        }

        // Recalcular montos
        presupuesto.recalcularMontos();

        Presupuesto presupuestoActualizado = presupuestoRepository.save(presupuesto);

        return convertirAPresupuestoResponseDto(presupuestoActualizado);
    }

    @Override
    public PresupuestoResponseDto cambiarEstadoPresupuesto(Long id, EstadoPresupuesto nuevoEstado) {
        Presupuesto presupuesto = presupuestoRepository.findById(id)
                .orElseThrow(() -> new PresupuestoNotFoundException("Presupuesto no encontrado con ID: " + id));

        EstadoPresupuesto estadoAnterior = presupuesto.getEstado();

        // Si cambia a EN_CURSO y no tiene empleado asignado, auto-asignar el usuario actual
        if (nuevoEstado == EstadoPresupuesto.EN_CURSO && presupuesto.getEmpleado() == null) {
            // Obtener el usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();

                // Buscar el usuario y su empleado asociado
                Usuario usuario = usuarioRepository.findByUsernameWithDetails(username)
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));

                if (usuario.getEmpleado() == null) {
                    throw new UsuarioSinEmpleadoException("El usuario '" + username + "' no tiene un empleado asociado. No se puede cambiar el presupuesto a estado EN_CURSO sin asignar un empleado.");
                }

                // Auto-asignar el empleado
                presupuesto.setEmpleado(usuario.getEmpleado());
            }
        }

        presupuesto.setEstado(nuevoEstado);
        Presupuesto presupuestoActualizado = presupuestoRepository.save(presupuesto);

        // Notificar cambio de estado via WebSocket
        PresupuestoEventDto evento = new PresupuestoEventDto();
        evento.setTipoEvento("CAMBIO_ESTADO");
        evento.setPresupuestoId(presupuesto.getId());
        evento.setServicioId(presupuesto.getServicio().getId());
        evento.setNumeroServicio(presupuesto.getServicio().getNumeroServicio());
        evento.setEstadoAnterior(estadoAnterior);
        evento.setEstadoNuevo(nuevoEstado);
        evento.setPresupuesto(convertirAPresupuestoListDto(presupuestoActualizado));
        notificationService.notificarPresupuesto(evento);

        return convertirAPresupuestoResponseDto(presupuestoActualizado);
    }

    @Override
    public PresupuestoResponseDto asignarEmpleado(Long presupuestoId, Long empleadoId) {
        // Validar presupuesto
        Presupuesto presupuesto = presupuestoRepository.findById(presupuestoId)
                .orElseThrow(() -> new PresupuestoNotFoundException("Presupuesto no encontrado con ID: " + presupuestoId));

        // Validar empleado
        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + empleadoId));

        // Asignar empleado
        presupuesto.setEmpleado(empleado);
        Presupuesto presupuestoActualizado = presupuestoRepository.save(presupuesto);

        // Notificar via WebSocket
        PresupuestoEventDto evento = new PresupuestoEventDto();
        evento.setTipoEvento("ACTUALIZADO");
        evento.setPresupuestoId(presupuesto.getId());
        evento.setServicioId(presupuesto.getServicio().getId());
        evento.setNumeroServicio(presupuesto.getServicio().getNumeroServicio());
        evento.setPresupuesto(convertirAPresupuestoListDto(presupuestoActualizado));
        notificationService.notificarPresupuesto(evento);

        return convertirAPresupuestoResponseDto(presupuestoActualizado);
    }

    @Override
    public PresupuestoResponseDto aprobarPresupuesto(Long id) {
        return aprobarPresupuesto(id, null);
    }

    @Override
    public PresupuestoResponseDto aprobarPresupuesto(Long id, String tipoPrecio) {
        // Obtener el presupuesto
        Presupuesto presupuesto = presupuestoRepository.findById(id)
                .orElseThrow(() -> new PresupuestoNotFoundException("Presupuesto no encontrado con ID: " + id));

        EstadoPresupuesto estadoAnterior = presupuesto.getEstado();

        // Cambiar estado del presupuesto
        presupuesto.setEstado(EstadoPresupuesto.APROBADO);

        // Guardar el tipo de precio confirmado y el canal
        if (tipoPrecio != null) {
            if ("ORIGINAL".equals(tipoPrecio)) {
                presupuesto.setTipoConfirmado(com.sigret.enums.TipoConfirmacion.ORIGINAL);
            } else if ("ALTERNATIVO".equals(tipoPrecio)) {
                presupuesto.setTipoConfirmado(com.sigret.enums.TipoConfirmacion.ALTERNATIVO);
            }
            presupuesto.setCanalConfirmacion(com.sigret.enums.CanalConfirmacion.EMAIL);
            presupuesto.setFechaConfirmacion(LocalDateTime.now());
        }

        Presupuesto presupuestoActualizado = presupuestoRepository.save(presupuesto);

        // Cambiar estado del servicio a APROBADO
        Servicio servicio = presupuesto.getServicio();
        servicio.setEstado(EstadoServicio.APROBADO);
        servicioRepository.save(servicio);

        // Ya NO se crea automáticamente la Orden de Trabajo
        // El propietario debe crearla manualmente desde el detalle del presupuesto

        // Notificar cambio de estado via WebSocket
        PresupuestoEventDto evento = new PresupuestoEventDto();
        evento.setTipoEvento("CAMBIO_ESTADO");
        evento.setPresupuestoId(presupuesto.getId());
        evento.setServicioId(servicio.getId());
        evento.setNumeroServicio(servicio.getNumeroServicio());
        evento.setEstadoAnterior(estadoAnterior);
        evento.setEstadoNuevo(EstadoPresupuesto.APROBADO);
        evento.setPresupuesto(convertirAPresupuestoListDto(presupuestoActualizado));
        notificationService.notificarPresupuesto(evento);

        return convertirAPresupuestoResponseDto(presupuestoActualizado);
    }

    @Override
    public PresupuestoResponseDto rechazarPresupuesto(Long id) {
        // Obtener el presupuesto
        Presupuesto presupuesto = presupuestoRepository.findById(id)
                .orElseThrow(() -> new PresupuestoNotFoundException("Presupuesto no encontrado con ID: " + id));

        EstadoPresupuesto estadoAnterior = presupuesto.getEstado();

        // Cambiar estado del presupuesto
        presupuesto.setEstado(EstadoPresupuesto.RECHAZADO);
        Presupuesto presupuestoActualizado = presupuestoRepository.save(presupuesto);

        // Cambiar estado del servicio a RECHAZADO
        Servicio servicio = presupuesto.getServicio();
        servicio.setEstado(EstadoServicio.RECHAZADO);
        servicioRepository.save(servicio);

        // Notificar cambio de estado via WebSocket
        PresupuestoEventDto evento = new PresupuestoEventDto();
        evento.setTipoEvento("CAMBIO_ESTADO");
        evento.setPresupuestoId(presupuesto.getId());
        evento.setServicioId(servicio.getId());
        evento.setNumeroServicio(servicio.getNumeroServicio());
        evento.setEstadoAnterior(estadoAnterior);
        evento.setEstadoNuevo(EstadoPresupuesto.RECHAZADO);
        evento.setPresupuesto(convertirAPresupuestoListDto(presupuestoActualizado));
        notificationService.notificarPresupuesto(evento);

        return convertirAPresupuestoResponseDto(presupuestoActualizado);
    }

    @Override
    @Transactional
    public Long crearOrdenDeTrabajo(Long presupuestoId) {
        // Validar que el presupuesto existe y está aprobado
        Presupuesto presupuesto = presupuestoRepository.findById(presupuestoId)
                .orElseThrow(() -> new PresupuestoNotFoundException("Presupuesto no encontrado con ID: " + presupuestoId));

        if (presupuesto.getEstado() != EstadoPresupuesto.APROBADO) {
            throw new RuntimeException("El presupuesto debe estar APROBADO para crear una orden de trabajo");
        }

        // Verificar si ya existe una orden de trabajo para este presupuesto
        if (!presupuesto.getOrdenesTrabajo().isEmpty()) {
            throw new RuntimeException("Ya existe una orden de trabajo para este presupuesto");
        }

        Servicio servicio = presupuesto.getServicio();

        // Crear la Orden de Trabajo
        OrdenTrabajo ordenTrabajo = new OrdenTrabajo();
        ordenTrabajo.setServicio(servicio);
        ordenTrabajo.setPresupuesto(presupuesto);
        ordenTrabajo.setEmpleado(presupuesto.getEmpleado()); // Mismo empleado del presupuesto
        ordenTrabajo.setEsSinCosto(servicio.getEsGarantia()); // Si es garantía, sin costo
        ordenTrabajo.setEstado(com.sigret.enums.EstadoOrdenTrabajo.PENDIENTE);
        ordenTrabajo.setFechaCreacion(LocalDateTime.now());
        OrdenTrabajo ordenGuardada = ordenTrabajoRepository.save(ordenTrabajo);

        return ordenGuardada.getId();
    }

    @Override
    public void eliminarPresupuesto(Long id) {
        if (!presupuestoRepository.existsById(id)) {
            throw new PresupuestoNotFoundException("Presupuesto no encontrado con ID: " + id);
        }
        presupuestoRepository.deleteById(id);
    }

    @Override
    public String generarNumeroPresupuesto() {
        String year = String.valueOf(LocalDate.now().getYear()).substring(2); // Últimos 2 dígitos del año
        String pattern = "PRE" + year + "%";
        
        log.error("DEBUG - Generando número de presupuesto con patrón: {}", pattern);

        Integer maxNumero = null;
        try {
            maxNumero = presupuestoRepository.findMaxNumeroPresupuesto(pattern);
            log.error("DEBUG - Máximo número encontrado: {}", maxNumero);
        } catch (Exception e) {
            log.error("ERROR al buscar máximo número: {}", e.getMessage());
            maxNumero = null;
        }
        
        int siguienteNumero = (maxNumero != null ? maxNumero : 0) + 1;
        
        String numeroGenerado = String.format("PRE%s%05d", year, siguienteNumero);
        log.error("DEBUG - Número generado: {}", numeroGenerado);

        return numeroGenerado;
    }

    private PresupuestoResponseDto convertirAPresupuestoResponseDto(Presupuesto presupuesto) {
        PresupuestoResponseDto dto = new PresupuestoResponseDto();
        dto.setId(presupuesto.getId());
        dto.setNumeroPresupuesto(presupuesto.getNumeroPresupuesto());
        dto.setServicioId(presupuesto.getServicio().getId());
        dto.setNumeroServicio(presupuesto.getServicio().getNumeroServicio());
        dto.setClienteNombre(presupuesto.getServicio().getCliente().getNombreCompleto());
        dto.setEquipoDescripcion(presupuesto.getServicio().getEquipo().getDescripcionCompleta());
        
        if (presupuesto.getEmpleado() != null) {
            dto.setEmpleadoId(presupuesto.getEmpleado().getId());
            dto.setEmpleadoNombre(presupuesto.getEmpleado().getNombreCompleto());
        }

        // El problema viene del servicio, no se guarda en presupuesto
        dto.setProblema(presupuesto.getServicio().getFallaReportada());
        dto.setDiagnostico(presupuesto.getDiagnostico());
        
        // Convertir detalles
        List<DetallePresupuestoDto> detallesDto = new ArrayList<>();
        for (DetallePresupuesto detalle : presupuesto.getDetallePresupuestos()) {
            DetallePresupuestoDto detalleDto = new DetallePresupuestoDto();
            detalleDto.setId(detalle.getId());
            detalleDto.setItem(detalle.getItem());
            detalleDto.setCantidad(detalle.getCantidad());
            detalleDto.setPrecioOriginal(detalle.getPrecioOriginal());
            detalleDto.setPrecioAlternativo(detalle.getPrecioAlternativo());
            detallesDto.add(detalleDto);
        }
        dto.setDetalles(detallesDto);
        
        dto.setManoObra(presupuesto.getManoObra());
        dto.setMontoRepuestosOriginal(presupuesto.getMontoRepuestosOriginal());
        dto.setMontoRepuestosAlternativo(presupuesto.getMontoRepuestosAlternativo());
        dto.setMontoTotalOriginal(presupuesto.getMontoTotalOriginal());
        dto.setMontoTotalAlternativo(presupuesto.getMontoTotalAlternativo());
        dto.setMostrarOriginal(presupuesto.getMostrarOriginal());
        dto.setMostrarAlternativo(presupuesto.getMostrarAlternativo());
        dto.setTipoConfirmado(presupuesto.getTipoConfirmado());
        dto.setFechaConfirmacion(presupuesto.getFechaConfirmacion());
        dto.setCanalConfirmacion(presupuesto.getCanalConfirmacion());
        dto.setFechaVencimiento(presupuesto.getFechaVencimiento());
        dto.setFechaSolicitud(presupuesto.getFechaSolicitud());
        dto.setFechaPactada(presupuesto.getFechaPactada());
        dto.setEstado(presupuesto.getEstado());
        dto.setFechaCreacion(presupuesto.getFechaCreacion());
        
        return dto;
    }

    private PresupuestoListDto convertirAPresupuestoListDto(Presupuesto presupuesto) {
        return new PresupuestoListDto(
                presupuesto.getId(),
                presupuesto.getNumeroPresupuesto(),
                presupuesto.getServicio().getNumeroServicio(),
                presupuesto.getServicio().getCliente().getNombreCompleto(),
                presupuesto.getEmpleado() != null ? presupuesto.getEmpleado().getNombreCompleto() : null,
                presupuesto.getServicio().getEquipo().getDescripcionCompleta(),
                presupuesto.getMontoTotalOriginal(),
                presupuesto.getFechaVencimiento(),
                presupuesto.getEstado(),
                presupuesto.getFechaCreacion()
        );
    }
}
