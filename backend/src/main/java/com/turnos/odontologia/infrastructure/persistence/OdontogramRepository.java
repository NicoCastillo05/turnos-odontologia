package com.turnos.odontologia.infrastructure.persistence;

import com.turnos.odontologia.domain.Odontogram;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OdontogramRepository extends JpaRepository<Odontogram, Long> {

    List<Odontogram> findAllByTenantId(Long tenantId);

    Optional<Odontogram> findByIdAndTenantId(Long id, Long tenantId);
}
