package com.turnos.odontologia.infrastructure.persistence;

import com.turnos.odontologia.domain.Chair;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChairRepository extends JpaRepository<Chair, Long> {

    List<Chair> findAllByTenantId(Long tenantId);

    Optional<Chair> findByIdAndTenantId(Long id, Long tenantId);
}
