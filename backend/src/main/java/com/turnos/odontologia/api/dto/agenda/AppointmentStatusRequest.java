package com.turnos.odontologia.api.dto.agenda;

import com.turnos.odontologia.domain.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record AppointmentStatusRequest(@NotNull AppointmentStatus status) {
}
