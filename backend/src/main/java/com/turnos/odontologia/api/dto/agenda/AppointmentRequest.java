package com.turnos.odontologia.api.dto.agenda;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AppointmentRequest(
        @NotNull Long patientId,
        @NotNull Long professionalId,
        @NotNull Long chairId,
        @NotNull Long serviceId,
        @NotNull LocalDateTime startAt,
        boolean allowOverbooking,
        @Size(max = 1000) String notes) {
}
