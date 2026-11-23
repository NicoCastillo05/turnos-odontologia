package com.turnos.odontologia.api.dto.agenda;

import com.turnos.odontologia.application.agenda.WeeklySchedule;

public record ProfessionalResponse(
        Long id,
        Long tenantId,
        Long userId,
        String specialty,
        WeeklySchedule schedule,
        boolean active) {
}
