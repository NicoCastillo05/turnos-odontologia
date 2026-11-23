package com.turnos.odontologia.application.agenda;

import java.time.LocalTime;

public record TimeWindow(LocalTime start, LocalTime end) {
}
