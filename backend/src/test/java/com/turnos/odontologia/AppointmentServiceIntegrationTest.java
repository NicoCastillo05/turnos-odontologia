package com.turnos.odontologia;

import com.turnos.odontologia.api.dto.agenda.AppointmentRequest;
import com.turnos.odontologia.api.dto.agenda.AppointmentResponse;
import com.turnos.odontologia.application.agenda.AppointmentService;
import com.turnos.odontologia.application.agenda.ProfessionalScheduleCodec;
import com.turnos.odontologia.application.agenda.TimeWindow;
import com.turnos.odontologia.application.agenda.WeeklySchedule;
import com.turnos.odontologia.application.exception.AvailabilityConflictException;
import com.turnos.odontologia.application.exception.BusinessValidationException;
import com.turnos.odontologia.application.exception.ResourceNotFoundException;
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
import com.turnos.odontologia.infrastructure.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AppointmentServiceIntegrationTest {

    private static final long TENANT = 4201L;
    private static final LocalDateTime TUESDAY_TEN = LocalDateTime.of(2026, 11, 24, 10, 0);

    @Autowired AppointmentService service;
    @Autowired AppointmentRepository appointments;
    @Autowired BlockRepository blocks;
    @Autowired UserRepository users;
    @Autowired ProfessionalRepository professionals;
    @Autowired ChairRepository chairs;
    @Autowired PatientRepository patients;
    @Autowired ServiceRepository services;
    @Autowired ProfessionalScheduleCodec scheduleCodec;

    @AfterEach
    void clearContext() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void creaTurnoProgramadoYCalculaFinDesdePrestacion() {
        Fixture fixture = fixture("happy", 30);
        authenticate(fixture.admin(), "admin");

        AppointmentResponse created = service.create(request(fixture, TUESDAY_TEN, false));

        assertThat(created.status()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(created.endAt()).isEqualTo(TUESDAY_TEN.plusMinutes(30));
        assertThat(created.overbooked()).isFalse();
    }

    @Test
    void rechazaFueraDeHorarioYReferenciaDeOtroTenant() {
        Fixture fixture = fixture("invalid", 30);
        authenticate(fixture.admin(), "admin");

        assertThatThrownBy(() -> service.create(request(fixture, TUESDAY_TEN.minusHours(2), false)))
                .isInstanceOf(BusinessValidationException.class);

        Patient foreign = new Patient();
        foreign.setTenantId(9999L);
        foreign.setFullName("Paciente de otra clínica");
        patients.save(foreign);
        AppointmentRequest foreignRequest = new AppointmentRequest(
                foreign.getId(), fixture.professional().getId(), fixture.chair().getId(),
                fixture.service().getId(), TUESDAY_TEN, false, null);
        assertThatThrownBy(() -> service.create(foreignRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void conflictoRequiereConfirmacionYElSobreturnoRegistraAutorizante() {
        Fixture fixture = fixture("overbook", 30);
        authenticate(fixture.admin(), "admin");
        service.create(request(fixture, TUESDAY_TEN, false));

        assertThatThrownBy(() -> service.create(request(fixture, TUESDAY_TEN.plusMinutes(10), false)))
                .isInstanceOf(AvailabilityConflictException.class)
                .extracting("code").isEqualTo("PROFESSIONAL_CONFLICT");

        AppointmentResponse overbooked = service.create(request(fixture, TUESDAY_TEN.plusMinutes(10), true));
        assertThat(overbooked.overbooked()).isTrue();
        assertThat(overbooked.overbookAuthorizedByUserId()).isEqualTo(fixture.admin().getId());
    }

    @Test
    void sobreturnoNoIgnoraBloqueos() {
        Fixture fixture = fixture("blocked", 30);
        authenticate(fixture.admin(), "admin");
        Block block = new Block();
        block.setTenantId(TENANT);
        block.setProfessional(fixture.professional());
        block.setStartAt(TUESDAY_TEN);
        block.setEndAt(TUESDAY_TEN.plusMinutes(30));
        block.setReason("Ausencia");
        blocks.save(block);

        assertThatThrownBy(() -> service.create(request(fixture, TUESDAY_TEN, true)))
                .isInstanceOf(AvailabilityConflictException.class)
                .extracting("code").isEqualTo("PROFESSIONAL_BLOCKED");
    }

    @Test
    void actualizaRecalculaCambiaEstadoYAdminElimina() {
        Fixture fixture = fixture("lifecycle", 30);
        authenticate(fixture.admin(), "admin");
        AppointmentResponse created = service.create(request(fixture, TUESDAY_TEN, false));

        AppointmentRequest moved = request(fixture, TUESDAY_TEN.plusHours(1), false);
        AppointmentResponse updated = service.update(created.id(), moved);
        assertThat(updated.startAt()).isEqualTo(TUESDAY_TEN.plusHours(1));
        assertThat(updated.endAt()).isEqualTo(TUESDAY_TEN.plusHours(1).plusMinutes(30));

        assertThat(service.updateStatus(created.id(), AppointmentStatus.CONFIRMED).status())
                .isEqualTo(AppointmentStatus.CONFIRMED);
        service.delete(created.id());
        assertThat(appointments.findByIdAndTenantId(created.id(), TENANT)).isEmpty();
    }

    @Test
    void recepcionistaYProfesionalPropioPuedenAutorizarSobreturno() {
        Fixture fixture = fixture("authorized", 30);
        User receptionist = user("reception-authorized", "receptionist");
        authenticate(fixture.admin(), "admin");
        service.create(request(fixture, TUESDAY_TEN, false));

        authenticate(receptionist, "receptionist");
        assertThat(service.create(request(fixture, TUESDAY_TEN.plusMinutes(5), true)).overbooked()).isTrue();

        authenticate(fixture.professionalUser(), "professional");
        AppointmentResponse professionalOverbooking = service.create(
                request(fixture, TUESDAY_TEN.plusMinutes(10), true));
        assertThat(professionalOverbooking.overbookAuthorizedByUserId())
                .isEqualTo(fixture.professionalUser().getId());
    }

    @Test
    void profesionalNoOperaAgendaAjenaYRecepcionistaNoElimina() {
        Fixture own = fixture("own", 30);
        Fixture other = fixture("other-professional", 30);
        User receptionist = user("reception-delete", "receptionist");

        authenticate(own.professionalUser(), "professional");
        assertThatThrownBy(() -> service.create(request(other, TUESDAY_TEN, true)))
                .isInstanceOf(AccessDeniedException.class);

        authenticate(own.admin(), "admin");
        AppointmentResponse created = service.create(request(own, TUESDAY_TEN, false));
        authenticate(receptionist, "receptionist");
        assertThatThrownBy(() -> service.delete(created.id())).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void recursoInactivoSeRechazaYUpdateExcluyeElMismoTurno() {
        Fixture fixture = fixture("inactive", 30);
        authenticate(fixture.admin(), "admin");
        AppointmentResponse created = service.create(request(fixture, TUESDAY_TEN, false));

        assertThat(service.update(created.id(), request(fixture, TUESDAY_TEN, false)).id())
                .isEqualTo(created.id());

        fixture.chair().setActive(false);
        chairs.save(fixture.chair());
        assertThatThrownBy(() -> service.create(request(fixture, TUESDAY_TEN.plusHours(1), false)))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void detectaConflictoExclusivoDeSillon() {
        Fixture occupied = fixture("chair-occupied", 30);
        Fixture otherProfessional = fixture("chair-other-professional", 30);
        authenticate(occupied.admin(), "admin");
        service.create(request(occupied, TUESDAY_TEN, false));

        AppointmentRequest sameChair = new AppointmentRequest(
                otherProfessional.patient().getId(), otherProfessional.professional().getId(),
                occupied.chair().getId(), otherProfessional.service().getId(),
                TUESDAY_TEN.plusMinutes(10), false, null);

        assertThatThrownBy(() -> service.create(sameChair))
                .isInstanceOf(AvailabilityConflictException.class)
                .extracting("code").isEqualTo("CHAIR_CONFLICT");
    }

    @Test
    void detectaBloqueoExclusivoDeSillon() {
        Fixture fixture = fixture("chair-blocked", 30);
        authenticate(fixture.admin(), "admin");
        Block block = new Block();
        block.setTenantId(TENANT);
        block.setChair(fixture.chair());
        block.setStartAt(TUESDAY_TEN);
        block.setEndAt(TUESDAY_TEN.plusMinutes(30));
        block.setReason("Mantenimiento");
        blocks.save(block);

        assertThatThrownBy(() -> service.create(request(fixture, TUESDAY_TEN, true)))
                .isInstanceOf(AvailabilityConflictException.class)
                .extracting("code").isEqualTo("CHAIR_BLOCKED");
    }

    @Test
    void updateRechazadoPorConflictoConservaHorarioOriginal() {
        Fixture fixture = fixture("update-conflict", 30);
        authenticate(fixture.admin(), "admin");
        service.create(request(fixture, TUESDAY_TEN, false));
        AppointmentResponse later = service.create(request(fixture, TUESDAY_TEN.plusHours(1), false));

        assertThatThrownBy(() -> service.update(
                later.id(), request(fixture, TUESDAY_TEN.plusMinutes(15), false)))
                .isInstanceOf(AvailabilityConflictException.class)
                .extracting("code").isEqualTo("PROFESSIONAL_CONFLICT");
        assertThat(service.get(later.id()).startAt()).isEqualTo(TUESDAY_TEN.plusHours(1));
    }

    private Fixture fixture(String suffix, int durationMinutes) {
        User admin = user("admin-" + suffix, "admin");
        User professionalUser = user("professional-" + suffix, "professional");

        Professional professional = new Professional();
        professional.setTenantId(TENANT);
        professional.setUser(professionalUser);
        professional.setSpecialty("Odontología");
        professional.setSchedule(scheduleCodec.encode(new WeeklySchedule(Map.of(
                DayOfWeek.TUESDAY, List.of(new TimeWindow(LocalTime.of(9, 0), LocalTime.of(13, 0)))))));
        professional.setActive(true);
        professionals.save(professional);

        Chair chair = new Chair();
        chair.setTenantId(TENANT);
        chair.setName("Sillón " + suffix);
        chair.setActive(true);
        chairs.save(chair);

        com.turnos.odontologia.domain.Service dentalService = new com.turnos.odontologia.domain.Service();
        dentalService.setTenantId(TENANT);
        dentalService.setName("Prestación " + suffix);
        dentalService.setDurationMinutes(durationMinutes);
        dentalService.setActive(true);
        services.save(dentalService);

        Patient patient = new Patient();
        patient.setTenantId(TENANT);
        patient.setFullName("Paciente de prueba " + suffix);
        patients.save(patient);
        return new Fixture(admin, professionalUser, professional, chair, dentalService, patient);
    }

    private User user(String suffix, String role) {
        User user = new User();
        user.setTenantId(TENANT);
        user.setEmail(suffix + "@example.test");
        user.setPasswordHash("hash");
        user.setRole(role);
        return users.save(user);
    }

    private AppointmentRequest request(Fixture fixture, LocalDateTime startAt, boolean allowOverbooking) {
        return new AppointmentRequest(fixture.patient().getId(), fixture.professional().getId(),
                fixture.chair().getId(), fixture.service().getId(), startAt, allowOverbooking, "Control");
    }

    private void authenticate(User user, String role) {
        TenantContext.set(TENANT);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                user.getId(), null, List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))));
    }

    private record Fixture(User admin, User professionalUser, Professional professional, Chair chair,
                           com.turnos.odontologia.domain.Service service, Patient patient) {
    }
}
