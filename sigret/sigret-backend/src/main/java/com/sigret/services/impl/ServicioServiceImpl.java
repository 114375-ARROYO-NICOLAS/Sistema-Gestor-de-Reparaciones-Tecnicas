package com.sigret.services.impl;

import com.sigret.dtos.detalleservicio.DetalleServicioDto;
import com.sigret.dtos.servicio.ItemEvaluacionGarantiaDto;
import com.sigret.dtos.servicio.ItemServicioOriginalDto;
import com.sigret.dtos.servicio.ServicioCreateDto;
import com.sigret.dtos.servicio.ServicioListDto;
import com.sigret.dtos.servicio.ServicioResponseDto;
import com.sigret.dtos.servicio.ServicioUpdateDto;
import com.sigret.entities.Cliente;
import com.sigret.entities.ClienteEquipo;
import com.sigret.entities.DetalleServicio;
import com.sigret.entities.Empleado;
import com.sigret.entities.Equipo;
import com.sigret.entities.OrdenTrabajo;
import com.sigret.entities.Repuesto;
import com.sigret.entities.Servicio;
import com.sigret.enums.EstadoOrdenTrabajo;
import com.sigret.enums.EstadoServicio;
import com.sigret.exception.ServicioNotFoundException;
import com.sigret.entities.Presupuesto;
import com.sigret.repositories.ClienteRepository;
import com.sigret.repositories.EmpleadoRepository;
import com.sigret.repositories.EquipoRepository;
import com.sigret.repositories.PresupuestoRepository;
import com.sigret.repositories.RepuestoRepository;
import com.sigret.repositories.ServicioRepository;
import com.sigret.services.ServicioService;
import com.sigret.services.WebSocketNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ServicioServiceImpl implements ServicioService {

    private static final Map<EstadoServicio, Set<EstadoServicio>> TRANSICIONES_VALIDAS = Map.ofEntries(
        Map.entry(EstadoServicio.RECIBIDO,                      Set.of(EstadoServicio.PRESUPUESTADO, EstadoServicio.RECHAZADO)),
        Map.entry(EstadoServicio.ESPERANDO_EVALUACION_GARANTIA, Set.of(EstadoServicio.EN_REPARACION, EstadoServicio.GARANTIA_SIN_REPARACION, EstadoServicio.GARANTIA_RECHAZADA)),
        Map.entry(EstadoServicio.PRESUPUESTADO,                 Set.of(EstadoServicio.APROBADO, EstadoServicio.RECHAZADO)),
        Map.entry(EstadoServicio.APROBADO,                      Set.of(EstadoServicio.EN_REPARACION)),
        Map.entry(EstadoServicio.EN_REPARACION,                 Set.of(EstadoServicio.TERMINADO, EstadoServicio.ESPERANDO_EVALUACION_GARANTIA)),
        Map.entry(EstadoServicio.TERMINADO,                     Set.of(EstadoServicio.FINALIZADO)),
        Map.entry(EstadoServicio.GARANTIA_SIN_REPARACION,       Set.of(EstadoServicio.FINALIZADO)),
        Map.entry(EstadoServicio.RECHAZADO,                     Set.of()),
        Map.entry(EstadoServicio.GARANTIA_RECHAZADA,            Set.of()),
        Map.entry(EstadoServicio.FINALIZADO,                    Set.of())
    );

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private PresupuestoRepository presupuestoRepository;

    @Autowired
    private RepuestoRepository repuestoRepository;

    @Autowired
    private WebSocketNotificationService notificationService;

    @Autowired
    private com.sigret.services.PresupuestoService presupuestoService;

    @Override
    public ServicioResponseDto crearServicio(ServicioCreateDto servicioCreateDto) {
        // Validar que el cliente existe
        Cliente cliente = clienteRepository.findById(servicioCreateDto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + servicioCreateDto.getClienteId()));

        // Validar que el equipo existe
        Equipo equipo = equipoRepository.findById(servicioCreateDto.getEquipoId())
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado con ID: " + servicioCreateDto.getEquipoId()));

        // Validar que el empleado existe
        Empleado empleadoRecepcion = empleadoRepository.findById(servicioCreateDto.getEmpleadoRecepcionId())
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + servicioCreateDto.getEmpleadoRecepcionId()));

        // Determinar si es garantía
        boolean esGarantia = servicioCreateDto.getEsGarantia() != null && servicioCreateDto.getEsGarantia();

        // Generar número de servicio automático (GTA para garantías, SRV para servicios normales)
        String numeroServicio = esGarantia ? generarNumeroGarantia() : generarNumeroServicio();

        // Crear el servicio
        Servicio servicio = new Servicio();
        servicio.setNumeroServicio(numeroServicio);
        servicio.setCliente(cliente);
        servicio.setEquipo(equipo);
        servicio.setEmpleadoRecepcion(empleadoRecepcion);
        servicio.setTipoIngreso(servicioCreateDto.getTipoIngreso());
        servicio.setFirmaIngreso(servicioCreateDto.getFirmaIngreso());
        servicio.setFirmaConformidad(servicioCreateDto.getFirmaConformidad());
        servicio.setFallaReportada(servicioCreateDto.getFallaReportada());
        servicio.setObservaciones(servicioCreateDto.getObservaciones());
        servicio.setEsGarantia(servicioCreateDto.getEsGarantia() != null ? servicioCreateDto.getEsGarantia() : false);
        servicio.setAbonaVisita(servicioCreateDto.getAbonaVisita() != null ? servicioCreateDto.getAbonaVisita() : false);
        servicio.setMontoVisita(servicioCreateDto.getMontoVisita() != null ? servicioCreateDto.getMontoVisita() : BigDecimal.ZERO);
        servicio.setMontoPagado(servicioCreateDto.getMontoPagado());
        servicio.setEstado(esGarantia ? EstadoServicio.ESPERANDO_EVALUACION_GARANTIA : EstadoServicio.RECIBIDO);
        servicio.setFechaCreacion(LocalDateTime.now());
        servicio.setFechaRecepcion(LocalDate.now());

        // Si es una garantía, establecer los campos adicionales de garantía
        if (esGarantia && servicioCreateDto.getServicioGarantiaId() != null) {
            Servicio servicioOriginal = servicioRepository.findById(servicioCreateDto.getServicioGarantiaId())
                    .orElseThrow(() -> new RuntimeException("Servicio original no encontrado con ID: " + servicioCreateDto.getServicioGarantiaId()));
            servicio.setServicioGarantia(servicioOriginal);

            // Calcular automáticamente si está dentro del plazo de garantía (90 días)
            LocalDate fechaDevolucion = servicioOriginal.getFechaDevolucionReal();
            if (fechaDevolucion == null) {
                throw new RuntimeException("El servicio original no tiene fecha de devolución real. No se puede calcular el plazo de garantía.");
            }
            boolean dentroPlazo = LocalDate.now().isBefore(fechaDevolucion.plusDays(90).plusDays(1));
            servicio.setGarantiaDentroPlazo(dentroPlazo);

            servicio.setGarantiaCumpleCondiciones(servicioCreateDto.getGarantiaCumpleCondiciones());
            servicio.setObservacionesGarantia(servicioCreateDto.getObservacionesGarantia());
            
            // Si se proporciona técnico de evaluación
            if (servicioCreateDto.getTecnicoEvaluacionId() != null) {
                Empleado tecnicoEvaluacion = empleadoRepository.findById(servicioCreateDto.getTecnicoEvaluacionId())
                        .orElseThrow(() -> new RuntimeException("Técnico de evaluación no encontrado con ID: " + servicioCreateDto.getTecnicoEvaluacionId()));
                servicio.setTecnicoEvaluacion(tecnicoEvaluacion);
                servicio.setFechaEvaluacionGarantia(LocalDateTime.now());
                servicio.setObservacionesEvaluacionGarantia(servicioCreateDto.getObservacionesEvaluacionGarantia());
            }
        }

        // Asociar equipo al cliente automáticamente si no existe la relación
        asociarEquipoACliente(cliente, equipo);

        Servicio servicioGuardado = servicioRepository.save(servicio);

        // Crear detalles del servicio si se proporcionan
        if (servicioCreateDto.getDetalles() != null && !servicioCreateDto.getDetalles().isEmpty()) {
            for (DetalleServicioDto detalleDto : servicioCreateDto.getDetalles()) {
                DetalleServicio detalle = new DetalleServicio();
                detalle.setServicio(servicioGuardado);
                detalle.setComponente(detalleDto.getComponente());
                detalle.setPresente(detalleDto.getPresente());
                detalle.setComentario(detalleDto.getComentario());
                servicioGuardado.getDetalleServicios().add(detalle);
            }
            servicioGuardado = servicioRepository.save(servicioGuardado);
        }

        // Notificar creación del servicio via WebSocket
        notificationService.notificarServicioCreado(convertirAServicioListDto(servicioGuardado));

        // CREAR AUTOMÁTICAMENTE EL PRESUPUESTO EN ESTADO PENDIENTE
        // No crear presupuesto para garantías, ya que son sin costo
        if (!esGarantia) {
            Presupuesto presupuesto = new Presupuesto();
            // Generar número de presupuesto automáticamente
            String numeroPresupuesto = presupuestoService.generarNumeroPresupuesto();
            presupuesto.setNumeroPresupuesto(numeroPresupuesto);
            presupuesto.setServicio(servicioGuardado);
            presupuesto.setEmpleado(empleadoRecepcion); // Asignar al mismo empleado que recibe el servicio
            // El diagnóstico se completa por el técnico cuando pasa a EN_CURSO
            presupuesto.setDiagnostico("");
            presupuesto.setManoObra(BigDecimal.ZERO);
            presupuesto.setMontoRepuestosOriginal(BigDecimal.ZERO);
            presupuesto.setEstado(com.sigret.enums.EstadoPresupuesto.PENDIENTE);
            presupuesto.setFechaCreacion(LocalDateTime.now());
            presupuesto.setFechaSolicitud(LocalDate.now());
            presupuestoRepository.save(presupuesto);
        }

        return convertirAServicioResponseDto(servicioGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public ServicioResponseDto obtenerServicioPorId(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ServicioNotFoundException("Servicio no encontrado con ID: " + id));

        return convertirAServicioResponseDto(servicio);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServicioListDto> obtenerServicios(Pageable pageable) {
        Page<Servicio> servicios = servicioRepository.findByActivoTrue(pageable);
        return servicios.map(this::convertirAServicioListDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioListDto> obtenerServiciosPorEstado(EstadoServicio estado) {
        List<Servicio> servicios = servicioRepository.findByEstadoAndActivoTrue(estado);
        return servicios.stream()
                .map(this::convertirAServicioListDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioListDto> obtenerServiciosPorCliente(Long clienteId) {
        List<Servicio> servicios = servicioRepository.findByClienteIdAndActivoTrue(clienteId);
        return servicios.stream()
                .map(this::convertirAServicioListDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioListDto> obtenerServiciosPorFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Servicio> servicios = servicioRepository.findByFechaRecepcionBetweenAndActivoTrue(fechaInicio, fechaFin);
        return servicios.stream()
                .map(this::convertirAServicioListDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ServicioResponseDto obtenerServicioPorNumero(String numeroServicio) {
        Servicio servicio = servicioRepository.findByNumeroServicio(numeroServicio)
                .orElseThrow(() -> new ServicioNotFoundException("Servicio no encontrado con número: " + numeroServicio));

        return convertirAServicioResponseDto(servicio);
    }

    @Override
    public ServicioResponseDto actualizarServicio(Long id, ServicioUpdateDto servicioUpdateDto) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ServicioNotFoundException("Servicio no encontrado con ID: " + id));

        // Actualizar campos si se proporcionan
        if (servicioUpdateDto.getTipoIngreso() != null) {
            servicio.setTipoIngreso(servicioUpdateDto.getTipoIngreso());
        }

        if (servicioUpdateDto.getFirmaIngreso() != null) {
            servicio.setFirmaIngreso(servicioUpdateDto.getFirmaIngreso());
        }

        if (servicioUpdateDto.getFirmaConformidad() != null) {
            servicio.setFirmaConformidad(servicioUpdateDto.getFirmaConformidad());
        }

        if (servicioUpdateDto.getEsGarantia() != null) {
            servicio.setEsGarantia(servicioUpdateDto.getEsGarantia());
        }

        if (servicioUpdateDto.getAbonaVisita() != null) {
            servicio.setAbonaVisita(servicioUpdateDto.getAbonaVisita());
        }

        if (servicioUpdateDto.getMontoVisita() != null) {
            servicio.setMontoVisita(servicioUpdateDto.getMontoVisita());
        }

        if (servicioUpdateDto.getMontoPagado() != null) {
            servicio.setMontoPagado(servicioUpdateDto.getMontoPagado());
        }

        if (servicioUpdateDto.getEstado() != null) {
            servicio.setEstado(servicioUpdateDto.getEstado());

            // Si el servicio cambia a TERMINADO y no tiene fechaDevolucionReal, establecerla automáticamente
            if (servicioUpdateDto.getEstado() == EstadoServicio.TERMINADO && servicio.getFechaDevolucionReal() == null) {
                servicio.setFechaDevolucionReal(LocalDate.now());
            }
        }

        if (servicioUpdateDto.getFechaDevolucionPrevista() != null) {
            servicio.setFechaDevolucionPrevista(servicioUpdateDto.getFechaDevolucionPrevista());
        }

        if (servicioUpdateDto.getFechaDevolucionReal() != null) {
            servicio.setFechaDevolucionReal(servicioUpdateDto.getFechaDevolucionReal());
        }

        // Actualizar campos de evaluación de garantía
        if (servicioUpdateDto.getTecnicoEvaluacionId() != null) {
            Empleado tecnicoEvaluacion = empleadoRepository.findById(servicioUpdateDto.getTecnicoEvaluacionId())
                    .orElseThrow(() -> new RuntimeException("Técnico de evaluación no encontrado con ID: " + servicioUpdateDto.getTecnicoEvaluacionId()));
            servicio.setTecnicoEvaluacion(tecnicoEvaluacion);
            servicio.setFechaEvaluacionGarantia(LocalDateTime.now());
        }

        if (servicioUpdateDto.getGarantiaCumpleCondiciones() != null) {
            servicio.setGarantiaCumpleCondiciones(servicioUpdateDto.getGarantiaCumpleCondiciones());
        }

        if (servicioUpdateDto.getObservacionesEvaluacionGarantia() != null) {
            String observaciones = servicioUpdateDto.getObservacionesEvaluacionGarantia();

            // Si hay items seleccionados en la evaluación, agregarlos a las observaciones
            if (servicioUpdateDto.getItemsEvaluacionGarantia() != null && !servicioUpdateDto.getItemsEvaluacionGarantia().isEmpty()) {
                observaciones += "\n\nItems identificados con falla:\n";
                for (ItemEvaluacionGarantiaDto item : servicioUpdateDto.getItemsEvaluacionGarantia()) {
                    Repuesto repuesto = repuestoRepository.findById(item.getRepuestoId())
                            .orElse(null);
                    if (repuesto != null) {
                        observaciones += "- " + repuesto.getDescripcionCompleta();
                        if (item.getComentario() != null && !item.getComentario().isEmpty()) {
                            observaciones += ": " + item.getComentario();
                        }
                        observaciones += "\n";
                    }
                }
            }

            servicio.setObservacionesEvaluacionGarantia(observaciones);
        }

        if (servicioUpdateDto.getGarantiaDentroPlazo() != null) {
            servicio.setGarantiaDentroPlazo(servicioUpdateDto.getGarantiaDentroPlazo());
        }

        if (servicioUpdateDto.getObservacionesGarantia() != null) {
            servicio.setObservacionesGarantia(servicioUpdateDto.getObservacionesGarantia());
        }

        Servicio servicioActualizado = servicioRepository.save(servicio);

        // Notificar actualización del servicio via WebSocket
        notificationService.notificarServicioActualizado(convertirAServicioListDto(servicioActualizado));

        return convertirAServicioResponseDto(servicioActualizado);
    }

    @Override
    public ServicioResponseDto cambiarEstadoServicio(Long id, EstadoServicio nuevoEstado) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ServicioNotFoundException("Servicio no encontrado con ID: " + id));

        EstadoServicio estadoAnterior = servicio.getEstado();

        Set<EstadoServicio> permitidos = TRANSICIONES_VALIDAS.getOrDefault(estadoAnterior, Set.of());
        if (!permitidos.contains(nuevoEstado)) {
            throw new IllegalStateException(
                String.format("Transición de estado inválida: %s → %s", estadoAnterior, nuevoEstado)
            );
        }

        servicio.setEstado(nuevoEstado);

        // Si el servicio cambia a TERMINADO y no tiene fechaDevolucionReal, establecerla automáticamente
        if (nuevoEstado == EstadoServicio.TERMINADO && servicio.getFechaDevolucionReal() == null) {
            servicio.setFechaDevolucionReal(LocalDate.now());
        }

        Servicio servicioActualizado = servicioRepository.save(servicio);

        // Notificar cambio de estado via WebSocket
        notificationService.notificarCambioEstado(convertirAServicioListDto(servicioActualizado), estadoAnterior);

        return convertirAServicioResponseDto(servicioActualizado);
    }

    @Override
    public void eliminarServicio(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ServicioNotFoundException("Servicio no encontrado con ID: " + id));
        servicio.setActivo(false);
        servicioRepository.save(servicio);

        // Notificar eliminación del servicio via WebSocket
        notificationService.notificarServicioEliminado(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServicioListDto> obtenerServiciosEliminados(Pageable pageable) {
        Page<Servicio> servicios = servicioRepository.findByActivoFalse(pageable);
        return servicios.map(this::convertirAServicioListDto);
    }

    @Override
    public ServicioResponseDto restaurarServicio(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ServicioNotFoundException("Servicio no encontrado con ID: " + id));

        if (servicio.getActivo()) {
            throw new RuntimeException("El servicio no está eliminado");
        }

        servicio.setActivo(true);
        Servicio servicioRestaurado = servicioRepository.save(servicio);

        // Notificar restauración via WebSocket
        notificationService.notificarServicioCreado(convertirAServicioListDto(servicioRestaurado));

        return convertirAServicioResponseDto(servicioRestaurado);
    }

    @Override
    public String generarNumeroServicio() {
        String year = String.valueOf(LocalDate.now().getYear()).substring(2); // Últimos 2 dígitos del año
        String pattern = "SRV" + year + "%";

        Integer maxNumero = servicioRepository.findMaxNumeroServicio(pattern);
        int siguienteNumero = (maxNumero != null ? maxNumero : 0) + 1;

        return String.format("SRV%s%05d", year, siguienteNumero);
    }

    private String generarNumeroGarantia() {
        String year = String.valueOf(LocalDate.now().getYear()).substring(2); // Últimos 2 dígitos del año
        String pattern = "GTA" + year + "%";

        Integer maxNumero = servicioRepository.findMaxNumeroServicio(pattern);
        int siguienteNumero = (maxNumero != null ? maxNumero : 0) + 1;

        return String.format("GTA%s%05d", year, siguienteNumero);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioListDto> obtenerServiciosGarantia() {
        List<Servicio> servicios = servicioRepository.findServiciosGarantiaActivos();
        return servicios.stream()
                .map(this::convertirAServicioListDto)
                .collect(Collectors.toList());
    }

    @Override
    public ServicioResponseDto crearServicioGarantia(Long servicioOriginalId, ServicioCreateDto servicioGarantiaDto) {
        // Validar que el servicio original existe
        Servicio servicioOriginal = servicioRepository.findById(servicioOriginalId)
                .orElseThrow(() -> new ServicioNotFoundException("Servicio original no encontrado con ID: " + servicioOriginalId));

        // Crear el servicio de garantía
        ServicioCreateDto garantiaDto = new ServicioCreateDto();
        garantiaDto.setClienteId(servicioOriginal.getCliente().getId());
        garantiaDto.setEquipoId(servicioOriginal.getEquipo().getId());
        garantiaDto.setEmpleadoRecepcionId(servicioGarantiaDto.getEmpleadoRecepcionId());
        garantiaDto.setTipoIngreso(servicioGarantiaDto.getTipoIngreso());
        garantiaDto.setEsGarantia(true);
        garantiaDto.setServicioGarantiaId(servicioOriginalId);
        garantiaDto.setGarantiaDentroPlazo(servicioGarantiaDto.getGarantiaDentroPlazo());
        garantiaDto.setGarantiaCumpleCondiciones(servicioGarantiaDto.getGarantiaCumpleCondiciones());
        garantiaDto.setObservacionesGarantia(servicioGarantiaDto.getObservacionesGarantia());
        garantiaDto.setTecnicoEvaluacionId(servicioGarantiaDto.getTecnicoEvaluacionId());
        garantiaDto.setObservacionesEvaluacionGarantia(servicioGarantiaDto.getObservacionesEvaluacionGarantia());

        return crearServicio(garantiaDto);
    }

    private void asociarEquipoACliente(Cliente cliente, Equipo equipo) {
        // Verificar si ya existe la relación
        boolean yaAsociado = cliente.getClienteEquipos().stream()
                .anyMatch(ce -> ce.getEquipo().getId().equals(equipo.getId()));

        if (!yaAsociado) {
            ClienteEquipo clienteEquipo = new ClienteEquipo();
            clienteEquipo.setCliente(cliente);
            clienteEquipo.setEquipo(equipo);
            clienteEquipo.setFechaAlta(LocalDate.now());
            // Guardar la relación (esto se manejará en el repositorio de ClienteEquipo)
        }
    }

    private ServicioResponseDto convertirAServicioResponseDto(Servicio servicio) {
        ServicioResponseDto dto = new ServicioResponseDto();
        dto.setId(servicio.getId());
        dto.setNumeroServicio(servicio.getNumeroServicio());
        dto.setClienteId(servicio.getCliente().getId());
        dto.setClienteNombre(servicio.getCliente().getNombreCompleto());
        dto.setClienteDocumento(servicio.getCliente().getDocumento());
        dto.setEquipoId(servicio.getEquipo().getId());
        dto.setEquipoDescripcion(servicio.getEquipo().getDescripcionCompleta());
        dto.setEquipoNumeroSerie(servicio.getEquipo().getNumeroSerie());
        dto.setEmpleadoRecepcionId(servicio.getEmpleadoRecepcion().getId());
        dto.setEmpleadoRecepcionNombre(servicio.getEmpleadoRecepcion().getNombreCompleto());
        dto.setTipoIngreso(servicio.getTipoIngreso());
        dto.setFirmaIngreso(servicio.getFirmaIngreso());
        dto.setFirmaConformidad(servicio.getFirmaConformidad());
        dto.setFallaReportada(servicio.getFallaReportada());
        dto.setObservaciones(servicio.getObservaciones());
        dto.setEsGarantia(servicio.getEsGarantia());
        dto.setServicioGarantiaId(servicio.getServicioGarantia() != null ? servicio.getServicioGarantia().getId() : null);
        dto.setServicioGarantiaNumero(servicio.getServicioGarantia() != null ? servicio.getServicioGarantia().getNumeroServicio() : null);
        dto.setGarantiaDentroPlazo(servicio.getGarantiaDentroPlazo());
        dto.setGarantiaCumpleCondiciones(servicio.getGarantiaCumpleCondiciones());
        dto.setObservacionesGarantia(servicio.getObservacionesGarantia());
        dto.setTecnicoEvaluacionId(servicio.getTecnicoEvaluacion() != null ? servicio.getTecnicoEvaluacion().getId() : null);
        dto.setTecnicoEvaluacionNombre(servicio.getTecnicoEvaluacion() != null ? servicio.getTecnicoEvaluacion().getNombreCompleto() : null);
        dto.setFechaEvaluacionGarantia(servicio.getFechaEvaluacionGarantia());
        dto.setObservacionesEvaluacionGarantia(servicio.getObservacionesEvaluacionGarantia());
        dto.setAbonaVisita(servicio.getAbonaVisita());
        dto.setMontoVisita(servicio.getMontoVisita());
        dto.setMontoPagado(servicio.getMontoPagado());
        dto.setEstado(servicio.getEstado());
        dto.setFechaCreacion(servicio.getFechaCreacion());
        dto.setFechaRecepcion(servicio.getFechaRecepcion());
        dto.setFechaDevolucionPrevista(servicio.getFechaDevolucionPrevista());
        dto.setFechaDevolucionReal(servicio.getFechaDevolucionReal());
        dto.setDescripcionCompleta(servicio.getDescripcionCompleta());
        dto.setActivo(servicio.getActivo());

        // Convertir detalles
        List<DetalleServicioDto> detallesDto = servicio.getDetalleServicios().stream()
                .map(detalle -> new DetalleServicioDto(
                        detalle.getId(),
                        detalle.getComponente(),
                        detalle.getPresente(),
                        detalle.getComentario()
                ))
                .collect(Collectors.toList());
        dto.setDetalles(detallesDto);

        return dto;
    }

    private ServicioListDto convertirAServicioListDto(Servicio servicio) {
        ServicioListDto dto = new ServicioListDto();
        dto.setId(servicio.getId());
        dto.setNumeroServicio(servicio.getNumeroServicio());
        dto.setClienteNombre(servicio.getCliente().getNombreCompleto());
        dto.setClienteDocumento(servicio.getCliente().getDocumento());
        dto.setEquipoDescripcion(servicio.getEquipo().getDescripcionCompleta());
        dto.setEquipoNumeroSerie(servicio.getEquipo().getNumeroSerie());
        dto.setEmpleadoRecepcionNombre(servicio.getEmpleadoRecepcion().getNombreCompleto());
        dto.setTipoIngreso(servicio.getTipoIngreso());
        dto.setFallaReportada(servicio.getFallaReportada());
        dto.setObservaciones(servicio.getObservaciones());
        dto.setEsGarantia(servicio.getEsGarantia());
        dto.setAbonaVisita(servicio.getAbonaVisita());
        dto.setMontoVisita(servicio.getMontoVisita());
        dto.setMontoPagado(servicio.getMontoPagado());
        dto.setEstado(servicio.getEstado());
        dto.setFechaCreacion(servicio.getFechaCreacion());
        dto.setFechaRecepcion(servicio.getFechaRecepcion());
        dto.setFechaDevolucionPrevista(servicio.getFechaDevolucionPrevista());
        dto.setFechaDevolucionReal(servicio.getFechaDevolucionReal());
        dto.setActivo(servicio.getActivo());

        // Técnico evaluador (para garantías)
        if (servicio.getTecnicoEvaluacion() != null) {
            dto.setTecnicoEvaluacionId(servicio.getTecnicoEvaluacion().getId());
            dto.setTecnicoEvaluacionNombre(servicio.getTecnicoEvaluacion().getNombreCompleto());
        }

        // Técnico asignado a la reparación (desde la orden de trabajo activa)
        if (!servicio.getOrdenesTrabajo().isEmpty()) {
            // Obtener la orden de trabajo más reciente o activa
            OrdenTrabajo ordenActiva = servicio.getOrdenesTrabajo().stream()
                    .filter(ot -> ot.getEstado() != EstadoOrdenTrabajo.TERMINADA && ot.getEstado() != EstadoOrdenTrabajo.CANCELADA)
                    .findFirst()
                    .orElse(servicio.getOrdenesTrabajo().get(servicio.getOrdenesTrabajo().size() - 1));

            if (ordenActiva != null && ordenActiva.getEmpleado() != null) {
                dto.setTecnicoAsignadoId(ordenActiva.getEmpleado().getId());
                dto.setTecnicoAsignadoNombre(ordenActiva.getEmpleado().getNombreCompleto());
            }
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemServicioOriginalDto> obtenerItemsServicioOriginal(Long servicioGarantiaId) {
        // Obtener el servicio de garantía
        Servicio servicioGarantia = servicioRepository.findById(servicioGarantiaId)
                .orElseThrow(() -> new ServicioNotFoundException("Servicio de garantía no encontrado con ID: " + servicioGarantiaId));

        // Verificar que es un servicio de garantía y tiene servicio original
        if (!servicioGarantia.getEsGarantia() || servicioGarantia.getServicioGarantia() == null) {
            throw new RuntimeException("El servicio no es una garantía o no tiene servicio original asociado");
        }

        Servicio servicioOriginal = servicioGarantia.getServicioGarantia();

        // Obtener la orden de trabajo del servicio original
        // Tomar la primera orden terminada, o la última si no hay terminada
        OrdenTrabajo ordenOriginal = servicioOriginal.getOrdenesTrabajo().stream()
                .filter(ot -> ot.getEstado() == EstadoOrdenTrabajo.TERMINADA)
                .findFirst()
                .orElse(servicioOriginal.getOrdenesTrabajo().isEmpty() ? null :
                        servicioOriginal.getOrdenesTrabajo().get(servicioOriginal.getOrdenesTrabajo().size() - 1));

        if (ordenOriginal == null) {
            return new ArrayList<>(); // No hay orden de trabajo en el servicio original
        }

        // Convertir los detalles de la orden de trabajo a DTOs
        return ordenOriginal.getDetalleOrdenesTrabajo().stream()
                .map(detalle -> new ItemServicioOriginalDto(
                        detalle.getRepuesto() != null ? detalle.getRepuesto().getId() : null,
                        detalle.getItemDisplay(),
                        detalle.getCantidad(),
                        detalle.getComentario()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public ServicioResponseDto finalizarServicio(Long id, String firmaConformidad) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ServicioNotFoundException("Servicio no encontrado con ID: " + id));

        if (servicio.getEstado() != EstadoServicio.TERMINADO) {
            throw new RuntimeException("Solo se pueden finalizar servicios en estado TERMINADO. Estado actual: " + servicio.getEstado());
        }

        if (firmaConformidad == null || firmaConformidad.isBlank()) {
            throw new RuntimeException("La firma de conformidad es obligatoria para finalizar el servicio");
        }

        EstadoServicio estadoAnterior = servicio.getEstado();
        servicio.setFirmaConformidad(firmaConformidad);
        servicio.setEstado(EstadoServicio.FINALIZADO);

        if (servicio.getFechaDevolucionReal() == null) {
            servicio.setFechaDevolucionReal(LocalDate.now());
        }

        Servicio servicioActualizado = servicioRepository.save(servicio);

        notificationService.notificarCambioEstado(convertirAServicioListDto(servicioActualizado), estadoAnterior);

        return convertirAServicioResponseDto(servicioActualizado);
    }
}
