package com.sigret.repositories;

import com.sigret.entities.OrdenTrabajo;
import com.sigret.enums.EstadoOrdenTrabajo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrdenTrabajoRepository extends JpaRepository<OrdenTrabajo, Long> {

    List<OrdenTrabajo> findByEstado(EstadoOrdenTrabajo estado);

    List<OrdenTrabajo> findByEmpleadoId(Long empleadoId);

    List<OrdenTrabajo> findByServicioId(Long servicioId);

    @Query("SELECT ot FROM OrdenTrabajo ot WHERE ot.fechaComienzo BETWEEN :fechaInicio AND :fechaFin")
    List<OrdenTrabajo> findByFechaComienzoBetween(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

    @Query("SELECT ot FROM OrdenTrabajo ot WHERE ot.esSinCosto = true")
    List<OrdenTrabajo> findOrdenesTrabajoSinCosto();

    @Query("SELECT ot FROM OrdenTrabajo ot JOIN FETCH ot.servicio s JOIN FETCH ot.empleado e WHERE ot.id = :id")
    OrdenTrabajo findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT MAX(CAST(SUBSTRING(ot.numeroOrdenTrabajo, 8) AS int)) FROM OrdenTrabajo ot WHERE ot.numeroOrdenTrabajo LIKE :pattern")
    Integer findMaxNumeroOrdenTrabajo(@Param("pattern") String pattern);
}
