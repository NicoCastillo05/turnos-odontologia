package com.turnos.odontologia.api.dto.agenda;

public record ServiceResponse(
        Long id,
        Long tenantId,
        String name,
        int durationMinutes,
        String color,
        boolean active) {
}
