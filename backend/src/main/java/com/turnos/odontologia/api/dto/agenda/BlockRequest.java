package com.turnos.odontologia.api.dto.agenda;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record BlockRequest(
        Long professionalId,
        Long chairId,
        @NotNull LocalDateTime startAt,
        @NotNull LocalDateTime endAt,
        @NotBlank String reason) {
}
