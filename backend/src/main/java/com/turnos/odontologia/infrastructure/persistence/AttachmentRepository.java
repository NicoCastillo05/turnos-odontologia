package com.turnos.odontologia.infrastructure.persistence;

import com.turnos.odontologia.domain.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findAllByTenantId(Long tenantId);

    Optional<Attachment> findByIdAndTenantId(Long id, Long tenantId);
}
