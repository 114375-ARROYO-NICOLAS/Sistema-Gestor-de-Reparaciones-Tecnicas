package com.sigret.repositories;

import com.sigret.entities.ClienteEquipo;
import com.sigret.entities.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteEquipoRepository extends JpaRepository<ClienteEquipo, Long> {

    /**
     * Obtener todos los equipos activos de un cliente
     */
    @Query("SELECT ce.equipo FROM ClienteEquipo ce WHERE ce.cliente.id = :clienteId AND ce.activo = true")
    List<Equipo> findEquiposByClienteId(@Param("clienteId") Long clienteId);

    /**
     * Verificar si un equipo está asociado a un cliente
     */
    @Query("SELECT CASE WHEN COUNT(ce) > 0 THEN true ELSE false END FROM ClienteEquipo ce " +
           "WHERE ce.cliente.id = :clienteId AND ce.equipo.id = :equipoId AND ce.activo = true")
    boolean existsByClienteIdAndEquipoId(@Param("clienteId") Long clienteId, @Param("equipoId") Long equipoId);

    /**
     * Obtener la relación cliente-equipo activa
     */
    @Query("SELECT ce FROM ClienteEquipo ce WHERE ce.cliente.id = :clienteId AND ce.equipo.id = :equipoId AND ce.activo = true")
    ClienteEquipo findByClienteIdAndEquipoIdAndActivoTrue(@Param("clienteId") Long clienteId, @Param("equipoId") Long equipoId);
}
