package com.sigret.services.impl;

import com.sigret.dtos.dashboard.DashboardEstadisticasDto;
import com.sigret.dtos.dashboard.DashboardEstadisticasDto.TendenciaMensualDto;
import com.sigret.enums.EstadoOrdenTrabajo;
import com.sigret.enums.EstadoPresupuesto;
import com.sigret.enums.EstadoServicio;
import com.sigret.repositories.OrdenTrabajoRepository;
import com.sigret.repositories.PresupuestoRepository;
import com.sigret.repositories.ServicioRepository;
import com.sigret.services.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private PresupuestoRepository presupuestoRepository;

    @Autowired
    private OrdenTrabajoRepository ordenTrabajoRepository;

    @Override
    public DashboardEstadisticasDto obtenerEstadisticas(LocalDate fechaDesde, LocalDate fechaHasta) {
        DashboardEstadisticasDto dto = new DashboardEstadisticasDto();

        boolean filtrarFechas = fechaDesde != null && fechaHasta != null;
        LocalDateTime desdeDateTime = filtrarFechas ? fechaDesde.atStartOfDay() : null;
        LocalDateTime hastaDateTime = filtrarFechas ? fechaHasta.plusDays(1).atStartOfDay() : null;

        // KPI 1: Servicios Activos
        List<EstadoServicio> estadosActivos = List.of(
                EstadoServicio.RECIBIDO,
                EstadoServicio.ESPERANDO_EVALUACION_GARANTIA,
                EstadoServicio.PRESUPUESTADO,
                EstadoServicio.APROBADO,
                EstadoServicio.EN_REPARACION
        );
        dto.setServiciosActivos(filtrarFechas
                ? servicioRepository.countByEstadoInAndFechas(estadosActivos, desdeDateTime, hastaDateTime)
                : servicioRepository.countByEstadoInAndActivoTrue(estadosActivos));

        // KPI 2: Presupuestos Pendientes
        List<EstadoPresupuesto> estadosPendientes = List.of(
                EstadoPresupuesto.PENDIENTE,
                EstadoPresupuesto.EN_CURSO,
                EstadoPresupuesto.LISTO
        );
        dto.setPresupuestosPendientes(filtrarFechas
                ? presupuestoRepository.countByEstadoInAndFechas(estadosPendientes, desdeDateTime, hastaDateTime)
                : presupuestoRepository.countByEstadoIn(estadosPendientes));

        // KPI 3: Ordenes en Progreso
        List<EstadoOrdenTrabajo> estadosOrdenes = List.of(
                EstadoOrdenTrabajo.PENDIENTE,
                EstadoOrdenTrabajo.EN_PROGRESO
        );
        dto.setOrdenesEnProgreso(filtrarFechas
                ? ordenTrabajoRepository.countByEstadoInAndFechas(estadosOrdenes, fechaDesde, fechaHasta)
                : ordenTrabajoRepository.countByEstadoIn(estadosOrdenes));

        // KPI 4: Tasa de Aprobacion de Presupuestos
        long aprobados = filtrarFechas
                ? presupuestoRepository.countByEstadoAndFechas(EstadoPresupuesto.APROBADO, desdeDateTime, hastaDateTime)
                : presupuestoRepository.countByEstado(EstadoPresupuesto.APROBADO);
        long rechazados = filtrarFechas
                ? presupuestoRepository.countByEstadoAndFechas(EstadoPresupuesto.RECHAZADO, desdeDateTime, hastaDateTime)
                : presupuestoRepository.countByEstado(EstadoPresupuesto.RECHAZADO);
        double tasa = (aprobados + rechazados) > 0
                ? (double) aprobados / (aprobados + rechazados) * 100.0
                : 0.0;
        dto.setTasaAprobacionPresupuestos(Math.round(tasa * 10.0) / 10.0);

        // KPI 5: Servicios completados (en el periodo o en el mes actual)
        if (filtrarFechas) {
            dto.setServiciosCompletadosMes(servicioRepository.countByEstadoAndFechas(
                    EstadoServicio.TERMINADO, desdeDateTime, hastaDateTime));
        } else {
            YearMonth currentMonth = YearMonth.now();
            LocalDateTime inicioMes = currentMonth.atDay(1).atStartOfDay();
            LocalDateTime finMes = currentMonth.plusMonths(1).atDay(1).atStartOfDay();
            dto.setServiciosCompletadosMes(servicioRepository.countTerminadosEnMes(inicioMes, finMes));
        }

        // Chart: Servicios por estado
        Map<String, Long> serviciosPorEstado = new LinkedHashMap<>();
        for (EstadoServicio estado : EstadoServicio.values()) {
            long count = filtrarFechas
                    ? servicioRepository.countByEstadoAndFechas(estado, desdeDateTime, hastaDateTime)
                    : servicioRepository.countByEstadoAndActivoTrue(estado);
            serviciosPorEstado.put(estado.name(), count);
        }
        dto.setServiciosPorEstado(serviciosPorEstado);

        // Chart: Presupuestos por estado
        Map<String, Long> presupuestosPorEstado = new LinkedHashMap<>();
        for (EstadoPresupuesto estado : EstadoPresupuesto.values()) {
            long count = filtrarFechas
                    ? presupuestoRepository.countByEstadoAndFechas(estado, desdeDateTime, hastaDateTime)
                    : presupuestoRepository.countByEstado(estado);
            presupuestosPorEstado.put(estado.name(), count);
        }
        dto.setPresupuestosPorEstado(presupuestosPorEstado);

        // Chart: Ordenes de trabajo por estado
        Map<String, Long> ordenesPorEstado = new LinkedHashMap<>();
        for (EstadoOrdenTrabajo estado : EstadoOrdenTrabajo.values()) {
            long count = filtrarFechas
                    ? ordenTrabajoRepository.countByEstadoAndFechas(estado, fechaDesde, fechaHasta)
                    : ordenTrabajoRepository.countByEstado(estado);
            ordenesPorEstado.put(estado.name(), count);
        }
        dto.setOrdenesTrabajoPorEstado(ordenesPorEstado);

        // Chart: Tendencia mensual
        buildTendenciaMensual(dto, filtrarFechas, desdeDateTime, hastaDateTime);

        // Chart: Ordenes por empleado
        List<Object[]> ordenesPorEmpleado = ordenTrabajoRepository.countOrdenesPorEmpleado(
                filtrarFechas ? fechaDesde : null,
                filtrarFechas ? fechaHasta : null);
        Map<String, Long> empleadoMap = new LinkedHashMap<>();
        for (Object[] row : ordenesPorEmpleado) {
            empleadoMap.put((String) row[0], ((Number) row[1]).longValue());
        }
        dto.setOrdenesPorEmpleado(empleadoMap);

        // Chart: Garantias por tipo de equipo
        List<Object[]> garantiasPorTipo = servicioRepository.countGarantiasPorTipoEquipo(
                filtrarFechas ? desdeDateTime : null,
                filtrarFechas ? hastaDateTime : null);
        Map<String, Long> garantiasMap = new LinkedHashMap<>();
        for (Object[] row : garantiasPorTipo) {
            garantiasMap.put((String) row[0], ((Number) row[1]).longValue());
        }
        dto.setGarantiasPorTipoEquipo(garantiasMap);

        return dto;
    }

    private void buildTendenciaMensual(DashboardEstadisticasDto dto, boolean filtrarFechas,
                                        LocalDateTime desdeDateTime, LocalDateTime hastaDateTime) {
        List<Object[]> resultados;
        if (filtrarFechas) {
            resultados = servicioRepository.countServiciosPorMesEnRango(desdeDateTime, hastaDateTime);
        } else {
            LocalDateTime seisAtras = LocalDateTime.now().minusMonths(6)
                    .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            resultados = servicioRepository.countServiciosPorMes(seisAtras);
        }

        Map<String, Long> resultadosMap = new LinkedHashMap<>();
        for (Object[] row : resultados) {
            resultadosMap.put((String) row[0], ((Number) row[1]).longValue());
        }

        // Determinar rango de meses a mostrar
        YearMonth mesInicio;
        YearMonth mesFin;
        if (filtrarFechas) {
            mesInicio = YearMonth.from(desdeDateTime.toLocalDate());
            mesFin = YearMonth.from(hastaDateTime.toLocalDate().minusDays(1));
        } else {
            mesInicio = YearMonth.now().minusMonths(5);
            mesFin = YearMonth.now();
        }

        List<TendenciaMensualDto> tendencia = new ArrayList<>();
        YearMonth current = mesInicio;
        while (!current.isAfter(mesFin)) {
            String mesKey = String.format("%d-%02d", current.getYear(), current.getMonthValue());
            String label = current.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
            label = label.substring(0, 1).toUpperCase() + label.substring(1);
            long cantidad = resultadosMap.getOrDefault(mesKey, 0L);
            tendencia.add(new TendenciaMensualDto(mesKey, label, cantidad));
            current = current.plusMonths(1);
        }
        dto.setTendenciaMensual(tendencia);
    }
}
