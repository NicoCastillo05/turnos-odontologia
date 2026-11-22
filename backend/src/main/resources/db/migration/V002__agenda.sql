-- V002: agenda (profesionales, sillones, servicios, turnos y bloqueos)

CREATE TABLE professionals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    specialty VARCHAR(150),
    schedule VARCHAR(2000),
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_professionals_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE chairs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    duration_minutes INT NOT NULL,
    color VARCHAR(20),
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    professional_id BIGINT NOT NULL,
    chair_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    deposit_id BIGINT,
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT ck_appointments_status CHECK (status IN ('scheduled', 'confirmed', 'cancelled', 'no_show', 'completed')),
    CONSTRAINT fk_appointments_professional FOREIGN KEY (professional_id) REFERENCES professionals (id),
    CONSTRAINT fk_appointments_chair FOREIGN KEY (chair_id) REFERENCES chairs (id),
    CONSTRAINT fk_appointments_service FOREIGN KEY (service_id) REFERENCES services (id)
);

CREATE TABLE blocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    professional_id BIGINT,
    chair_id BIGINT,
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP NOT NULL,
    reason VARCHAR(200) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_blocks_professional FOREIGN KEY (professional_id) REFERENCES professionals (id),
    CONSTRAINT fk_blocks_chair FOREIGN KEY (chair_id) REFERENCES chairs (id)
);

-- Seed: servicios base con duraciones (regla de negocio RN-AG-03).
INSERT INTO services (tenant_id, name, duration_minutes, color, active, created_at, updated_at)
SELECT 1, 'Consulta', 30, '#4F8EF7', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM services WHERE tenant_id = 1 AND name = 'Consulta');

INSERT INTO services (tenant_id, name, duration_minutes, color, active, created_at, updated_at)
SELECT 1, 'Limpieza', 45, '#38B000', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM services WHERE tenant_id = 1 AND name = 'Limpieza');

INSERT INTO services (tenant_id, name, duration_minutes, color, active, created_at, updated_at)
SELECT 1, 'Control', 20, '#FFB703', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM services WHERE tenant_id = 1 AND name = 'Control');

-- Política de cancelación por defecto: 24 horas (en clinics.settings).
UPDATE clinics SET settings = '{"cancellationHours":24}', updated_at = CURRENT_TIMESTAMP
WHERE slug = 'demo';
