package com.sigret.repositories;

import com.sigret.entities.Marca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarcaRepository extends JpaRepository<Marca, Long> {

    boolean existsByDescripcion(String descripcion);

    @Query("SELECT m FROM Marca m WHERE m.descripcion LIKE %:termino%")
    List<Marca> buscarPorTermino(@Param("termino") String termino);
}
