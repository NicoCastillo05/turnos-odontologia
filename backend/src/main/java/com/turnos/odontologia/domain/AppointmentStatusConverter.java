package com.turnos.odontologia.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Locale;

@Converter
public class AppointmentStatusConverter implements AttributeConverter<AppointmentStatus, String> {

    @Override
    public String convertToDatabaseColumn(AppointmentStatus status) {
        return status == null ? null : status.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public AppointmentStatus convertToEntityAttribute(String value) {
        return value == null ? null : AppointmentStatus.valueOf(value.toUpperCase(Locale.ROOT));
    }
}
