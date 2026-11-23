package com.turnos.odontologia.api.dto.agenda;

import com.turnos.odontologia.domain.AppointmentStatus;

import java.time.LocalDateTime;

public record AppointmentResponse(
        Long id,
        Long tenantId,
        Long patientId,
        Long professionalId,
        Long chairId,
        Long serviceId,
        LocalDateTime startAt,
        LocalDateTime endAt,
        AppointmentStatus status,
        boolean overbooked,
        Long overbookAuthorizedByUserId,
        String notes) {
}
