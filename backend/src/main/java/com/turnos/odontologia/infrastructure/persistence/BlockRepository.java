package com.turnos.odontologia.infrastructure.persistence;

import com.turnos.odontologia.domain.Block;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, Long> {

    List<Block> findAllByTenantId(Long tenantId);

    Optional<Block> findByIdAndTenantId(Long id, Long tenantId);
}
