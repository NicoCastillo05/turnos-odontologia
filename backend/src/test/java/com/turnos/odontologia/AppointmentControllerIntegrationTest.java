package com.turnos.odontologia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.turnos.odontologia.api.dto.agenda.AppointmentRequest;
import com.turnos.odontologia.application.agenda.ProfessionalScheduleCodec;
import com.turnos.odontologia.application.agenda.TimeWindow;
import com.turnos.odontologia.application.agenda.WeeklySchedule;
import com.turnos.odontologia.domain.Chair;
import com.turnos.odontologia.domain.Patient;
import com.turnos.odontologia.domain.Professional;
import com.turnos.odontologia.domain.User;
import com.turnos.odontologia.infrastructure.persistence.ChairRepository;
import com.turnos.odontologia.infrastructure.persistence.PatientRepository;
import com.turnos.odontologia.infrastructure.persistence.ProfessionalRepository;
import com.turnos.odontologia.infrastructure.persistence.ServiceRepository;
import com.turnos.odontologia.infrastructure.persistence.UserRepository;
import com.turnos.odontologia.infrastructure.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AppointmentControllerIntegrationTest {

    private static final long TENANT_A = 4301L;
    private static final long TENANT_B = 4302L;
    private static final LocalDateTime TUESDAY_TEN = LocalDateTime.of(2026, 11, 24, 10, 0);

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JwtService jwtService;
    @Autowired UserRepository users;
    @Autowired ProfessionalRepository professionals;
    @Autowired ChairRepository chairs;
    @Autowired ServiceRepository services;
    @Autowired PatientRepository patients;
    @Autowired ProfessionalScheduleCodec scheduleCodec;

    @Test
    void adminCompletaCrudYListadoFiltrado() throws Exception {
        Fixture fixture = fixture(TENANT_A, "crud");
        JsonNode created = create(fixture, fixture.admin(), "admin", TUESDAY_TEN, false);
        long id = created.get("id").asLong();

        mockMvc.perform(get("/api/appointments/{id}", id).header("Authorization", token(fixture.admin(), "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("scheduled"))
                .andExpect(jsonPath("$.endAt").value("2026-11-24T10:30:00"));

        mockMvc.perform(get("/api/appointments")
                        .param("from", "2026-11-24T09:00:00")
                        .param("to", "2026-11-24T12:00:00")
                        .param("professionalId", fixture.professional().getId().toString())
                        .header("Authorization", token(fixture.admin(), "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));

        AppointmentRequest moved = request(fixture, TUESDAY_TEN.plusHours(1), false);
        mockMvc.perform(put("/api/appointments/{id}", id)
                        .header("Authorization", token(fixture.admin(), "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moved)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startAt").value("2026-11-24T11:00:00"));

        mockMvc.perform(patch("/api/appointments/{id}/status", id)
                        .header("Authorization", token(fixture.admin(), "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"confirmed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("confirmed"));

        mockMvc.perform(delete("/api/appointments/{id}", id)
                        .header("Authorization", token(fixture.admin(), "admin")))
                .andExpect(status().isNoContent());
    }

    @Test
    void devuelveErroresEstablesParaConflictoYEstadoDesconocido() throws Exception {
        Fixture fixture = fixture(TENANT_A, "errors");
        create(fixture, fixture.admin(), "admin", TUESDAY_TEN, false);

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", token(fixture.receptionist(), "receptionist"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(
                                fixture, TUESDAY_TEN.plusMinutes(10), false))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PROFESSIONAL_CONFLICT"))
                .andExpect(jsonPath("$.message").value("El profesional no está disponible"));

        JsonNode created = create(fixture, fixture.receptionist(), "receptionist",
                TUESDAY_TEN.plusMinutes(10), true);
        mockMvc.perform(patch("/api/appointments/{id}/status", created.get("id").asLong())
                        .header("Authorization", token(fixture.receptionist(), "receptionist"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"unknown\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BUSINESS_VALIDATION"));
    }

    @Test
    void matrizDeRolesRestringeProfesionalAjenoYPaciente() throws Exception {
        Fixture owner = fixture(TENANT_A, "owner");
        Fixture other = fixture(TENANT_A, "other");
        long id = create(owner, owner.admin(), "admin", TUESDAY_TEN, false).get("id").asLong();

        mockMvc.perform(get("/api/appointments/{id}", id)
                        .header("Authorization", token(other.professionalUser(), "professional")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        mockMvc.perform(get("/api/appointments")
                        .header("Authorization", token(other.professionalUser(), "professional")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(get("/api/appointments")
                        .header("Authorization", token(owner.patientUser(), "patient")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", token(owner.professionalUser(), "professional"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(
                                owner, TUESDAY_TEN.plusMinutes(10), true))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.overbooked").value(true));

        mockMvc.perform(delete("/api/appointments/{id}", id)
                        .header("Authorization", token(owner.receptionist(), "receptionist")))
                .andExpect(status().isForbidden());
    }

    @Test
    void tenantAjenoNoPuedeConsultarNiReferenciarRecursos() throws Exception {
        Fixture tenantA = fixture(TENANT_A, "tenant-a");
        Fixture tenantB = fixture(TENANT_B, "tenant-b");
        long idB = create(tenantB, tenantB.admin(), "admin", TUESDAY_TEN, false).get("id").asLong();

        long idA = create(tenantA, tenantA.admin(), "admin", TUESDAY_TEN, false).get("id").asLong();

        mockMvc.perform(get("/api/appointments/{id}", idB)
                        .header("Authorization", token(tenantA.admin(), "admin")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        mockMvc.perform(put("/api/appointments/{id}", idB)
                        .header("Authorization", token(tenantA.admin(), "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(
                                tenantA, TUESDAY_TEN.plusHours(1), false))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
        mockMvc.perform(delete("/api/appointments/{id}", idB)
                        .header("Authorization", token(tenantA.admin(), "admin")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        AppointmentRequest crossTenant = new AppointmentRequest(tenantB.patient().getId(),
                tenantA.professional().getId(), tenantA.chair().getId(), tenantA.service().getId(),
                TUESDAY_TEN, false, null);
        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", token(tenantA.admin(), "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crossTenant)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        mockMvc.perform(get("/api/appointments")
                        .header("Authorization", token(tenantA.admin(), "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(idA));
    }

    private JsonNode create(Fixture fixture, User actor, String role, LocalDateTime startAt,
                            boolean allowOverbooking) throws Exception {
        String body = mockMvc.perform(post("/api/appointments")
                        .header("Authorization", token(actor, role))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(fixture, startAt, allowOverbooking))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    private Fixture fixture(long tenantId, String suffix) {
        User admin = user(tenantId, "admin-" + suffix, "admin");
        User receptionist = user(tenantId, "receptionist-" + suffix, "receptionist");
        User professionalUser = user(tenantId, "professional-" + suffix, "professional");
        User patientUser = user(tenantId, "patient-" + suffix, "patient");

        Professional professional = new Professional();
        professional.setTenantId(tenantId);
        professional.setUser(professionalUser);
        professional.setSpecialty("Odontología");
        professional.setSchedule(scheduleCodec.encode(new WeeklySchedule(Map.of(DayOfWeek.TUESDAY,
                List.of(new TimeWindow(LocalTime.of(9, 0), LocalTime.of(13, 0)))))));
        professional.setActive(true);
        professionals.save(professional);

        Chair chair = new Chair();
        chair.setTenantId(tenantId);
        chair.setName("Sillón " + suffix);
        chair.setActive(true);
        chairs.save(chair);

        com.turnos.odontologia.domain.Service dentalService = new com.turnos.odontologia.domain.Service();
        dentalService.setTenantId(tenantId);
        dentalService.setName("Prestación " + suffix);
        dentalService.setDurationMinutes(30);
        dentalService.setActive(true);
        services.save(dentalService);

        Patient patient = new Patient();
        patient.setTenantId(tenantId);
        patient.setFullName("Paciente de prueba " + suffix);
        patients.save(patient);
        return new Fixture(admin, receptionist, professionalUser, patientUser,
                professional, chair, dentalService, patient);
    }

    private User user(long tenantId, String suffix, String role) {
        User user = new User();
        user.setTenantId(tenantId);
        user.setEmail(suffix + "@example.test");
        user.setPasswordHash("hash");
        user.setRole(role);
        return users.save(user);
    }

    private AppointmentRequest request(Fixture fixture, LocalDateTime startAt, boolean allowOverbooking) {
        return new AppointmentRequest(fixture.patient().getId(), fixture.professional().getId(),
                fixture.chair().getId(), fixture.service().getId(), startAt, allowOverbooking, null);
    }

    private String token(User user, String role) {
        return "Bearer " + jwtService.generateAccessToken(user.getId(), user.getTenantId(), role);
    }

    private record Fixture(User admin, User receptionist, User professionalUser, User patientUser,
                           Professional professional, Chair chair,
                           com.turnos.odontologia.domain.Service service, Patient patient) {
    }
}
