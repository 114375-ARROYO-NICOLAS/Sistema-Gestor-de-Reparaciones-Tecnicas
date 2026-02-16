package com.sigret.services;

import com.sigret.dtos.dashboard.DashboardEstadisticasDto;

import java.time.LocalDate;

public interface DashboardService {
    DashboardEstadisticasDto obtenerEstadisticas(LocalDate fechaDesde, LocalDate fechaHasta);
}
