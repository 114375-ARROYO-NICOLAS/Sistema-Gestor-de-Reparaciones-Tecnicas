package com.sigret.repositories;

import com.sigret.entities.Servicio;
import com.sigret.enums.EstadoServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    Optional<Servicio> findByNumeroServicio(String numeroServicio);

    boolean existsByNumeroServicio(String numeroServicio);

    List<Servicio> findByEstado(EstadoServicio estado);

    List<Servicio> findByClienteId(Long clienteId);

    @Query("SELECT s FROM Servicio s WHERE s.fechaRecepcion BETWEEN :fechaInicio AND :fechaFin")
    List<Servicio> findByFechaRecepcionBetween(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

    @Query("SELECT s FROM Servicio s WHERE s.esGarantia = true")
    List<Servicio> findServiciosGarantia();

    @Query("SELECT MAX(CAST(SUBSTRING(s.numeroServicio, 5) AS INTEGER)) FROM Servicio s WHERE s.numeroServicio LIKE :pattern")
    Integer findMaxNumeroServicio(@Param("pattern") String pattern);
}
