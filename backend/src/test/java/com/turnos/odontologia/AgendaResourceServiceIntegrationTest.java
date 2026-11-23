package com.turnos.odontologia;

import com.turnos.odontologia.api.dto.agenda.BlockRequest;
import com.turnos.odontologia.api.dto.agenda.ChairRequest;
import com.turnos.odontologia.api.dto.agenda.ProfessionalRequest;
import com.turnos.odontologia.api.dto.agenda.ServiceRequest;
import com.turnos.odontologia.application.agenda.AgendaResourceService;
import com.turnos.odontologia.application.agenda.TimeWindow;
import com.turnos.odontologia.application.agenda.WeeklySchedule;
import com.turnos.odontologia.application.exception.BusinessValidationException;
import com.turnos.odontologia.application.exception.ResourceNotFoundException;
import com.turnos.odontologia.domain.User;
import com.turnos.odontologia.domain.Appointment;
import com.turnos.odontologia.domain.AppointmentStatus;
import com.turnos.odontologia.domain.Patient;
import com.turnos.odontologia.infrastructure.persistence.AppointmentRepository;
import com.turnos.odontologia.infrastructure.persistence.ChairRepository;
import com.turnos.odontologia.infrastructure.persistence.PatientRepository;
import com.turnos.odontologia.infrastructure.persistence.ProfessionalRepository;
import com.turnos.odontologia.infrastructure.persistence.ServiceRepository;
import com.turnos.odontologia.infrastructure.persistence.UserRepository;
import com.turnos.odontologia.infrastructure.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
class AgendaResourceServiceIntegrationTest {

    private static final long TENANT_A = 3101L;
    private static final long TENANT_B = 3102L;

    @Autowired
    private AgendaResourceService service;

    @Autowired
    private UserRepository userRepository;

    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private ChairRepository chairRepository;
    @Autowired private ServiceRepository serviceRepository;
    @Autowired private ProfessionalRepository professionalRepository;
    @Autowired private PatientRepository patientRepository;

    private User professionalUser;

