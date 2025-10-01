package com.sigret.repositories;

import com.sigret.entities.TipoPersona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoPersonaRepository extends JpaRepository<TipoPersona, Long> {
}
