package com.sigret.repositories;

import com.sigret.entities.PresupuestoToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PresupuestoTokenRepository extends JpaRepository<PresupuestoToken, Long> {

    Optional<PresupuestoToken> findByToken(String token);

    List<PresupuestoToken> findByPresupuestoIdAndUsadoFalse(Long presupuestoId);

    @Query("SELECT pt FROM PresupuestoToken pt WHERE pt.fechaExpiracion < :fecha AND pt.usado = false")
    List<PresupuestoToken> findExpiredTokens(@Param("fecha") LocalDateTime fecha);

    void deleteByPresupuestoIdAndUsadoFalse(Long presupuestoId);
}