package com.sigret.repositories;

import com.sigret.entities.Empleado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    // Buscar empleados activos
    List<Empleado> findByActivoTrue();

    // Buscar empleados por estado (activo/inactivo)
    Page<Empleado> findByActivo(Boolean activo, Pageable pageable);

    // Buscar empleados con filtro por nombre completo o documento
    @Query("SELECT e FROM Empleado e " +
           "WHERE (:activo IS NULL OR e.activo = :activo) " +
           "AND (:busqueda IS NULL OR LOWER(e.persona.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
           "OR LOWER(e.persona.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
           "OR LOWER(e.persona.razonSocial) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
           "OR LOWER(e.persona.documento) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
    Page<Empleado> buscarEmpleadosConFiltros(@Param("activo") Boolean activo, 
                                               @Param("busqueda") String busqueda, 
                                               Pageable pageable);

    // Buscar empleados por tipo
    @Query("SELECT e FROM Empleado e WHERE e.tipoEmpleado.id = :tipoEmpleadoId")
    List<Empleado> findByTipoEmpleado(@Param("tipoEmpleadoId") Long tipoEmpleadoId);

    // Verificar si existe un empleado con el documento
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Empleado e WHERE e.persona.documento = :documento")
    boolean existsByDocumento(@Param("documento") String documento);
}
