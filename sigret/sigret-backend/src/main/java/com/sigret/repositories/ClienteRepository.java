package com.sigret.repositories;

import com.sigret.entities.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Búsqueda por documento
    Optional<Cliente> findByPersonaDocumentoAndActivoTrue(String documento);

    boolean existsByPersonaDocumento(String documento);

    // Obtener solo clientes activos
    Page<Cliente> findByActivoTrue(Pageable pageable);

    List<Cliente> findByActivoTrue();

    // Búsqueda con autocompletado (case-insensitive, solo activos)
    @Query("SELECT c FROM Cliente c JOIN c.persona p " +
           "WHERE c.activo = true AND " +
           "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(p.apellido) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(p.razonSocial) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "p.documento LIKE CONCAT('%', :termino, '%'))")
    List<Cliente> buscarClientesPorTermino(@Param("termino") String termino, Pageable pageable);

    // Búsqueda paginada con filtros
    @Query("SELECT c FROM Cliente c JOIN c.persona p " +
           "WHERE c.activo = true AND " +
           "(:termino IS NULL OR " +
           "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(p.apellido) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(p.razonSocial) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "p.documento LIKE CONCAT('%', :termino, '%'))")
    Page<Cliente> buscarClientesConFiltros(@Param("termino") String termino, Pageable pageable);

    @Query("SELECT c FROM Cliente c JOIN FETCH c.persona p WHERE c.id = :id AND c.activo = true")
    Optional<Cliente> findByIdWithPersona(@Param("id") Long id);

    @Query("SELECT c FROM Cliente c JOIN FETCH c.persona p JOIN FETCH p.contactos WHERE c.id = :id AND c.activo = true")
    Optional<Cliente> findByIdWithPersonaAndContactos(@Param("id") Long id);

    // Obtener solo clientes inactivos (eliminados lógicamente)
    Page<Cliente> findByActivoFalse(Pageable pageable);

    // Buscar por ID incluyendo inactivos
    @Query("SELECT c FROM Cliente c WHERE c.id = :id")
    Optional<Cliente> findByIdIncludingInactive(@Param("id") Long id);
}
