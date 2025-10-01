package com.sigret.services.impl;

import com.sigret.dtos.presupuesto.PresupuestoCreateDto;
import com.sigret.dtos.presupuesto.PresupuestoListDto;
import com.sigret.dtos.presupuesto.PresupuestoResponseDto;
import com.sigret.dtos.presupuesto.PresupuestoUpdateDto;
import com.sigret.entities.Presupuesto;
import com.sigret.entities.Servicio;
import com.sigret.enums.EstadoPresupuesto;
import com.sigret.exception.PresupuestoNotFoundException;
import com.sigret.repositories.PresupuestoRepository;
import com.sigret.repositories.ServicioRepository;
import com.sigret.services.PresupuestoService;
import org.springframework.beans.factory.annotation.Autowired;
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

        presupuesto.setEstado(nuevoEstado);
        Presupuesto presupuestoActualizado = presupuestoRepository.save(presupuesto);

        return convertirAPresupuestoResponseDto(presupuestoActualizado);
    }

    @Override
    public PresupuestoResponseDto aprobarPresupuesto(Long id) {
        return cambiarEstadoPresupuesto(id, EstadoPresupuesto.APROBADO);
    }

    @Override
    public PresupuestoResponseDto rechazarPresupuesto(Long id) {
        return cambiarEstadoPresupuesto(id, EstadoPresupuesto.RECHAZADO);
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
                presupuesto.getMontoTotalOriginal(),
                presupuesto.getFechaVencimiento(),
                presupuesto.getEstado(),
                presupuesto.getFechaCreacion()
        );
    }
}
