package com.turnos.odontologia.application.agenda;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ProfessionalScheduleCodec {

    private static final TypeReference<Map<DayOfWeek, List<TimeWindow>>> SCHEDULE_TYPE =
            new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    public ProfessionalScheduleCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String encode(WeeklySchedule schedule) {
        Map<DayOfWeek, List<TimeWindow>> normalized = normalize(schedule);
        try {
            return objectMapper.writeValueAsString(normalized);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("No se pudo serializar el horario", e);
        }
    }

    public WeeklySchedule decode(String json) {
        if (json == null || json.isBlank()) {
            return new WeeklySchedule(Map.of());
        }
        try {
            return new WeeklySchedule(normalize(new WeeklySchedule(objectMapper.readValue(json, SCHEDULE_TYPE))));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("El horario almacenado no es válido", e);
        }
    }

    private Map<DayOfWeek, List<TimeWindow>> normalize(WeeklySchedule schedule) {
        if (schedule == null) {
            throw new IllegalArgumentException("El horario es obligatorio");
        }

        Map<DayOfWeek, List<TimeWindow>> normalized = new EnumMap<>(DayOfWeek.class);
        schedule.windows().forEach((day, windows) -> {
            if (day == null) {
                throw new IllegalArgumentException("El día del horario es obligatorio");
            }
            List<TimeWindow> sorted = new ArrayList<>(windows == null ? List.of() : windows);
            sorted.sort((left, right) -> left.start().compareTo(right.start()));
            TimeWindow previous = null;
            for (TimeWindow window : sorted) {
                if (window == null || window.start() == null || window.end() == null ||
                        !window.start().isBefore(window.end())) {
                    throw new IllegalArgumentException("El inicio de cada ventana debe ser anterior al fin");
                }
                if (previous != null && window.start().isBefore(previous.end())) {
                    throw new IllegalArgumentException("Las ventanas del mismo día no pueden estar superpuestas");
                }
                previous = window;
            }
            normalized.put(day, List.copyOf(sorted));
        });
        return Map.copyOf(normalized);
    }
}
