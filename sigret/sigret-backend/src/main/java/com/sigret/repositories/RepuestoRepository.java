package com.sigret.repositories;

import com.sigret.entities.Repuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepuestoRepository extends JpaRepository<Repuesto, Long> {

    List<Repuesto> findByTipoEquipoId(Long tipoEquipoId);

    @Query("SELECT r FROM Repuesto r WHERE LOWER(r.descripcion) LIKE LOWER(CONCAT('%', :termino, '%')) " +
            "OR LOWER(r.tipoEquipo.descripcion) LIKE LOWER(CONCAT('%', :termino, '%'))")
    List<Repuesto> buscarPorTermino(@Param("termino") String termino);

    boolean existsByDescripcionAndTipoEquipoId(String descripcion, Long tipoEquipoId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Repuesto r " +
            "WHERE r.descripcion = :descripcion AND r.tipoEquipo.id = :tipoEquipoId AND r.id <> :id")
    boolean existsByDescripcionAndTipoEquipoIdAndIdNot(@Param("descripcion") String descripcion,
                                                        @Param("tipoEquipoId") Long tipoEquipoId,
                                                        @Param("id") Long id);
}