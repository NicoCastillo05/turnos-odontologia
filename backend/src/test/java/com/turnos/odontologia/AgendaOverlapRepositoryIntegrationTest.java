package com.turnos.odontologia;

import com.turnos.odontologia.domain.Appointment;
import com.turnos.odontologia.domain.AppointmentStatus;
import com.turnos.odontologia.domain.Block;
import com.turnos.odontologia.domain.Chair;
import com.turnos.odontologia.domain.Patient;
import com.turnos.odontologia.domain.Professional;
import com.turnos.odontologia.domain.User;
import com.turnos.odontologia.infrastructure.persistence.AppointmentRepository;
import com.turnos.odontologia.infrastructure.persistence.BlockRepository;
import com.turnos.odontologia.infrastructure.persistence.ChairRepository;
import com.turnos.odontologia.infrastructure.persistence.PatientRepository;
import com.turnos.odontologia.infrastructure.persistence.ProfessionalRepository;
import com.turnos.odontologia.infrastructure.persistence.ServiceRepository;
import com.turnos.odontologia.infrastructure.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AgendaOverlapRepositoryIntegrationTest {

    private static final long TENANT_A = 4101L;
    private static final long TENANT_B = 4102L;
    private static final LocalDateTime TEN = LocalDateTime.of(2026, 11, 24, 10, 0);

    @Autowired AppointmentRepository appointments;
    @Autowired BlockRepository blocks;
    @Autowired UserRepository users;
    @Autowired ProfessionalRepository professionals;
    @Autowired ChairRepository chairs;
    @Autowired PatientRepository patients;
    @Autowired ServiceRepository services;

    @Test
    void turnoDetectaSolapamientoPeroPermiteAdyacenciaYExclusionPropia() {
        Fixture fixture = fixture(TENANT_A, "a");
        Appointment existing = appointment(fixture, TEN, TEN.plusMinutes(30), AppointmentStatus.SCHEDULED);

        assertThat(appointments.existsOverlappingForProfessional(
                TENANT_A, fixture.professional().getId(), TEN.plusMinutes(15), TEN.plusMinutes(45), null)).isTrue();
        assertThat(appointments.existsOverlappingForChair(
                TENANT_A, fixture.chair().getId(), TEN.minusMinutes(15), TEN.plusMinutes(15), null)).isTrue();
        assertThat(appointments.existsOverlappingForProfessional(
                TENANT_A, fixture.professional().getId(), TEN.plusMinutes(30), TEN.plusMinutes(60), null)).isFalse();
        assertThat(appointments.existsOverlappingForProfessional(
                TENANT_A, fixture.professional().getId(), TEN, TEN.plusMinutes(30), existing.getId())).isFalse();
    }

    @Test
    void turnoCanceladoYOtroTenantNoBloquean() {
        Fixture tenantA = fixture(TENANT_A, "cancelled");
        appointment(tenantA, TEN, TEN.plusMinutes(30), AppointmentStatus.CANCELLED);
        Fixture tenantB = fixture(TENANT_B, "other");
        appointment(tenantB, TEN, TEN.plusMinutes(30), AppointmentStatus.SCHEDULED);

        assertThat(appointments.existsOverlappingForProfessional(
                TENANT_A, tenantA.professional().getId(), TEN, TEN.plusMinutes(30), null)).isFalse();
        assertThat(appointments.existsOverlappingForChair(
                TENANT_A, tenantA.chair().getId(), TEN, TEN.plusMinutes(30), null)).isFalse();
    }

    @Test
    void bloqueoUsaLaMismaSemanticaParaProfesionalYSillon() {
        Fixture fixture = fixture(TENANT_A, "block");
        Block block = new Block();
        block.setTenantId(TENANT_A);
        block.setProfessional(fixture.professional());
        block.setChair(fixture.chair());
        block.setStartAt(TEN);
        block.setEndAt(TEN.plusMinutes(30));
        block.setReason("Mantenimiento");
        blocks.saveAndFlush(block);

        assertThat(blocks.existsOverlappingForProfessional(
                TENANT_A, fixture.professional().getId(), TEN.plusMinutes(15), TEN.plusMinutes(45))).isTrue();
        assertThat(blocks.existsOverlappingForChair(
                TENANT_A, fixture.chair().getId(), TEN.plusMinutes(15), TEN.plusMinutes(45))).isTrue();
        assertThat(blocks.existsOverlappingForProfessional(
                TENANT_A, fixture.professional().getId(), TEN.plusMinutes(30), TEN.plusHours(1))).isFalse();
    }

    private Fixture fixture(long tenantId, String suffix) {
        User user = new User();
        user.setTenantId(tenantId);
        user.setEmail("professional-" + suffix + "@example.test");
        user.setPasswordHash("hash");
        user.setRole("professional");
        users.save(user);

        Professional professional = new Professional();
        professional.setTenantId(tenantId);
        professional.setUser(user);
        professional.setSpecialty("Odontología");
        professional.setSchedule("{}");
        professional.setActive(true);
        professionals.save(professional);

        Chair chair = new Chair();
        chair.setTenantId(tenantId);
        chair.setName("Sillón " + suffix);
        chair.setActive(true);
        chairs.save(chair);

        com.turnos.odontologia.domain.Service service = new com.turnos.odontologia.domain.Service();
        service.setTenantId(tenantId);
        service.setName("Consulta " + suffix);
        service.setDurationMinutes(30);
        service.setActive(true);
        services.save(service);

        Patient patient = new Patient();
        patient.setTenantId(tenantId);
        patient.setFullName("Paciente de prueba " + suffix);
        patients.save(patient);

        return new Fixture(professional, chair, service, patient);
    }

    private Appointment appointment(Fixture fixture, LocalDateTime start, LocalDateTime end,
                                    AppointmentStatus status) {
        Appointment appointment = new Appointment();
        appointment.setTenantId(fixture.professional().getTenantId());
        appointment.setPatient(fixture.patient());
        appointment.setProfessional(fixture.professional());
        appointment.setChair(fixture.chair());
        appointment.setService(fixture.service());
        appointment.setStartAt(start);
        appointment.setEndAt(end);
        appointment.setStatus(status);
        return appointments.saveAndFlush(appointment);
    }

    private record Fixture(Professional professional, Chair chair,
                           com.turnos.odontologia.domain.Service service, Patient patient) {
    }
}
