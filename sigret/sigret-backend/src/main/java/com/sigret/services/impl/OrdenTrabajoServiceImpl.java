package com.sigret.services.impl;

import com.sigret.dtos.ordenTrabajo.OrdenTrabajoCreateDto;
import com.sigret.dtos.ordenTrabajo.OrdenTrabajoEventDto;
import com.sigret.dtos.ordenTrabajo.OrdenTrabajoListDto;
import com.sigret.dtos.ordenTrabajo.OrdenTrabajoResponseDto;
import com.sigret.dtos.ordenTrabajo.OrdenTrabajoUpdateDto;
import com.sigret.dtos.servicio.ItemEvaluacionGarantiaDto;
import com.sigret.entities.DetalleOrdenTrabajo;
import com.sigret.entities.Empleado;
import com.sigret.entities.OrdenTrabajo;
import com.sigret.entities.Presupuesto;
import com.sigret.entities.Repuesto;
import com.sigret.entities.Servicio;
import com.sigret.enums.EstadoOrdenTrabajo;
import com.sigret.enums.EstadoServicio;
import com.sigret.exception.OrdenTrabajoNotFoundException;
import com.sigret.repositories.EmpleadoRepository;
import com.sigret.repositories.OrdenTrabajoRepository;
import com.sigret.repositories.PresupuestoRepository;
import com.sigret.repositories.RepuestoRepository;
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
    private RepuestoRepository repuestoRepository;

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

        // Generar número de orden de trabajo automáticamente
        String numeroOrdenTrabajo = generarNumeroOrdenTrabajo();

        // Crear la orden de trabajo
        OrdenTrabajo ordenTrabajo = new OrdenTrabajo();
        ordenTrabajo.setNumeroOrdenTrabajo(numeroOrdenTrabajo);
        ordenTrabajo.setServicio(servicio);
        ordenTrabajo.setPresupuesto(presupuesto);
        ordenTrabajo.setEmpleado(empleado);
        ordenTrabajo.setMontoTotalRepuestos(ordenTrabajoCreateDto.getMontoTotalRepuestos());
        ordenTrabajo.setMontoExtras(ordenTrabajoCreateDto.getMontoExtras());
        ordenTrabajo.setObservacionesExtras(ordenTrabajoCreateDto.getObservacionesExtras());
        ordenTrabajo.setEsSinCosto(ordenTrabajoCreateDto.getEsSinCosto());
        ordenTrabajo.setEstado(ordenTrabajoCreateDto.getEstado());

        OrdenTrabajo ordenTrabajoGuardada = ordenTrabajoRepository.save(ordenTrabajo);

        // Copiar detalles del presupuesto si existe
        if (presupuesto != null && presupuesto.getDetallePresupuestos() != null) {
            for (var detallePresupuesto : presupuesto.getDetallePresupuestos()) {
                DetalleOrdenTrabajo detalle = new DetalleOrdenTrabajo();
                detalle.setOrdenTrabajo(ordenTrabajoGuardada);
                detalle.setItemDescripcion(detallePresupuesto.getItem());
                detalle.setCantidad(detallePresupuesto.getCantidad());
                detalle.setComentario(null); // Sin comentario inicial
                detalle.setCompletado(false);
                ordenTrabajoGuardada.getDetalleOrdenesTrabajo().add(detalle);
            }
            // Guardar nuevamente para persistir los detalles
            ordenTrabajoGuardada = ordenTrabajoRepository.save(ordenTrabajoGuardada);
        }

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
        OrdenTrabajoResponseDto dto = new OrdenTrabajoResponseDto();
        dto.setId(ordenTrabajo.getId());
        dto.setNumeroOrdenTrabajo(ordenTrabajo.getNumeroOrdenTrabajo());
        dto.setServicioId(ordenTrabajo.getServicio().getId());
        dto.setNumeroServicio(ordenTrabajo.getServicio().getNumeroServicio());
        dto.setClienteNombre(ordenTrabajo.getServicio().getCliente().getNombreCompleto());
        dto.setEquipoDescripcion(ordenTrabajo.getServicio().getEquipo().getDescripcionCompleta());
        dto.setPresupuestoId(ordenTrabajo.getPresupuesto() != null ? ordenTrabajo.getPresupuesto().getId() : null);
        dto.setNumeroPresupuesto(ordenTrabajo.getPresupuesto() != null ? ordenTrabajo.getPresupuesto().getId().toString() : null);
        dto.setEmpleadoId(ordenTrabajo.getEmpleado().getId());
        dto.setEmpleadoNombre(ordenTrabajo.getEmpleado().getNombreCompleto());
        dto.setMontoTotalRepuestos(ordenTrabajo.getMontoTotalRepuestos());
        dto.setMontoExtras(ordenTrabajo.getMontoExtras());
        dto.setObservacionesExtras(ordenTrabajo.getObservacionesExtras());
        dto.setEsSinCosto(ordenTrabajo.getEsSinCosto());
        dto.setEstado(ordenTrabajo.getEstado());
        dto.setFechaCreacion(ordenTrabajo.getFechaCreacion());
        dto.setFechaComienzo(ordenTrabajo.getFechaComienzo());
        dto.setFechaFin(ordenTrabajo.getFechaFin());
        dto.setMontoTotalFinal(ordenTrabajo.getMontoTotalFinal());
        dto.setDiasReparacion(ordenTrabajo.getDiasReparacion());

        // Convertir detalles
        if (ordenTrabajo.getDetalleOrdenesTrabajo() != null) {
            dto.setDetalles(ordenTrabajo.getDetalleOrdenesTrabajo().stream()
                    .map(this::convertirADetalleOrdenTrabajoDto)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private com.sigret.dtos.detalleOrdenTrabajo.DetalleOrdenTrabajoDto convertirADetalleOrdenTrabajoDto(DetalleOrdenTrabajo detalle) {
        return new com.sigret.dtos.detalleOrdenTrabajo.DetalleOrdenTrabajoDto(
                detalle.getId(),
                detalle.getItemDisplay(),
                detalle.getCantidad(),
                detalle.getComentario(),
                detalle.getCompletado()
        );
    }

    private OrdenTrabajoListDto convertirAOrdenTrabajoListDto(OrdenTrabajo ordenTrabajo) {
        return new OrdenTrabajoListDto(
                ordenTrabajo.getId(),
                ordenTrabajo.getNumeroOrdenTrabajo(),
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

    @Override
    public String generarNumeroOrdenTrabajo() {
        String year = String.valueOf(LocalDate.now().getYear()).substring(2); // Últimos 2 dígitos del año
        String pattern = "ODT" + year + "%";

        Integer maxNumero = ordenTrabajoRepository.findMaxNumeroOrdenTrabajo(pattern);
        int siguienteNumero = (maxNumero != null ? maxNumero : 0) + 1;

        return String.format("ODT%s%05d", year, siguienteNumero);
    }

    @Override
    public OrdenTrabajoResponseDto crearOrdenTrabajoGarantia(Long servicioId, Long empleadoId, String observaciones, List<ItemEvaluacionGarantiaDto> itemsEvaluacion) {
        // Validar que el servicio existe
        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + servicioId));

        // Validar que es un servicio de garantía
        if (!servicio.getEsGarantia()) {
            throw new RuntimeException("El servicio no es una garantía");
        }

        // Validar que el empleado existe
        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + empleadoId));

        // Generar número de orden de trabajo automáticamente
        String numeroOrdenTrabajo = generarNumeroOrdenTrabajo();

        // Crear la orden de trabajo SIN COSTO para garantía
        OrdenTrabajo ordenTrabajo = new OrdenTrabajo();
        ordenTrabajo.setNumeroOrdenTrabajo(numeroOrdenTrabajo);
        ordenTrabajo.setServicio(servicio);
        ordenTrabajo.setPresupuesto(null); // Sin presupuesto porque es garantía
        ordenTrabajo.setEmpleado(empleado);
        ordenTrabajo.setMontoTotalRepuestos(java.math.BigDecimal.ZERO);
        ordenTrabajo.setMontoExtras(java.math.BigDecimal.ZERO);
        ordenTrabajo.setObservacionesExtras(observaciones);
        ordenTrabajo.setEsSinCosto(true); // Marca como sin costo (garantía)
        ordenTrabajo.setEstado(EstadoOrdenTrabajo.PENDIENTE);

        OrdenTrabajo ordenTrabajoGuardada = ordenTrabajoRepository.save(ordenTrabajo);

        // Crear los DetalleOrdenTrabajo con los items seleccionados en la evaluación
        if (itemsEvaluacion != null && !itemsEvaluacion.isEmpty()) {
            for (ItemEvaluacionGarantiaDto item : itemsEvaluacion) {
                Repuesto repuesto = repuestoRepository.findById(item.getRepuestoId())
                        .orElseThrow(() -> new RuntimeException("Repuesto no encontrado con ID: " + item.getRepuestoId()));

                DetalleOrdenTrabajo detalle = new DetalleOrdenTrabajo();
                detalle.setOrdenTrabajo(ordenTrabajoGuardada);
                detalle.setRepuesto(repuesto);
                detalle.setCantidad(1); // Cantidad 1 para garantías
                detalle.setComentario(item.getComentario());

                ordenTrabajoGuardada.getDetalleOrdenesTrabajo().add(detalle);
            }

            // Guardar nuevamente para persistir los detalles
            ordenTrabajoGuardada = ordenTrabajoRepository.save(ordenTrabajoGuardada);
        }

        // Notificar via WebSocket
        OrdenTrabajoEventDto evento = new OrdenTrabajoEventDto();
        evento.setTipoEvento("CREADO");
        evento.setOrdenTrabajoId(ordenTrabajoGuardada.getId());
        evento.setServicioId(servicio.getId());
        evento.setNumeroServicio(servicio.getNumeroServicio());
        evento.setOrdenTrabajo(convertirAOrdenTrabajoListDto(ordenTrabajoGuardada));
        notificationService.notificarOrdenTrabajo(evento);

        return convertirAOrdenTrabajoResponseDto(ordenTrabajoGuardada);
    }

    @Override
    public OrdenTrabajoResponseDto actualizarDetalleOrdenTrabajo(Long ordenTrabajoId, Long detalleId, String comentario, Boolean completado) {
        // Obtener la orden de trabajo
        OrdenTrabajo ordenTrabajo = ordenTrabajoRepository.findById(ordenTrabajoId)
                .orElseThrow(() -> new OrdenTrabajoNotFoundException("Orden de trabajo no encontrada con ID: " + ordenTrabajoId));

        // Buscar el detalle específico
        DetalleOrdenTrabajo detalle = ordenTrabajo.getDetalleOrdenesTrabajo().stream()
                .filter(d -> d.getId().equals(detalleId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Detalle no encontrado con ID: " + detalleId));

        // Actualizar campos
        if (comentario != null) {
            detalle.setComentario(comentario);
        }
        if (completado != null) {
            detalle.setCompletado(completado);
        }

        // Guardar cambios
        ordenTrabajoRepository.save(ordenTrabajo);

        // Notificar vía WebSocket
        OrdenTrabajoEventDto evento = new OrdenTrabajoEventDto();
        evento.setTipoEvento("ACTUALIZADO");
        evento.setOrdenTrabajoId(ordenTrabajo.getId());
        evento.setServicioId(ordenTrabajo.getServicio().getId());
        evento.setNumeroServicio(ordenTrabajo.getServicio().getNumeroServicio());
        evento.setOrdenTrabajo(convertirAOrdenTrabajoListDto(ordenTrabajo));
        notificationService.notificarOrdenTrabajo(evento);

        return convertirAOrdenTrabajoResponseDto(ordenTrabajo);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean todosLosDetallesCompletados(Long ordenTrabajoId) {
        OrdenTrabajo ordenTrabajo = ordenTrabajoRepository.findById(ordenTrabajoId)
                .orElseThrow(() -> new OrdenTrabajoNotFoundException("Orden de trabajo no encontrada con ID: " + ordenTrabajoId));

        // Si no tiene detalles, retornar true
        if (ordenTrabajo.getDetalleOrdenesTrabajo() == null || ordenTrabajo.getDetalleOrdenesTrabajo().isEmpty()) {
            return true;
        }

        // Verificar que todos los detalles estén completados
        return ordenTrabajo.getDetalleOrdenesTrabajo().stream()
                .allMatch(DetalleOrdenTrabajo::getCompletado);
    }
}
