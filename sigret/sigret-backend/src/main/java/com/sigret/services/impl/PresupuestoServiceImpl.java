package com.sigret.services.impl;

import com.sigret.dtos.presupuesto.PresupuestoCreateDto;
import com.sigret.dtos.presupuesto.PresupuestoEventDto;
import com.sigret.dtos.presupuesto.PresupuestoListDto;
import com.sigret.dtos.presupuesto.PresupuestoResponseDto;
import com.sigret.dtos.presupuesto.PresupuestoUpdateDto;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PresupuestoServiceImpl implements PresupuestoService {

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
        // Validar que el servicio existe
        Servicio servicio = servicioRepository.findById(presupuestoCreateDto.getServicioId())
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + presupuestoCreateDto.getServicioId()));

        // Generar número de presupuesto si no se proporciona
        String numeroPresupuesto = presupuestoCreateDto.getNumeroPresupuesto();
        if (numeroPresupuesto == null || numeroPresupuesto.trim().isEmpty()) {
            numeroPresupuesto = generarNumeroPresupuesto();
        }

        // Crear el presupuesto
        Presupuesto presupuesto = new Presupuesto();
        // Note: Presupuesto entity doesn't have numeroPresupuesto field
        presupuesto.setServicio(servicio);
        presupuesto.setMontoTotalOriginal(presupuestoCreateDto.getMontoTotal());
        presupuesto.setFechaVencimiento(presupuestoCreateDto.getFechaVencimiento());
        presupuesto.setEstado(presupuestoCreateDto.getEstado());
        // Note: Presupuesto entity doesn't have observaciones field
        presupuesto.setFechaCreacion(LocalDateTime.now());

        Presupuesto presupuestoGuardado = presupuestoRepository.save(presupuesto);

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
        // Obtener el presupuesto
        Presupuesto presupuesto = presupuestoRepository.findById(id)
                .orElseThrow(() -> new PresupuestoNotFoundException("Presupuesto no encontrado con ID: " + id));

        EstadoPresupuesto estadoAnterior = presupuesto.getEstado();

        // Cambiar estado del presupuesto
        presupuesto.setEstado(EstadoPresupuesto.APROBADO);
        Presupuesto presupuestoActualizado = presupuestoRepository.save(presupuesto);

        // Cambiar estado del servicio a APROBADO
        Servicio servicio = presupuesto.getServicio();
        servicio.setEstado(EstadoServicio.APROBADO);
        servicioRepository.save(servicio);

        // Crear automáticamente la Orden de Trabajo
        OrdenTrabajo ordenTrabajo = new OrdenTrabajo();
        ordenTrabajo.setServicio(servicio);
        ordenTrabajo.setPresupuesto(presupuestoActualizado);
        ordenTrabajo.setEmpleado(presupuesto.getEmpleado()); // Mismo empleado del presupuesto
        ordenTrabajo.setEsSinCosto(servicio.getEsGarantia()); // Si es garantía, sin costo
        ordenTrabajo.setEstado(com.sigret.enums.EstadoOrdenTrabajo.PENDIENTE);
        ordenTrabajo.setFechaCreacion(LocalDateTime.now());
        ordenTrabajoRepository.save(ordenTrabajo);

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
    public void eliminarPresupuesto(Long id) {
        if (!presupuestoRepository.existsById(id)) {
            throw new PresupuestoNotFoundException("Presupuesto no encontrado con ID: " + id);
        }
        presupuestoRepository.deleteById(id);
    }

    @Override
    public String generarNumeroPresupuesto() {
        String year = String.valueOf(LocalDate.now().getYear());
        // Since Presupuesto entity doesn't have numeroPresupuesto field, 
        // we'll use a simple timestamp-based approach
        long timestamp = System.currentTimeMillis();
        return String.format("PRES-%s-%d", year, timestamp % 100000);
    }

    private PresupuestoResponseDto convertirAPresupuestoResponseDto(Presupuesto presupuesto) {
        return new PresupuestoResponseDto(
                presupuesto.getId(),
                presupuesto.getId().toString(), // Using ID as presupuesto number
                presupuesto.getServicio().getId(),
                presupuesto.getServicio().getNumeroServicio(),
                presupuesto.getServicio().getCliente().getNombreCompleto(),
                presupuesto.getServicio().getEquipo().getDescripcionCompleta(),
                presupuesto.getMontoTotalOriginal(),
                presupuesto.getFechaVencimiento(),
                presupuesto.getEstado(),
                null, // Presupuesto entity doesn't have observaciones field
                presupuesto.getFechaCreacion()
        );
    }

    private PresupuestoListDto convertirAPresupuestoListDto(Presupuesto presupuesto) {
        return new PresupuestoListDto(
                presupuesto.getId(),
                presupuesto.getId().toString(), // Using ID as presupuesto number
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
