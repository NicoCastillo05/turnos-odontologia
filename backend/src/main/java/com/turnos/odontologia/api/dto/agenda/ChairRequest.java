package com.turnos.odontologia.api.dto.agenda;

import jakarta.validation.constraints.NotBlank;

public record ChairRequest(@NotBlank String name, boolean active) {
}
