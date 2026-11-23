package com.turnos.odontologia;

import com.turnos.odontologia.domain.AppointmentStatus;
import com.turnos.odontologia.domain.AppointmentStatusConverter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppointmentStatusConverterTest {

    private final AppointmentStatusConverter converter = new AppointmentStatusConverter();

    @Test
    void persisteEstadosEnMinusculasYLosRecuperaTipados() {
        assertThat(converter.convertToDatabaseColumn(AppointmentStatus.NO_SHOW)).isEqualTo("no_show");
        assertThat(converter.convertToEntityAttribute("completed")).isEqualTo(AppointmentStatus.COMPLETED);
    }

    @Test
    void conservaValoresNulos() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }
}
