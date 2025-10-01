package com.sigret.repositories;

import com.sigret.entities.Modelo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModeloRepository extends JpaRepository<Modelo, Long> {

    boolean existsByDescripcionAndMarcaId(String descripcion, Long marcaId);

    List<Modelo> findByMarcaId(Long marcaId);

    @Query("SELECT m FROM Modelo m WHERE m.descripcion LIKE %:termino%")
    List<Modelo> buscarPorTermino(@Param("termino") String termino);
}
