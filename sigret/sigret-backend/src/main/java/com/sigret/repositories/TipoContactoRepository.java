package com.sigret.repositories;

import com.sigret.entities.TipoContacto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoContactoRepository extends JpaRepository<TipoContacto, Long> {
    
    /**
     * Busca un tipo de contacto por su descripción
     */
    Optional<TipoContacto> findByDescripcion(String descripcion);
    
    /**
     * Verifica si existe un tipo de contacto con una descripción
     */
    boolean existsByDescripcion(String descripcion);
}

