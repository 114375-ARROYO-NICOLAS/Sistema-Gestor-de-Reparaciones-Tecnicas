package com.sigret.services.impl;

import com.sigret.dtos.ordenTrabajo.OrdenTrabajoCreateDto;
import com.sigret.dtos.ordenTrabajo.OrdenTrabajoEventDto;
import com.sigret.dtos.ordenTrabajo.OrdenTrabajoListDto;
import com.sigret.dtos.ordenTrabajo.OrdenTrabajoResponseDto;
import com.sigret.dtos.ordenTrabajo.OrdenTrabajoUpdateDto;
import com.sigret.entities.Empleado;
import com.sigret.entities.OrdenTrabajo;
import com.sigret.entities.Presupuesto;
import com.sigret.entities.Servicio;
import com.sigret.enums.EstadoOrdenTrabajo;
import com.sigret.enums.EstadoServicio;
import com.sigret.exception.OrdenTrabajoNotFoundException;
import com.sigret.repositories.EmpleadoRepository;
import com.sigret.repositories.OrdenTrabajoRepository;
import com.sigret.repositories.PresupuestoRepository;
import com.sigret.repositories.ServicioRepository;
import com.sigret.services.OrdenTrabajoService;
import com.sigret.services.WebSocketNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrdenTrabajoServiceImpl implements OrdenTrabajoService {

    @Autowired
    private OrdenTrabajoRepository ordenTrabajoRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private PresupuestoRepository presupuestoRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private WebSocketNotificationService notificationService;

    @Override
    public OrdenTrabajoResponseDto crearOrdenTrabajo(OrdenTrabajoCreateDto ordenTrabajoCreateDto) {
        // Validar que el servicio existe
        Servicio servicio = servicioRepository.findById(ordenTrabajoCreateDto.getServicioId())
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + ordenTrabajoCreateDto.getServicioId()));

        // Validar que el empleado existe
        Empleado empleado = empleadoRepository.findById(ordenTrabajoCreateDto.getEmpleadoId())
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + ordenTrabajoCreateDto.getEmpleadoId()));

        // Validar presupuesto si se proporciona
        Presupuesto presupuesto = null;
        if (ordenTrabajoCreateDto.getPresupuestoId() != null) {
            presupuesto = presupuestoRepository.findById(ordenTrabajoCreateDto.getPresupuestoId())
                    .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado con ID: " + ordenTrabajoCreateDto.getPresupuestoId()));
        }

        // Crear la orden de trabajo
        OrdenTrabajo ordenTrabajo = new OrdenTrabajo();
        ordenTrabajo.setServicio(servicio);
        ordenTrabajo.setPresupuesto(presupuesto);
        ordenTrabajo.setEmpleado(empleado);
        ordenTrabajo.setMontoTotalRepuestos(ordenTrabajoCreateDto.getMontoTotalRepuestos());
        ordenTrabajo.setMontoExtras(ordenTrabajoCreateDto.getMontoExtras());
        ordenTrabajo.setObservacionesExtras(ordenTrabajoCreateDto.getObservacionesExtras());
        ordenTrabajo.setEsSinCosto(ordenTrabajoCreateDto.getEsSinCosto());
        ordenTrabajo.setEstado(ordenTrabajoCreateDto.getEstado());

        OrdenTrabajo ordenTrabajoGuardada = ordenTrabajoRepository.save(ordenTrabajo);

        return convertirAOrdenTrabajoResponseDto(ordenTrabajoGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenTrabajoResponseDto obtenerOrdenTrabajoPorId(Long id) {
        OrdenTrabajo ordenTrabajo = ordenTrabajoRepository.findById(id)
                .orElseThrow(() -> new OrdenTrabajoNotFoundException("Orden de trabajo no encontrada con ID: " + id));

        return convertirAOrdenTrabajoResponseDto(ordenTrabajo);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrdenTrabajoListDto> obtenerOrdenesTrabajo(Pageable pageable) {
        Page<OrdenTrabajo> ordenesTrabajo = ordenTrabajoRepository.findAll(pageable);
        return ordenesTrabajo.map(this::convertirAOrdenTrabajoListDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenTrabajoListDto> obtenerOrdenesTrabajoPorEstado(EstadoOrdenTrabajo estado) {
        List<OrdenTrabajo> ordenesTrabajo = ordenTrabajoRepository.findByEstado(estado);
        return ordenesTrabajo.stream()
                .map(this::convertirAOrdenTrabajoListDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenTrabajoListDto> obtenerOrdenesTrabajoPorEmpleado(Long empleadoId) {
        List<OrdenTrabajo> ordenesTrabajo = ordenTrabajoRepository.findByEmpleadoId(empleadoId);
        return ordenesTrabajo.stream()
                .map(this::convertirAOrdenTrabajoListDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenTrabajoListDto> obtenerOrdenesTrabajoPorServicio(Long servicioId) {
        List<OrdenTrabajo> ordenesTrabajo = ordenTrabajoRepository.findByServicioId(servicioId);
        return ordenesTrabajo.stream()
                .map(this::convertirAOrdenTrabajoListDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenTrabajoListDto> obtenerOrdenesTrabajoPorFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        List<OrdenTrabajo> ordenesTrabajo = ordenTrabajoRepository.findByFechaComienzoBetween(fechaInicio, fechaFin);
        return ordenesTrabajo.stream()
                .map(this::convertirAOrdenTrabajoListDto)
                .collect(Collectors.toList());
    }

    @Override
    public OrdenTrabajoResponseDto actualizarOrdenTrabajo(Long id, OrdenTrabajoUpdateDto ordenTrabajoUpdateDto) {
        OrdenTrabajo ordenTrabajo = ordenTrabajoRepository.findById(id)
                .orElseThrow(() -> new OrdenTrabajoNotFoundException("Orden de trabajo no encontrada con ID: " + id));

        // Actualizar campos si se proporcionan
        if (ordenTrabajoUpdateDto.getPresupuestoId() != null) {
            Presupuesto presupuesto = presupuestoRepository.findById(ordenTrabajoUpdateDto.getPresupuestoId())
                    .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado con ID: " + ordenTrabajoUpdateDto.getPresupuestoId()));
            ordenTrabajo.setPresupuesto(presupuesto);
        }

        if (ordenTrabajoUpdateDto.getEmpleadoId() != null) {
            Empleado empleado = empleadoRepository.findById(ordenTrabajoUpdateDto.getEmpleadoId())
                    .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + ordenTrabajoUpdateDto.getEmpleadoId()));
            ordenTrabajo.setEmpleado(empleado);
        }

        if (ordenTrabajoUpdateDto.getMontoTotalRepuestos() != null) {
            ordenTrabajo.setMontoTotalRepuestos(ordenTrabajoUpdateDto.getMontoTotalRepuestos());
        }

        if (ordenTrabajoUpdateDto.getMontoExtras() != null) {
            ordenTrabajo.setMontoExtras(ordenTrabajoUpdateDto.getMontoExtras());
        }

        if (ordenTrabajoUpdateDto.getObservacionesExtras() != null) {
            ordenTrabajo.setObservacionesExtras(ordenTrabajoUpdateDto.getObservacionesExtras());
        }

        if (ordenTrabajoUpdateDto.getEsSinCosto() != null) {
            ordenTrabajo.setEsSinCosto(ordenTrabajoUpdateDto.getEsSinCosto());
        }

        if (ordenTrabajoUpdateDto.getEstado() != null) {
            ordenTrabajo.setEstado(ordenTrabajoUpdateDto.getEstado());
        }

        OrdenTrabajo ordenTrabajoActualizada = ordenTrabajoRepository.save(ordenTrabajo);

        return convertirAOrdenTrabajoResponseDto(ordenTrabajoActualizada);
    }

    @Override
    public OrdenTrabajoResponseDto cambiarEstadoOrdenTrabajo(Long id, EstadoOrdenTrabajo nuevoEstado) {
        OrdenTrabajo ordenTrabajo = ordenTrabajoRepository.findById(id)
                .orElseThrow(() -> new OrdenTrabajoNotFoundException("Orden de trabajo no encontrada con ID: " + id));

        ordenTrabajo.setEstado(nuevoEstado);
        OrdenTrabajo ordenTrabajoActualizada = ordenTrabajoRepository.save(ordenTrabajo);

        return convertirAOrdenTrabajoResponseDto(ordenTrabajoActualizada);
    }

    @Override
    public OrdenTrabajoResponseDto asignarEmpleado(Long ordenTrabajoId, Long empleadoId) {
        // Validar orden de trabajo
        OrdenTrabajo ordenTrabajo = ordenTrabajoRepository.findById(ordenTrabajoId)
                .orElseThrow(() -> new OrdenTrabajoNotFoundException("Orden de trabajo no encontrada con ID: " + ordenTrabajoId));

        // Validar empleado
        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + empleadoId));

        // Asignar empleado
        ordenTrabajo.setEmpleado(empleado);
        OrdenTrabajo ordenTrabajoActualizada = ordenTrabajoRepository.save(ordenTrabajo);

        // Notificar via WebSocket
        OrdenTrabajoEventDto evento = new OrdenTrabajoEventDto();
        evento.setTipoEvento("ACTUALIZADO");
        evento.setOrdenTrabajoId(ordenTrabajo.getId());
        evento.setServicioId(ordenTrabajo.getServicio().getId());
        evento.setNumeroServicio(ordenTrabajo.getServicio().getNumeroServicio());
        evento.setOrdenTrabajo(convertirAOrdenTrabajoListDto(ordenTrabajoActualizada));
        notificationService.notificarOrdenTrabajo(evento);

        return convertirAOrdenTrabajoResponseDto(ordenTrabajoActualizada);
    }

    @Override
    public OrdenTrabajoResponseDto iniciarOrdenTrabajo(Long id) {
        // Obtener la orden de trabajo
        OrdenTrabajo ordenTrabajo = ordenTrabajoRepository.findById(id)
                .orElseThrow(() -> new OrdenTrabajoNotFoundException("Orden de trabajo no encontrada con ID: " + id));

        EstadoOrdenTrabajo estadoAnterior = ordenTrabajo.getEstado();

        // Cambiar estado a EN_PROGRESO
        ordenTrabajo.setEstado(EstadoOrdenTrabajo.EN_PROGRESO);
        ordenTrabajo.setFechaComienzo(LocalDate.now());
        OrdenTrabajo ordenTrabajoActualizada = ordenTrabajoRepository.save(ordenTrabajo);

        // Cambiar estado del servicio a EN_REPARACION
        Servicio servicio = ordenTrabajo.getServicio();
        servicio.setEstado(EstadoServicio.EN_REPARACION);
        servicioRepository.save(servicio);

        // Notificar cambio de estado via WebSocket
        OrdenTrabajoEventDto evento = new OrdenTrabajoEventDto();
        evento.setTipoEvento("CAMBIO_ESTADO");
        evento.setOrdenTrabajoId(ordenTrabajo.getId());
        evento.setServicioId(servicio.getId());
        evento.setNumeroServicio(servicio.getNumeroServicio());
        evento.setEstadoAnterior(estadoAnterior);
        evento.setEstadoNuevo(EstadoOrdenTrabajo.EN_PROGRESO);
        evento.setOrdenTrabajo(convertirAOrdenTrabajoListDto(ordenTrabajoActualizada));
        notificationService.notificarOrdenTrabajo(evento);

        return convertirAOrdenTrabajoResponseDto(ordenTrabajoActualizada);
    }

    @Override
    public OrdenTrabajoResponseDto finalizarOrdenTrabajo(Long id) {
        // Obtener la orden de trabajo
        OrdenTrabajo ordenTrabajo = ordenTrabajoRepository.findById(id)
                .orElseThrow(() -> new OrdenTrabajoNotFoundException("Orden de trabajo no encontrada con ID: " + id));

        EstadoOrdenTrabajo estadoAnterior = ordenTrabajo.getEstado();

        // Cambiar estado a TERMINADA
        ordenTrabajo.setEstado(EstadoOrdenTrabajo.TERMINADA);
        ordenTrabajo.setFechaFin(LocalDate.now());
        OrdenTrabajo ordenTrabajoActualizada = ordenTrabajoRepository.save(ordenTrabajo);

        // Cambiar estado del servicio a TERMINADO
        Servicio servicio = ordenTrabajo.getServicio();
        servicio.setEstado(EstadoServicio.TERMINADO);
        servicioRepository.save(servicio);

        // Notificar cambio de estado via WebSocket
        OrdenTrabajoEventDto evento = new OrdenTrabajoEventDto();
        evento.setTipoEvento("CAMBIO_ESTADO");
        evento.setOrdenTrabajoId(ordenTrabajo.getId());
        evento.setServicioId(servicio.getId());
        evento.setNumeroServicio(servicio.getNumeroServicio());
        evento.setEstadoAnterior(estadoAnterior);
        evento.setEstadoNuevo(EstadoOrdenTrabajo.TERMINADA);
        evento.setOrdenTrabajo(convertirAOrdenTrabajoListDto(ordenTrabajoActualizada));
        notificationService.notificarOrdenTrabajo(evento);

        return convertirAOrdenTrabajoResponseDto(ordenTrabajoActualizada);
    }

    @Override
    public void eliminarOrdenTrabajo(Long id) {
        if (!ordenTrabajoRepository.existsById(id)) {
            throw new OrdenTrabajoNotFoundException("Orden de trabajo no encontrada con ID: " + id);
        }
        ordenTrabajoRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenTrabajoListDto> obtenerOrdenesTrabajoSinCosto() {
        List<OrdenTrabajo> ordenesTrabajo = ordenTrabajoRepository.findOrdenesTrabajoSinCosto();
        return ordenesTrabajo.stream()
                .map(this::convertirAOrdenTrabajoListDto)
                .collect(Collectors.toList());
    }

    private OrdenTrabajoResponseDto convertirAOrdenTrabajoResponseDto(OrdenTrabajo ordenTrabajo) {
        return new OrdenTrabajoResponseDto(
                ordenTrabajo.getId(),
                ordenTrabajo.getServicio().getId(),
                ordenTrabajo.getServicio().getNumeroServicio(),
                ordenTrabajo.getServicio().getCliente().getNombreCompleto(),
                ordenTrabajo.getServicio().getEquipo().getDescripcionCompleta(),
                ordenTrabajo.getPresupuesto() != null ? ordenTrabajo.getPresupuesto().getId() : null,
                ordenTrabajo.getPresupuesto() != null ? ordenTrabajo.getPresupuesto().getId().toString() : null,
                ordenTrabajo.getEmpleado().getId(),
                ordenTrabajo.getEmpleado().getNombreCompleto(),
                ordenTrabajo.getMontoTotalRepuestos(),
                ordenTrabajo.getMontoExtras(),
                ordenTrabajo.getObservacionesExtras(),
                ordenTrabajo.getEsSinCosto(),
                ordenTrabajo.getEstado(),
                ordenTrabajo.getFechaCreacion(),
                ordenTrabajo.getFechaComienzo(),
                ordenTrabajo.getFechaFin(),
                ordenTrabajo.getMontoTotalFinal(),
                ordenTrabajo.getDiasReparacion()
        );
    }

    private OrdenTrabajoListDto convertirAOrdenTrabajoListDto(OrdenTrabajo ordenTrabajo) {
        return new OrdenTrabajoListDto(
                ordenTrabajo.getId(),
                ordenTrabajo.getServicio().getNumeroServicio(),
                ordenTrabajo.getServicio().getCliente().getNombreCompleto(),
                ordenTrabajo.getServicio().getEquipo().getDescripcionCompleta(),
                ordenTrabajo.getEmpleado().getNombreCompleto(),
                ordenTrabajo.getEstado(),
                ordenTrabajo.getFechaCreacion(),
                ordenTrabajo.getFechaComienzo(),
                ordenTrabajo.getFechaFin(),
                ordenTrabajo.getMontoTotalFinal(),
                ordenTrabajo.getEsSinCosto()
        );
    }
}
