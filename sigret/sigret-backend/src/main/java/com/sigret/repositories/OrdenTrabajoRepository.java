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

    // Dashboard queries
    long countByEstado(EstadoOrdenTrabajo estado);

    @Query("SELECT COUNT(ot) FROM OrdenTrabajo ot WHERE ot.estado IN :estados")
    long countByEstadoIn(@Param("estados") List<EstadoOrdenTrabajo> estados);

    // Dashboard queries con filtro de fechas
    @Query("SELECT COUNT(ot) FROM OrdenTrabajo ot WHERE ot.estado IN :estados AND ot.fechaComienzo >= :desde AND ot.fechaComienzo <= :hasta")
    long countByEstadoInAndFechas(@Param("estados") List<EstadoOrdenTrabajo> estados, @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    @Query("SELECT COUNT(ot) FROM OrdenTrabajo ot WHERE ot.estado = :estado AND ot.fechaComienzo >= :desde AND ot.fechaComienzo <= :hasta")
    long countByEstadoAndFechas(@Param("estado") EstadoOrdenTrabajo estado, @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    @Query(value = "SELECT CONCAT(p.nombre, ' ', p.apellido) AS empleado, COUNT(ot.id_orden_trabajo) AS cantidad " +
            "FROM ordenes_trabajo ot " +
            "JOIN empleados e ON ot.id_empleado = e.id_empleado " +
            "JOIN personas p ON e.id_persona = p.id_persona " +
            "WHERE (:desde IS NULL OR ot.fecha_comienzo >= :desde) " +
            "AND (:hasta IS NULL OR ot.fecha_comienzo <= :hasta) " +
            "GROUP BY e.id_empleado, p.nombre, p.apellido " +
            "ORDER BY cantidad DESC", nativeQuery = true)
    List<Object[]> countOrdenesPorEmpleado(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
}
