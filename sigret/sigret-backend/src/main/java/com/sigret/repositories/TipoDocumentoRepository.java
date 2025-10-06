package com.sigret.repositories;

import com.sigret.entities.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Long> {
    
    // Buscar por descripción
    Optional<TipoDocumento> findByDescripcionIgnoreCase(String descripcion);
    
    // Verificar si existe por descripción
    boolean existsByDescripcionIgnoreCase(String descripcion);
}
