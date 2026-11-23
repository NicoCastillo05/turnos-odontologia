package com.turnos.odontologia.infrastructure.persistence;

import com.turnos.odontologia.domain.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClinicRepository extends JpaRepository<Clinic, Long> {

    List<Clinic> findAllByTenantId(Long tenantId);

    Optional<Clinic> findByIdAndTenantId(Long id, Long tenantId);

    Optional<Clinic> findBySlug(String slug);
}
