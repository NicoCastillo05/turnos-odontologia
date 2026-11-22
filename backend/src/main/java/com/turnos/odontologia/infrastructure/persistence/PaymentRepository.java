package com.turnos.odontologia.infrastructure.persistence;

import com.turnos.odontologia.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByTenantId(Long tenantId);

    Optional<Payment> findByIdAndTenantId(Long id, Long tenantId);
}
