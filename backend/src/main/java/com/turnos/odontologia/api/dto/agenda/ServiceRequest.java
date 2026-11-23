package com.turnos.odontologia.api.dto.agenda;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ServiceRequest(
        @NotBlank String name,
        @Min(1) int durationMinutes,
        String color,
        boolean active) {
}
