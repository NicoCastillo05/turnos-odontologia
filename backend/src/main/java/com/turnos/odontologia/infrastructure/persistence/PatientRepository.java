package com.turnos.odontologia.infrastructure.persistence;

import com.turnos.odontologia.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    List<Patient> findAllByTenantId(Long tenantId);

    Optional<Patient> findByIdAndTenantId(Long id, Long tenantId);
}
