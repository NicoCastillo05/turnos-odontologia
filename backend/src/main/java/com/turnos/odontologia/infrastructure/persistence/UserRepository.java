package com.turnos.odontologia.infrastructure.persistence;

import com.turnos.odontologia.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findAllByTenantId(Long tenantId);

    Optional<User> findByIdAndTenantId(Long id, Long tenantId);

    Optional<User> findByEmailAndTenantId(String email, Long tenantId);

    Optional<User> findByEmail(String email);
}
