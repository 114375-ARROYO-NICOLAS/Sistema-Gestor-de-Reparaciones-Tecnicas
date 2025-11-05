package com.sigret.services.impl;

import com.sigret.dtos.detalleservicio.DetalleServicioDto;
import com.sigret.dtos.servicio.ServicioCreateDto;
import com.sigret.dtos.servicio.ServicioListDto;
import com.sigret.dtos.servicio.ServicioResponseDto;
import com.sigret.dtos.servicio.ServicioUpdateDto;
import com.sigret.entities.Cliente;
import com.sigret.entities.ClienteEquipo;
import com.sigret.entities.DetalleServicio;
import com.sigret.entities.Empleado;
import com.sigret.entities.Equipo;
import com.sigret.entities.Servicio;
import com.sigret.enums.EstadoServicio;
import com.sigret.exception.ServicioNotFoundException;
import com.sigret.entities.Presupuesto;
import com.sigret.repositories.ClienteRepository;
import com.sigret.repositories.EmpleadoRepository;
import com.sigret.repositories.EquipoRepository;
import com.sigret.repositories.PresupuestoRepository;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ServicioServiceImpl implements ServicioService {

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
    private WebSocketNotificationService notificationService;

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

        // Generar número de servicio automático
        String numeroServicio = generarNumeroServicio();

        // Crear el servicio
        Servicio servicio = new Servicio();
        servicio.setNumeroServicio(numeroServicio);
        servicio.setCliente(cliente);
        servicio.setEquipo(equipo);
        servicio.setEmpleadoRecepcion(empleadoRecepcion);
        servicio.setTipoIngreso(servicioCreateDto.getTipoIngreso());
        servicio.setFirmaIngreso(servicioCreateDto.getFirmaIngreso());
        servicio.setFirmaConformidad(servicioCreateDto.getFirmaConformidad());
        servicio.setEsGarantia(servicioCreateDto.getEsGarantia() != null ? servicioCreateDto.getEsGarantia() : false);
        servicio.setAbonaVisita(servicioCreateDto.getAbonaVisita() != null ? servicioCreateDto.getAbonaVisita() : false);
        servicio.setMontoVisita(servicioCreateDto.getMontoVisita() != null ? servicioCreateDto.getMontoVisita() : BigDecimal.ZERO);
        servicio.setMontoPagado(servicioCreateDto.getMontoPagado());
        servicio.setEstado(EstadoServicio.RECIBIDO);
        servicio.setFechaCreacion(LocalDateTime.now());
        servicio.setFechaRecepcion(LocalDate.now());

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
        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setServicio(servicioGuardado);
        presupuesto.setEmpleado(empleadoRecepcion); // Asignar al mismo empleado que recibe el servicio
        presupuesto.setProblema(""); // Vacío inicialmente
        presupuesto.setDiagnostico(""); // Vacío inicialmente
        presupuesto.setEstado(com.sigret.enums.EstadoPresupuesto.PENDIENTE);
        presupuesto.setFechaCreacion(LocalDateTime.now());
        presupuesto.setFechaSolicitud(LocalDate.now());
        presupuestoRepository.save(presupuesto);

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
        Page<Servicio> servicios = servicioRepository.findAll(pageable);
        return servicios.map(this::convertirAServicioListDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioListDto> obtenerServiciosPorEstado(EstadoServicio estado) {
        List<Servicio> servicios = servicioRepository.findByEstado(estado);
        return servicios.stream()
                .map(this::convertirAServicioListDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioListDto> obtenerServiciosPorCliente(Long clienteId) {
        List<Servicio> servicios = servicioRepository.findByClienteId(clienteId);
        return servicios.stream()
                .map(this::convertirAServicioListDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioListDto> obtenerServiciosPorFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Servicio> servicios = servicioRepository.findByFechaRecepcionBetween(fechaInicio, fechaFin);
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
        }

        if (servicioUpdateDto.getFechaDevolucionPrevista() != null) {
            servicio.setFechaDevolucionPrevista(servicioUpdateDto.getFechaDevolucionPrevista());
        }

        if (servicioUpdateDto.getFechaDevolucionReal() != null) {
            servicio.setFechaDevolucionReal(servicioUpdateDto.getFechaDevolucionReal());
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
        servicio.setEstado(nuevoEstado);
        Servicio servicioActualizado = servicioRepository.save(servicio);

        // Notificar cambio de estado via WebSocket
        notificationService.notificarCambioEstado(convertirAServicioListDto(servicioActualizado), estadoAnterior);

        return convertirAServicioResponseDto(servicioActualizado);
    }

    @Override
    public void eliminarServicio(Long id) {
        if (!servicioRepository.existsById(id)) {
            throw new ServicioNotFoundException("Servicio no encontrado con ID: " + id);
        }
        servicioRepository.deleteById(id);

        // Notificar eliminación del servicio via WebSocket
        notificationService.notificarServicioEliminado(id);
    }

    @Override
    public String generarNumeroServicio() {
        String year = String.valueOf(LocalDate.now().getYear()).substring(2); // Últimos 2 dígitos del año
        String pattern = "SRV" + year + "%";
        
        Integer maxNumero = servicioRepository.findMaxNumeroServicio(pattern);
        int siguienteNumero = (maxNumero != null ? maxNumero : 0) + 1;
        
        return String.format("SRV%s%05d", year, siguienteNumero);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioListDto> obtenerServiciosGarantia() {
        List<Servicio> servicios = servicioRepository.findServiciosGarantia();
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
        return new ServicioListDto(
                servicio.getId(),
                servicio.getNumeroServicio(),
                servicio.getCliente().getNombreCompleto(),
                servicio.getCliente().getDocumento(),
                servicio.getEquipo().getDescripcionCompleta(),
                servicio.getEquipo().getNumeroSerie(),
                servicio.getEmpleadoRecepcion().getNombreCompleto(),
                servicio.getTipoIngreso(),
                servicio.getEsGarantia(),
                servicio.getAbonaVisita(),
                servicio.getMontoVisita(),
                servicio.getMontoPagado(),
                servicio.getEstado(),
                servicio.getFechaCreacion(),
                servicio.getFechaRecepcion(),
                servicio.getFechaDevolucionPrevista(),
                servicio.getFechaDevolucionReal()
        );
    }
}
