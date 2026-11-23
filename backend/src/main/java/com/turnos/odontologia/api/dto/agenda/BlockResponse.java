package com.turnos.odontologia.api.dto.agenda;

import java.time.LocalDateTime;

public record BlockResponse(
        Long id,
        Long tenantId,
        Long professionalId,
        Long chairId,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String reason) {
}
