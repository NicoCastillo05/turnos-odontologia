package com.turnos.odontologia.infrastructure.persistence;

import com.turnos.odontologia.domain.Professional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfessionalRepository extends JpaRepository<Professional, Long> {

    List<Professional> findAllByTenantId(Long tenantId);

    Optional<Professional> findByIdAndTenantId(Long id, Long tenantId);
}
