package com.turnos.odontologia.infrastructure.persistence;

import com.turnos.odontologia.domain.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepositRepository extends JpaRepository<Deposit, Long> {

    List<Deposit> findAllByTenantId(Long tenantId);

    Optional<Deposit> findByIdAndTenantId(Long id, Long tenantId);
}
