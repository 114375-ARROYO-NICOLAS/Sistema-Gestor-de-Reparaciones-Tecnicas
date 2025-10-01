package com.sigret.repositories;

import com.sigret.entities.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    boolean existsByNumeroSerie(String numeroSerie);

    List<Equipo> findByMarcaId(Long marcaId);

    List<Equipo> findByTipoEquipoId(Long tipoEquipoId);

    @Query("SELECT e FROM Equipo e WHERE e.numeroSerie LIKE %:termino% OR e.observaciones LIKE %:termino%")
    List<Equipo> buscarPorTermino(@Param("termino") String termino);

    @Query("SELECT e FROM Equipo e JOIN FETCH e.marca m JOIN FETCH e.tipoEquipo te LEFT JOIN FETCH e.modelo mo WHERE e.id = :id")
    Equipo findByIdWithDetails(@Param("id") Long id);
}
