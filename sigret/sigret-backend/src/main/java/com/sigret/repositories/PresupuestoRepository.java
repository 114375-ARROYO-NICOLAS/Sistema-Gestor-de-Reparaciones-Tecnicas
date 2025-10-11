package com.sigret.repositories;

import com.sigret.entities.Presupuesto;
import com.sigret.enums.EstadoPresupuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PresupuestoRepository extends JpaRepository<Presupuesto, Long> {

    // Note: Presupuesto entity doesn't have numeroPresupuesto field
    // Optional<Presupuesto> findByNumeroPresupuesto(String numeroPresupuesto);
    // boolean existsByNumeroPresupuesto(String numeroPresupuesto);

    List<Presupuesto> findByServicioId(Long servicioId);

    List<Presupuesto> findByEstado(EstadoPresupuesto estado);

    @Query("SELECT p FROM Presupuesto p JOIN p.servicio s WHERE s.cliente.id = :clienteId")
    List<Presupuesto> findByClienteId(@Param("clienteId") Long clienteId);

    @Query("SELECT p FROM Presupuesto p WHERE p.fechaCreacion BETWEEN :fechaInicio AND :fechaFin")
    List<Presupuesto> findByFechaCreacionBetween(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

    // Note: Presupuesto entity doesn't have numeroPresupuesto field
    // @Query("SELECT MAX(CAST(SUBSTRING(p.numeroPresupuesto, 5) AS UNSIGNED)) FROM Presupuesto p WHERE p.numeroPresupuesto LIKE :pattern")
    // Integer findMaxNumeroPresupuesto(@Param("pattern") String pattern);
}
