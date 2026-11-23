package com.turnos.odontologia.application.agenda;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

public record WeeklySchedule(Map<DayOfWeek, List<TimeWindow>> windows) {

    public WeeklySchedule {
        windows = windows == null ? Map.of() : Map.copyOf(windows);
    }
}
