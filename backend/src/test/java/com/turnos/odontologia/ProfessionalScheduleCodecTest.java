package com.turnos.odontologia;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.turnos.odontologia.application.agenda.ProfessionalScheduleCodec;
import com.turnos.odontologia.application.agenda.TimeWindow;
import com.turnos.odontologia.application.agenda.WeeklySchedule;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfessionalScheduleCodecTest {

    private final ProfessionalScheduleCodec codec = new ProfessionalScheduleCodec(
            new ObjectMapper().findAndRegisterModules());

    @Test
    void normalizaYConservaMultiplesVentanas() {
        WeeklySchedule schedule = new WeeklySchedule(Map.of(
                DayOfWeek.MONDAY, List.of(
                        new TimeWindow(LocalTime.of(14, 0), LocalTime.of(18, 0)),
                        new TimeWindow(LocalTime.of(9, 0), LocalTime.of(12, 0))),
                DayOfWeek.FRIDAY, List.of(
                        new TimeWindow(LocalTime.of(8, 30), LocalTime.of(13, 0)))));

        WeeklySchedule decoded = codec.decode(codec.encode(schedule));

        assertThat(decoded.windows().get(DayOfWeek.MONDAY))
                .containsExactly(
                        new TimeWindow(LocalTime.of(9, 0), LocalTime.of(12, 0)),
                        new TimeWindow(LocalTime.of(14, 0), LocalTime.of(18, 0)));
        assertThat(decoded.windows().get(DayOfWeek.FRIDAY)).hasSize(1);
    }

    @Test
    void rechazaVentanaConFinNoPosterior() {
        WeeklySchedule schedule = new WeeklySchedule(Map.of(
                DayOfWeek.TUESDAY, List.of(
                        new TimeWindow(LocalTime.of(10, 0), LocalTime.of(10, 0)))));

        assertThatThrownBy(() -> codec.encode(schedule))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inicio");
    }

    @Test
    void rechazaVentanasSuperpuestas() {
        WeeklySchedule schedule = new WeeklySchedule(Map.of(
                DayOfWeek.WEDNESDAY, List.of(
                        new TimeWindow(LocalTime.of(9, 0), LocalTime.of(12, 0)),
                        new TimeWindow(LocalTime.of(11, 30), LocalTime.of(13, 0)))));

        assertThatThrownBy(() -> codec.encode(schedule))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("superpuestas");
    }
}