    @BeforeEach
    void setUp() {
        TenantContext.set(TENANT_A);
        professionalUser = userRepository.save(user(TENANT_A, "profesional-a@test.invalid", "professional"));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void creaYListaRecursosDentroDelTenant() {
        var chair = service.createChair(new ChairRequest("Sillón A", true));
        var dentalService = service.createService(new ServiceRequest("Consulta inicial", 30, "#123456", true));
        var professional = service.createProfessional(new ProfessionalRequest(
                professionalUser.getId(),
                "Odontología general",
                new WeeklySchedule(Map.of(DayOfWeek.MONDAY,
                        List.of(new TimeWindow(LocalTime.of(9, 0), LocalTime.of(13, 0))))),
                true));

        assertThat(chair.tenantId()).isEqualTo(TENANT_A);
        assertThat(dentalService.durationMinutes()).isEqualTo(30);
        assertThat(professional.schedule().windows().get(DayOfWeek.MONDAY)).hasSize(1);
        assertThat(service.listChairs()).extracting("id").contains(chair.id());
        assertThat(service.listServices()).extracting("id").contains(dentalService.id());
        assertThat(service.listProfessionals()).extracting("id").contains(professional.id());
    }

    @Test
    void ocultaRecursosDeOtraClinica() {
        var chair = service.createChair(new ChairRequest("Sillón privado", true));

        TenantContext.set(TENANT_B);

        assertThatThrownBy(() -> service.getChair(chair.id()))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(service.listChairs()).isEmpty();
    }

    @Test
    void creaBloqueoProfesionalYSillon() {
        var chair = service.createChair(new ChairRequest("Sillón B", true));
        var professional = service.createProfessional(new ProfessionalRequest(
                professionalUser.getId(), "Ortodoncia", new WeeklySchedule(Map.of()), true));

        var block = service.createBlock(new BlockRequest(
                professional.id(), chair.id(),
                LocalDateTime.of(2026, 11, 24, 10, 0),
                LocalDateTime.of(2026, 11, 24, 11, 0),
                "Capacitación"));

        assertThat(block.professionalId()).isEqualTo(professional.id());
        assertThat(block.chairId()).isEqualTo(chair.id());
        assertThat(service.listBlocks()).extracting("id").contains(block.id());
    }

    @Test
    void creaBloqueosSoloPorProfesionalOSoloPorSillon() {
        var chair = service.createChair(new ChairRequest("Sillón individual", true));
        var professional = service.createProfessional(new ProfessionalRequest(
                professionalUser.getId(), "Ortodoncia", new WeeklySchedule(Map.of()), true));

        var professionalBlock = service.createBlock(new BlockRequest(
                professional.id(), null,
                LocalDateTime.of(2026, 11, 24, 8, 0),
                LocalDateTime.of(2026, 11, 24, 9, 0),
                "Ausencia"));
        var chairBlock = service.createBlock(new BlockRequest(
                null, chair.id(),
                LocalDateTime.of(2026, 11, 24, 9, 0),
                LocalDateTime.of(2026, 11, 24, 10, 0),
                "Mantenimiento"));

        assertThat(professionalBlock.professionalId()).isEqualTo(professional.id());
        assertThat(professionalBlock.chairId()).isNull();
        assertThat(chairBlock.professionalId()).isNull();
        assertThat(chairBlock.chairId()).isEqualTo(chair.id());
    }

    @Test
    void bloqueoDeOtroTenantNoPuedeConsultarse() {
        var chair = service.createChair(new ChairRequest("Sillón tenant A", true));
        var block = service.createBlock(new BlockRequest(
                null, chair.id(),
                LocalDateTime.of(2026, 11, 24, 10, 0),
                LocalDateTime.of(2026, 11, 24, 11, 0),
                "Privado"));

        TenantContext.set(TENANT_B);

        assertThatThrownBy(() -> service.getBlock(block.id()))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(service.listBlocks()).isEmpty();
    }

    @Test
    void validaDuracionUsuarioDelTenantYEstadoInactivo() {
        assertThatThrownBy(() -> service.createService(new ServiceRequest(
                "Duración inválida", 0, null, true)))
                .isInstanceOf(BusinessValidationException.class);

        User foreignUser = userRepository.save(user(
                TENANT_B, "profesional-b@test.invalid", "professional"));
        assertThatThrownBy(() -> service.createProfessional(new ProfessionalRequest(
                foreignUser.getId(), "Ajeno", new WeeklySchedule(Map.of()), true)))
                .isInstanceOf(ResourceNotFoundException.class);

        var inactiveChair = service.createChair(new ChairRequest("Fuera de servicio", false));
        var inactiveService = service.createService(new ServiceRequest(
                "Prestación pausada", 30, null, false));
        assertThat(inactiveChair.active()).isFalse();
        assertThat(inactiveService.active()).isFalse();
    }

    @Test
    void rechazaBloqueoSinRecursoOConIntervaloInvalido() {
        assertThatThrownBy(() -> service.createBlock(new BlockRequest(
                null, null,
                LocalDateTime.of(2026, 11, 24, 10, 0),
                LocalDateTime.of(2026, 11, 24, 11, 0),
                "Sin recurso")))
                .isInstanceOf(BusinessValidationException.class);

        assertThatThrownBy(() -> service.createBlock(new BlockRequest(
                null, 9999L,
                LocalDateTime.of(2026, 11, 24, 11, 0),
                LocalDateTime.of(2026, 11, 24, 10, 0),
                "Intervalo inválido")))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void recursosReferenciadosPorTurnoSeDesactivanEnLugarDeEliminarse() {
        var chairResponse = service.createChair(new ChairRequest("Sillón referenciado", true));
        var serviceResponse = service.createService(new ServiceRequest(
                "Prestación referenciada", 30, "#123456", true));
        var professionalResponse = service.createProfessional(new ProfessionalRequest(
                professionalUser.getId(), "Cirugía", new WeeklySchedule(Map.of()), true));

        var chair = chairRepository.findByIdAndTenantId(chairResponse.id(), TENANT_A).orElseThrow();
        var dentalService = serviceRepository.findByIdAndTenantId(serviceResponse.id(), TENANT_A).orElseThrow();
        var professional = professionalRepository.findByIdAndTenantId(
                professionalResponse.id(), TENANT_A).orElseThrow();
        Patient patient = new Patient();
        patient.setTenantId(TENANT_A);
        patient.setFullName("Paciente ficticio");
        patientRepository.save(patient);

        Appointment appointment = new Appointment();
        appointment.setTenantId(TENANT_A);
        appointment.setPatient(patient);
        appointment.setProfessional(professional);
        appointment.setChair(chair);
        appointment.setService(dentalService);
        appointment.setStartAt(LocalDateTime.of(2026, 11, 24, 10, 0));
        appointment.setEndAt(LocalDateTime.of(2026, 11, 24, 10, 30));
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointmentRepository.saveAndFlush(appointment);

        service.deleteChair(chair.getId());
        service.deleteService(dentalService.getId());
        service.deleteProfessional(professional.getId());

        assertThat(chairRepository.findByIdAndTenantId(chair.getId(), TENANT_A))
                .get().extracting("active").isEqualTo(false);
        assertThat(serviceRepository.findByIdAndTenantId(dentalService.getId(), TENANT_A))
                .get().extracting("active").isEqualTo(false);
        assertThat(professionalRepository.findByIdAndTenantId(professional.getId(), TENANT_A))
                .get().extracting("active").isEqualTo(false);
    }

    @Test
    void recursosSinReferenciasSeEliminanFisicamente() {
        var chair = service.createChair(new ChairRequest("Sillón libre", true));
        var dentalService = service.createService(new ServiceRequest(
                "Prestación libre", 20, "#abcdef", true));
        var professional = service.createProfessional(new ProfessionalRequest(
                professionalUser.getId(), "Periodoncia", new WeeklySchedule(Map.of()), true));

        service.deleteChair(chair.id());
        service.deleteService(dentalService.id());
        service.deleteProfessional(professional.id());

        assertThat(chairRepository.findByIdAndTenantId(chair.id(), TENANT_A)).isEmpty();
        assertThat(serviceRepository.findByIdAndTenantId(dentalService.id(), TENANT_A)).isEmpty();
        assertThat(professionalRepository.findByIdAndTenantId(professional.id(), TENANT_A)).isEmpty();
    }

    private User user(long tenantId, String email, String role) {
        User user = new User();
        user.setTenantId(tenantId);
        user.setEmail(email);
        user.setPasswordHash("hash-ficticio");
        user.setRole(role);
        return user;
    }
}
