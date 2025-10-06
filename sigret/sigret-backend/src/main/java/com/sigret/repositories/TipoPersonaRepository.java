package com.sigret.repositories;

import com.sigret.entities.TipoPersona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoPersonaRepository extends JpaRepository<TipoPersona, Long> {
    
    // Buscar por descripción
    Optional<TipoPersona> findByDescripcionIgnoreCase(String descripcion);
    
    // Verificar si existe por descripción
    boolean existsByDescripcionIgnoreCase(String descripcion);
}
