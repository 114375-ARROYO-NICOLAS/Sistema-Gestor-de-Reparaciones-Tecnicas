package com.sigret.repositories;

import com.sigret.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByPersonaDocumento(String documento);

    boolean existsByPersonaDocumento(String documento);

    @Query("SELECT c FROM Cliente c JOIN FETCH c.persona p WHERE c.id = :id")
    Optional<Cliente> findByIdWithPersona(@Param("id") Long id);

    @Query("SELECT c FROM Cliente c JOIN FETCH c.persona p JOIN FETCH p.contactos WHERE c.id = :id")
    Optional<Cliente> findByIdWithPersonaAndContactos(@Param("id") Long id);
}
