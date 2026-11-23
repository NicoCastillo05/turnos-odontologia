package com.turnos.odontologia.api.dto.agenda;

import com.turnos.odontologia.application.agenda.WeeklySchedule;
import jakarta.validation.constraints.NotNull;

public record ProfessionalRequest(
        @NotNull Long userId,
        String specialty,
        @NotNull WeeklySchedule schedule,
        boolean active) {
}
