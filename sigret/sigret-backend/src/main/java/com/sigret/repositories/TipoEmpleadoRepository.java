package com.sigret.repositories;

import com.sigret.entities.TipoEmpleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoEmpleadoRepository extends JpaRepository<TipoEmpleado, Long> {
    
    // Buscar por descripción
    Optional<TipoEmpleado> findByDescripcionIgnoreCase(String descripcion);
    
    // Verificar si existe por descripción
    boolean existsByDescripcionIgnoreCase(String descripcion);
}
