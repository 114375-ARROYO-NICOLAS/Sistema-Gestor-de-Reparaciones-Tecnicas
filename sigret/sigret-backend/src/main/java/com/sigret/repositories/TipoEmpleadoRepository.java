package com.sigret.repositories;

import com.sigret.entities.TipoEmpleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoEmpleadoRepository extends JpaRepository<TipoEmpleado,Long> {
}
