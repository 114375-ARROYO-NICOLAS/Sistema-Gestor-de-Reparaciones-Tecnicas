package com.sigret.repositories;

import com.sigret.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByUsernameAndActivoTrue(String username);

    Optional<Usuario> findByIdAndActivoTrue(Long id);

    boolean existsByUsername(String username);

    @Query("SELECT u " +
            "FROM Usuario u " +
            "JOIN FETCH u.empleado e " +
            "JOIN FETCH e.persona p " +
            "WHERE u.username = :username AND u.activo = true")
    Optional<Usuario> findByUsernameWithDetails(@Param("username") String username);
}
