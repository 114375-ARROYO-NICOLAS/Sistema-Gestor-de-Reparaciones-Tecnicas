package com.sigret.repositories;

import com.sigret.entities.Servicio;
import com.sigret.enums.EstadoServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    Optional<Servicio> findByNumeroServicio(String numeroServicio);

    boolean existsByNumeroServicio(String numeroServicio);

    // Queries filtradas por activo = true (servicios no eliminados)
    Page<Servicio> findByActivoTrue(Pageable pageable);

    List<Servicio> findByEstadoAndActivoTrue(EstadoServicio estado);

    List<Servicio> findByClienteIdAndActivoTrue(Long clienteId);

    @Query("SELECT s FROM Servicio s WHERE s.fechaRecepcion BETWEEN :fechaInicio AND :fechaFin AND s.activo = true")
    List<Servicio> findByFechaRecepcionBetweenAndActivoTrue(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

    @Query("SELECT s FROM Servicio s WHERE s.esGarantia = true AND s.activo = true")
    List<Servicio> findServiciosGarantiaActivos();

    // Servicios eliminados (activo = false)
    Page<Servicio> findByActivoFalse(Pageable pageable);

    @Query("SELECT MAX(CAST(SUBSTRING(s.numeroServicio, 6) AS INTEGER)) FROM Servicio s WHERE s.numeroServicio LIKE :pattern")
    Integer findMaxNumeroServicio(@Param("pattern") String pattern);

    // Dashboard queries (solo servicios activos)
    @Query("SELECT COUNT(s) FROM Servicio s WHERE s.estado = :estado AND s.activo = true")
    long countByEstadoAndActivoTrue(@Param("estado") EstadoServicio estado);

    @Query("SELECT COUNT(s) FROM Servicio s WHERE s.estado IN :estados AND s.activo = true")
    long countByEstadoInAndActivoTrue(@Param("estados") List<EstadoServicio> estados);

    @Query("SELECT COUNT(s) FROM Servicio s WHERE s.estado = com.sigret.enums.EstadoServicio.TERMINADO AND s.fechaCreacion >= :inicioMes AND s.fechaCreacion < :finMes AND s.activo = true")
    long countTerminadosEnMes(@Param("inicioMes") LocalDateTime inicioMes, @Param("finMes") LocalDateTime finMes);

    @Query(value = "SELECT DATE_FORMAT(s.fecha_creacion, '%Y-%m') AS mes, COUNT(s.id_servicio) FROM servicios s WHERE s.fecha_creacion >= :desde AND s.activo = true GROUP BY mes ORDER BY mes", nativeQuery = true)
    List<Object[]> countServiciosPorMes(@Param("desde") LocalDateTime desde);

    // Dashboard queries con filtro de fechas (solo servicios activos)
    @Query("SELECT COUNT(s) FROM Servicio s WHERE s.estado IN :estados AND s.fechaCreacion >= :desde AND s.fechaCreacion < :hasta AND s.activo = true")
    long countByEstadoInAndFechas(@Param("estados") List<EstadoServicio> estados, @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query("SELECT COUNT(s) FROM Servicio s WHERE s.estado = :estado AND s.fechaCreacion >= :desde AND s.fechaCreacion < :hasta AND s.activo = true")
    long countByEstadoAndFechas(@Param("estado") EstadoServicio estado, @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query(value = "SELECT DATE_FORMAT(s.fecha_creacion, '%Y-%m') AS mes, COUNT(s.id_servicio) FROM servicios s WHERE s.fecha_creacion >= :desde AND s.fecha_creacion < :hasta AND s.activo = true GROUP BY mes ORDER BY mes", nativeQuery = true)
    List<Object[]> countServiciosPorMesEnRango(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query(value = "SELECT te.descripcion, COUNT(s.id_servicio) AS cantidad " +
            "FROM servicios s " +
            "JOIN equipos eq ON s.id_equipo = eq.id_equipo " +
            "JOIN tipos_equipo te ON eq.id_tipo_equipo = te.id_tipo_equipo " +
            "WHERE s.es_garantia = true " +
            "AND s.activo = true " +
            "AND (:desde IS NULL OR s.fecha_creacion >= :desde) " +
            "AND (:hasta IS NULL OR s.fecha_creacion < :hasta) " +
            "GROUP BY te.id_tipo_equipo, te.descripcion " +
            "ORDER BY cantidad DESC", nativeQuery = true)
    List<Object[]> countGarantiasPorTipoEquipo(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
