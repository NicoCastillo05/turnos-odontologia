package com.turnos.odontologia.infrastructure.security;

import com.turnos.odontologia.api.dto.agenda.BlockRequest;
import com.turnos.odontologia.infrastructure.persistence.BlockRepository;
import com.turnos.odontologia.infrastructure.persistence.ProfessionalRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("agendaAuthorization")
public class AgendaAuthorization {

    private final ProfessionalRepository professionalRepository;
    private final BlockRepository blockRepository;

    public AgendaAuthorization(ProfessionalRepository professionalRepository, BlockRepository blockRepository) {
        this.professionalRepository = professionalRepository;
        this.blockRepository = blockRepository;
    }

    public boolean ownsProfessional(Long professionalId) {
        Long tenantId = TenantContext.get();
        Long userId = currentUserId();
        if (tenantId == null || userId == null || professionalId == null) {
            return false;
        }
        return professionalRepository.findByIdAndTenantId(professionalId, tenantId)
                .map(professional -> professional.getUser().getId().equals(userId))
                .orElse(false);
    }

    public boolean canManageBlockRequest(BlockRequest request) {
        return request != null && ownsProfessional(request.professionalId());
    }

    public boolean ownsBlock(Long blockId) {
        Long tenantId = TenantContext.get();
        Long userId = currentUserId();
        if (tenantId == null || userId == null || blockId == null) {
            return false;
        }
        return blockRepository.findByIdAndTenantId(blockId, tenantId)
                .filter(block -> block.getProfessional() != null)
                .map(block -> block.getProfessional().getUser().getId().equals(userId))
                .orElse(false);
    }

    private Long currentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() == null
                ? null
                : SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal instanceof Long value ? value : null;
    }
}
