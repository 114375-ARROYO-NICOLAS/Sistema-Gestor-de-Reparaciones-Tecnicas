package com.sigret.repositories;

import com.sigret.entities.Presupuesto;
import com.sigret.enums.EstadoPresupuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PresupuestoRepository extends JpaRepository<Presupuesto, Long> {

    List<Presupuesto> findByServicioId(Long servicioId);

    List<Presupuesto> findByEstado(EstadoPresupuesto estado);

    @Query("SELECT p FROM Presupuesto p JOIN p.servicio s WHERE s.cliente.id = :clienteId")
    List<Presupuesto> findByClienteId(@Param("clienteId") Long clienteId);

    @Query("SELECT p FROM Presupuesto p WHERE p.fechaCreacion BETWEEN :fechaInicio AND :fechaFin")
    List<Presupuesto> findByFechaCreacionBetween(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

    @Query("SELECT MAX(CAST(SUBSTRING(p.numeroPresupuesto, 8) AS int)) FROM Presupuesto p WHERE p.numeroPresupuesto LIKE :pattern")
    Integer findMaxNumeroPresupuesto(@Param("pattern") String pattern);

    List<Presupuesto> findByEstadoAndFechaVencimientoBefore(EstadoPresupuesto estado, LocalDate fecha);

    // Dashboard queries
    long countByEstado(EstadoPresupuesto estado);

    @Query("SELECT COUNT(p) FROM Presupuesto p WHERE p.estado IN :estados")
    long countByEstadoIn(@Param("estados") List<EstadoPresupuesto> estados);

    // Dashboard queries con filtro de fechas
    @Query("SELECT COUNT(p) FROM Presupuesto p WHERE p.estado IN :estados AND p.fechaCreacion >= :desde AND p.fechaCreacion < :hasta")
    long countByEstadoInAndFechas(@Param("estados") List<EstadoPresupuesto> estados, @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query("SELECT COUNT(p) FROM Presupuesto p WHERE p.estado = :estado AND p.fechaCreacion >= :desde AND p.fechaCreacion < :hasta")
    long countByEstadoAndFechas(@Param("estado") EstadoPresupuesto estado, @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
