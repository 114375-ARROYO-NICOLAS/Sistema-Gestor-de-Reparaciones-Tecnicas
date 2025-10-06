package com.sigret.repositories;

import com.sigret.entities.Direccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DireccionRepository extends JpaRepository<Direccion, Long> {
    
    /**
     * Busca todas las direcciones de una persona por su ID
     */
    List<Direccion> findByPersonaId(Long personaId);
    
    /**
     * Busca la dirección principal de una persona
     */
    Optional<Direccion> findByPersonaIdAndEsPrincipalTrue(Long personaId);
    
    /**
     * Verifica si una persona tiene direcciones registradas
     */
    boolean existsByPersonaId(Long personaId);
    
    /**
     * Cuenta cuántas direcciones tiene una persona
     */
    long countByPersonaId(Long personaId);
    
    /**
     * Busca direcciones por ciudad
     */
    List<Direccion> findByCiudadContainingIgnoreCase(String ciudad);
    
    /**
     * Busca direcciones por provincia
     */
    List<Direccion> findByProvinciaContainingIgnoreCase(String provincia);
    
    /**
     * Busca una dirección por Place ID de Google
     */
    Optional<Direccion> findByPlaceId(String placeId);
    
    /**
     * Verifica si existe una dirección con un Place ID específico
     */
    boolean existsByPlaceId(String placeId);
}

