package com.turnos.odontologia.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum AppointmentStatus {
    SCHEDULED,
    CONFIRMED,
    CANCELLED,
    NO_SHOW,
    COMPLETED;

    @JsonValue
    public String value() {
        return name().toLowerCase(Locale.ROOT);
    }

    @JsonCreator
    public static AppointmentStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        return AppointmentStatus.valueOf(value.toUpperCase(Locale.ROOT));
    }
}
