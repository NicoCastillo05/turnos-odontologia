package com.turnos.odontologia;

import com.turnos.odontologia.domain.Clinic;
import com.turnos.odontologia.infrastructure.persistence.ClinicRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class CoreModelsIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ClinicRepository clinicRepository;

    @Test
    void migracionesAplicanEnOrdenYTodasLasTablasTienenTenantId() {
        List<String> versiones = jdbcTemplate.queryForList(
                "SELECT \"version\" FROM \"flyway_schema_history\" ORDER BY \"installed_rank\"",
                String.class);
        assertThat(versiones.size()).as("versiones aplicadas: %s", versiones).isGreaterThanOrEqualTo(4);

        List<String> tablas = List.of(
                "clinics", "users", "professionals", "chairs", "services", "patients",
                "appointments", "blocks", "payments", "deposits", "attachments", "odontograms");

        for (String tabla : tablas) {
            Integer columnas = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns " +
                            "WHERE UPPER(table_name) = UPPER(?) AND UPPER(column_name) = 'TENANT_ID'",
                    Integer.class, tabla);
            assertThat(columnas).as("columna tenant_id en %s", tabla).isEqualTo(1);
        }
    }

    @Test
    void repositoriosFiltranPorTenant() {
        Clinic clinicaA = new Clinic();
        clinicaA.setTenantId(1001L);
        clinicaA.setName("Clinica A de prueba");
        clinicaA.setSlug("test-clinica-a");
        clinicaA.setTimezone("America/Argentina/Buenos_Aires");
        clinicRepository.save(clinicaA);

        Clinic clinicaB = new Clinic();
        clinicaB.setTenantId(2002L);
        clinicaB.setName("Clinica B de prueba");
        clinicaB.setSlug("test-clinica-b");
        clinicaB.setTimezone("America/Argentina/Buenos_Aires");
        clinicRepository.save(clinicaB);

        assertThat(clinicRepository.findAllByTenantId(1001L))
                .extracting(Clinic::getSlug)
                .containsExactly("test-clinica-a");

        assertThat(clinicRepository.findByIdAndTenantId(clinicaA.getId(), 2002L)).isEmpty();
        assertThat(clinicRepository.findByIdAndTenantId(clinicaA.getId(), 1001L)).isPresent();
    }

    @Test
    void seedCargaDatosFicticios() {
        Integer roles = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT role) FROM users WHERE tenant_id = 1", Integer.class);
        assertThat(roles).isEqualTo(3);

        Integer servicios = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM services WHERE tenant_id = 1", Integer.class);
        assertThat(servicios).isGreaterThanOrEqualTo(2);

        String settings = jdbcTemplate.queryForObject(
                "SELECT settings FROM clinics WHERE slug = 'demo'", String.class);
        assertThat(settings).contains("24");

        Integer duracionConsulta = jdbcTemplate.queryForObject(
                "SELECT duration_minutes FROM services WHERE tenant_id = 1 AND name = 'Consulta'",
                Integer.class);
        assertThat(duracionConsulta).isEqualTo(30);
    }
}
