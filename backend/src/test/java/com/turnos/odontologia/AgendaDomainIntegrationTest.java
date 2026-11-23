package com.turnos.odontologia;

import com.turnos.odontologia.domain.Appointment;
import com.turnos.odontologia.domain.AppointmentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AgendaDomainIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void v005AgregaAuditoriaDeSobreturnosEIndicesDeDisponibilidad() {
        Integer columnas = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns " +
                        "WHERE UPPER(table_name) = 'APPOINTMENTS' " +
                        "AND UPPER(column_name) IN ('OVERBOOKED', 'OVERBOOK_AUTHORIZED_BY_USER_ID')",
                Integer.class);
        assertThat(columnas).isEqualTo(2);

        String valorPorDefecto = jdbcTemplate.queryForObject(
                "SELECT column_default FROM information_schema.columns " +
                        "WHERE UPPER(table_name) = 'APPOINTMENTS' AND UPPER(column_name) = 'OVERBOOKED'",
                String.class);
        assertThat(valorPorDefecto).isEqualToIgnoringCase("FALSE");

        Integer indices = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.indexes " +
                        "WHERE UPPER(index_name) IN (" +
                        "'IDX_APPOINTMENTS_TENANT_PROFESSIONAL_TIME'," +
                        "'IDX_APPOINTMENTS_TENANT_CHAIR_TIME'," +
                        "'IDX_BLOCKS_TENANT_PROFESSIONAL_TIME'," +
                        "'IDX_BLOCKS_TENANT_CHAIR_TIME')",
                Integer.class);
        assertThat(indices).isEqualTo(4);
    }

    @Test
    void appointmentUsaEstadoTipadoYAuditoriaDeSobreturno() {
        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setOverbooked(true);

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(appointment.isOverbooked()).isTrue();
        assertThat(appointment.getOverbookAuthorizedBy()).isNull();
    }
}
