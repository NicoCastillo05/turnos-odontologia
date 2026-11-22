package com.turnos.odontologia.infrastructure.persistence;

import com.turnos.odontologia.domain.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findAllByTenantId(Long tenantId);

    Optional<Appointment> findByIdAndTenantId(Long id, Long tenantId);
}
