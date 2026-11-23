package com.turnos.odontologia;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.turnos.odontologia.api.dto.agenda.ChairRequest;
import com.turnos.odontologia.domain.User;
import com.turnos.odontologia.domain.Block;
import com.turnos.odontologia.domain.Chair;
import com.turnos.odontologia.domain.Professional;
import com.turnos.odontologia.infrastructure.persistence.BlockRepository;
import com.turnos.odontologia.infrastructure.persistence.ChairRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AgendaResourceControllerIntegrationTest {

    private static final long TENANT_A = 3201L;
    private static final long TENANT_B = 3202L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfessionalRepository professionalRepository;

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ChairRepository chairRepository;

    @Test
    void adminCreaSillonYRecepcionistaPuedeListarlo() throws Exception {
        mockMvc.perform(post("/api/chairs")
                        .header("Authorization", bearer(10L, TENANT_A, "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChairRequest("Sillón HTTP", true))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenantId").value(TENANT_A))
                .andExpect(jsonPath("$.name").value("Sillón HTTP"));

        mockMvc.perform(get("/api/chairs")
                        .header("Authorization", bearer(11L, TENANT_A, "receptionist")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sillón HTTP"));
    }

    @Test
    void recepcionistaNoPuedeModificarConfiguracion() throws Exception {
        mockMvc.perform(post("/api/chairs")
                        .header("Authorization", bearer(11L, TENANT_A, "receptionist"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChairRequest("No permitido", true))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void listadoNoExponeRecursosDeOtroTenant() throws Exception {
        mockMvc.perform(post("/api/chairs")
                        .header("Authorization", bearer(20L, TENANT_B, "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChairRequest("Sillón B", true))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/chairs")
                        .header("Authorization", bearer(10L, TENANT_A, "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void bloqueoInvalidoDevuelveErrorEstableSinDatosSensibles() throws Exception {
        Map<String, Object> request = Map.of(
                "startAt", LocalDateTime.of(2026, 11, 24, 11, 0).toString(),
                "endAt", LocalDateTime.of(2026, 11, 24, 10, 0).toString(),
                "reason", "Intervalo inválido");

        mockMvc.perform(post("/api/blocks")
                        .header("Authorization", bearer(11L, TENANT_A, "receptionist"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BUSINESS_VALIDATION"))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void adminEliminaSillonYObtieneNoContent() throws Exception {
        String response = mockMvc.perform(post("/api/chairs")
                        .header("Authorization", bearer(10L, TENANT_A, "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChairRequest("A eliminar", true))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long chairId = objectMapper.readTree(response).get("id").asLong();
        mockMvc.perform(delete("/api/chairs/{id}", chairId)
                        .header("Authorization", bearer(10L, TENANT_A, "admin")))
                .andExpect(status().isNoContent());
    }

    @Test
    void recursoAusenteDevuelveNotFoundSinFiltrarOtroTenant() throws Exception {
        mockMvc.perform(get("/api/chairs/{id}", 999999L)
                        .header("Authorization", bearer(10L, TENANT_A, "admin")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void horarioProfesionalSuperpuestoDevuelveValidationError() throws Exception {
        User professionalUser = new User();
        professionalUser.setTenantId(TENANT_A);
        professionalUser.setEmail("schedule-http@example.test");
        professionalUser.setPasswordHash("hash");
        professionalUser.setRole("professional");
        userRepository.save(professionalUser);

        Map<String, Object> request = Map.of(
                "userId", professionalUser.getId(),
                "specialty", "Odontología",
                "schedule", Map.of("windows", Map.of("MONDAY", List.of(
                        Map.of("start", "09:00", "end", "12:00"),
                        Map.of("start", "11:00", "end", "13:00")))),
                "active", true);

        mockMvc.perform(post("/api/professionals")
                        .header("Authorization", bearer(10L, TENANT_A, "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BUSINESS_VALIDATION"));
    }

    @Test
    void ventanaProfesionalInvertidaTambienDevuelveValidationError() throws Exception {
        User professionalUser = new User();
        professionalUser.setTenantId(TENANT_A);
        professionalUser.setEmail("invalid-window-http@example.test");
        professionalUser.setPasswordHash("hash");
        professionalUser.setRole("professional");
        userRepository.save(professionalUser);

        Map<String, Object> request = Map.of(
                "userId", professionalUser.getId(),
                "specialty", "Odontología",
                "schedule", Map.of("windows", Map.of("MONDAY", List.of(
                        Map.of("start", "13:00", "end", "12:00")))),
                "active", true);

        mockMvc.perform(post("/api/professionals")
                        .header("Authorization", bearer(10L, TENANT_A, "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BUSINESS_VALIDATION"));
    }

    @Test
    void updateConHorarioSuperpuestoDevuelve422YConservaHorarioAnterior() throws Exception {
        User professionalUser = saveUser(
                TENANT_A, "schedule-update-http@example.test", "professional");
        String authorization = bearer(10L, TENANT_A, "admin");
        Map<String, Object> validRequest = Map.of(
                "userId", professionalUser.getId(),
                "specialty", "Odontología",
                "schedule", Map.of("windows", Map.of("MONDAY", List.of(
                        Map.of("start", "09:00", "end", "12:00")))),
                "active", true);

        String createdBody = mockMvc.perform(post("/api/professionals")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode created = objectMapper.readTree(createdBody);
        long professionalId = created.get("id").asLong();
        JsonNode originalSchedule = created.get("schedule");

        Map<String, Object> overlappingRequest = Map.of(
                "userId", professionalUser.getId(),
                "specialty", "No debe aplicarse",
                "schedule", Map.of("windows", Map.of("MONDAY", List.of(
                        Map.of("start", "09:00", "end", "12:00"),
                        Map.of("start", "11:00", "end", "13:00")))),
                "active", true);
        mockMvc.perform(put("/api/professionals/{id}", professionalId)
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overlappingRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BUSINESS_VALIDATION"));

        String persistedBody = mockMvc.perform(get("/api/professionals/{id}", professionalId)
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(objectMapper.readTree(persistedBody).get("schedule")).isEqualTo(originalSchedule);
    }

    @Test
    void profesionalSoloPuedeLeerSusBloqueos() throws Exception {
        User ownerUser = saveUser(TENANT_A, "block-owner@example.test", "professional");
        Professional owner = saveProfessional(ownerUser);
        Block ownBlock = saveBlock(owner, "Bloqueo propio", 10);

        User otherUser = saveUser(TENANT_A, "block-other@example.test", "professional");
        Professional other = saveProfessional(otherUser);
        Block otherBlock = saveBlock(other, "Bloqueo ajeno", 12);

        mockMvc.perform(get("/api/blocks")
                        .header("Authorization", bearer(ownerUser.getId(), TENANT_A, "professional")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(ownBlock.getId()));

        mockMvc.perform(get("/api/blocks/{id}", otherBlock.getId())
                        .header("Authorization", bearer(ownerUser.getId(), TENANT_A, "professional")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void administradorYRecepcionistaPuedenListarTodosLosBloqueosDelTenant() throws Exception {
        Professional first = saveProfessional(saveUser(
                TENANT_A, "block-admin-first@example.test", "professional"));
        Professional second = saveProfessional(saveUser(
                TENANT_A, "block-admin-second@example.test", "professional"));
        saveBlock(first, "Primero", 9);
        saveBlock(second, "Segundo", 14);

        mockMvc.perform(get("/api/blocks")
                        .header("Authorization", bearer(10L, TENANT_A, "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/blocks")
                        .header("Authorization", bearer(11L, TENANT_A, "receptionist")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void administradorCompletaCrudHttpDePrestacionProfesionalYBloqueo() throws Exception {
        User professionalUser = saveUser(TENANT_A, "crud-professional@example.test", "professional");
        String authorization = bearer(10L, TENANT_A, "admin");

        String professionalBody = mockMvc.perform(post("/api/professionals")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "userId", professionalUser.getId(),
                                "specialty", "Endodoncia",
                                "schedule", Map.of("windows", Map.of()),
                                "active", true))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long professionalId = objectMapper.readTree(professionalBody).get("id").asLong();

        String chairBody = mockMvc.perform(post("/api/chairs")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChairRequest("Sillón CRUD", true))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long chairId = objectMapper.readTree(chairBody).get("id").asLong();

        String serviceBody = mockMvc.perform(post("/api/services")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Consulta CRUD", "durationMinutes", 30,
                                "color", "#112233", "active", true))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long serviceId = objectMapper.readTree(serviceBody).get("id").asLong();

        String blockBody = mockMvc.perform(post("/api/blocks")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "professionalId", professionalId,
                                "chairId", chairId,
                                "startAt", "2026-11-24T10:00:00",
                                "endAt", "2026-11-24T11:00:00",
                                "reason", "Capacitación"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long blockId = objectMapper.readTree(blockBody).get("id").asLong();

        mockMvc.perform(get("/api/services/{id}", serviceId).header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Consulta CRUD"));
        mockMvc.perform(get("/api/professionals/{id}", professionalId).header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialty").value("Endodoncia"));
        mockMvc.perform(get("/api/blocks/{id}", blockId).header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reason").value("Capacitación"));

        mockMvc.perform(put("/api/services/{id}", serviceId)
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Consulta actualizada", "durationMinutes", 45,
                                "color", "#445566", "active", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.durationMinutes").value(45));
        mockMvc.perform(put("/api/professionals/{id}", professionalId)
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "userId", professionalUser.getId(),
                                "specialty", "Cirugía",
                                "schedule", Map.of("windows", Map.of()),
                                "active", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialty").value("Cirugía"));
        mockMvc.perform(put("/api/blocks/{id}", blockId)
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "professionalId", professionalId,
                                "chairId", chairId,
                                "startAt", "2026-11-24T11:00:00",
                                "endAt", "2026-11-24T12:00:00",
                                "reason", "Reunión"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reason").value("Reunión"));

        mockMvc.perform(delete("/api/blocks/{id}", blockId).header("Authorization", authorization))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/services/{id}", serviceId).header("Authorization", authorization))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/professionals/{id}", professionalId).header("Authorization", authorization))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/chairs/{id}", chairId).header("Authorization", authorization))
                .andExpect(status().isNoContent());
    }

    @Test
    void profesionalPrestacionYBloqueoDeOtroTenantPermanecenOcultos() throws Exception {
        Professional foreignProfessional = saveProfessional(saveUser(
                TENANT_B, "foreign-professional@example.test", "professional"));
        Block foreignBlock = saveBlock(foreignProfessional, "Bloqueo privado", 15);
        Chair foreignChair = new Chair();
        foreignChair.setTenantId(TENANT_B);
        foreignChair.setName("Sillón privado");
        foreignChair.setActive(true);
        chairRepository.save(foreignChair);
        com.turnos.odontologia.domain.Service foreignService = new com.turnos.odontologia.domain.Service();
        foreignService.setTenantId(TENANT_B);
        foreignService.setName("Prestación privada");
        foreignService.setDurationMinutes(30);
        foreignService.setActive(true);
        serviceRepository.save(foreignService);
        String authorization = bearer(10L, TENANT_A, "admin");

        mockMvc.perform(get("/api/professionals/{id}", foreignProfessional.getId())
                        .header("Authorization", authorization))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/services/{id}", foreignService.getId())
                        .header("Authorization", authorization))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/blocks/{id}", foreignBlock.getId())
                        .header("Authorization", authorization))
                .andExpect(status().isNotFound());

        User localProfessionalUser = saveUser(
                TENANT_A, "local-cross-write@example.test", "professional");
        mockMvc.perform(put("/api/professionals/{id}", foreignProfessional.getId())
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "userId", localProfessionalUser.getId(),
                                "specialty", "No permitido",
                                "schedule", Map.of("windows", Map.of()),
                                "active", true))))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/professionals/{id}", foreignProfessional.getId())
                        .header("Authorization", authorization))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/api/services/{id}", foreignService.getId())
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "No permitida", "durationMinutes", 30,
                                "active", true))))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/services/{id}", foreignService.getId())
                        .header("Authorization", authorization))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/api/blocks/{id}", foreignBlock.getId())
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "professionalId", foreignProfessional.getId(),
                                "startAt", "2026-11-24T16:00:00",
                                "endAt", "2026-11-24T17:00:00",
                                "reason", "No permitido"))))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/blocks/{id}", foreignBlock.getId())
                        .header("Authorization", authorization))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/blocks")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "professionalId", foreignProfessional.getId(),
                                "startAt", "2026-11-24T10:00:00",
                                "endAt", "2026-11-24T11:00:00",
                                "reason", "Referencia profesional cruzada"))))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/blocks")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "chairId", foreignChair.getId(),
                                "startAt", "2026-11-24T11:00:00",
                                "endAt", "2026-11-24T12:00:00",
                                "reason", "Referencia sillón cruzada"))))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/professionals").header("Authorization", authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/api/services").header("Authorization", authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/api/blocks").header("Authorization", authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void profesionalAdministraSoloBloqueosPropios() throws Exception {
        User ownerUser = saveUser(TENANT_A, "block-crud-owner@example.test", "professional");
        Professional owner = saveProfessional(ownerUser);
        User otherUser = saveUser(TENANT_A, "block-crud-other@example.test", "professional");
        Professional other = saveProfessional(otherUser);
        Block otherBlock = saveBlock(other, "Ajeno", 15);
        String authorization = bearer(ownerUser.getId(), TENANT_A, "professional");

        String ownBody = mockMvc.perform(post("/api/blocks")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "professionalId", owner.getId(),
                                "startAt", "2026-11-24T09:00:00",
                                "endAt", "2026-11-24T10:00:00",
                                "reason", "Propio"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long ownBlockId = objectMapper.readTree(ownBody).get("id").asLong();

        mockMvc.perform(put("/api/blocks/{id}", ownBlockId)
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "professionalId", owner.getId(),
                                "startAt", "2026-11-24T10:00:00",
                                "endAt", "2026-11-24T11:00:00",
                                "reason", "Propio actualizado"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reason").value("Propio actualizado"));

        mockMvc.perform(post("/api/blocks")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "professionalId", other.getId(),
                                "startAt", "2026-11-24T12:00:00",
                                "endAt", "2026-11-24T13:00:00",
                                "reason", "No permitido"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        mockMvc.perform(put("/api/blocks/{id}", otherBlock.getId())
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "professionalId", owner.getId(),
                                "startAt", "2026-11-24T16:00:00",
                                "endAt", "2026-11-24T17:00:00",
                                "reason", "No permitido"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        mockMvc.perform(delete("/api/blocks/{id}", otherBlock.getId())
                        .header("Authorization", authorization))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        mockMvc.perform(delete("/api/blocks/{id}", ownBlockId)
                        .header("Authorization", authorization))
                .andExpect(status().isNoContent());
    }

    private User saveUser(long tenantId, String email, String role) {
        User user = new User();
        user.setTenantId(tenantId);
        user.setEmail(email);
        user.setPasswordHash("hash");
        user.setRole(role);
        return userRepository.save(user);
    }

    private Professional saveProfessional(User user) {
        Professional professional = new Professional();
        professional.setTenantId(user.getTenantId());
        professional.setUser(user);
        professional.setSpecialty("Odontología");
        professional.setSchedule("{}");
        professional.setActive(true);
        return professionalRepository.save(professional);
    }

    private Block saveBlock(Professional professional, String reason, int hour) {
        Block block = new Block();
        block.setTenantId(professional.getTenantId());
        block.setProfessional(professional);
        block.setStartAt(LocalDateTime.of(2026, 11, 24, hour, 0));
        block.setEndAt(LocalDateTime.of(2026, 11, 24, hour + 1, 0));
        block.setReason(reason);
        return blockRepository.save(block);
    }

    private String bearer(long userId, long tenantId, String role) {
        return "Bearer " + jwtService.generateAccessToken(userId, tenantId, role);
    }
}
