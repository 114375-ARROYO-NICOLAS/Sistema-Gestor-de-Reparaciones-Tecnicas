package com.sigret.repositories;

import com.sigret.entities.Contacto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactoRepository extends JpaRepository<Contacto, Long> {
    
    /**
     * Encuentra todos los contactos de una persona
     */
    List<Contacto> findByPersonaId(Long personaId);
    
    /**
     * Elimina todos los contactos de una persona
     */
    void deleteByPersonaId(Long personaId);
}

