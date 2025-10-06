package com.sigret.repositories;

import com.sigret.entities.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {
    
    // Buscar persona por documento
    Optional<Persona> findByDocumento(String documento);
    
    // Verificar si existe una persona con el documento
    boolean existsByDocumento(String documento);
}
