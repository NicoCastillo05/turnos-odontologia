-- V001: identidad y tenant (datos 100% ficticios)

CREATE TABLE clinics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    timezone VARCHAR(60) NOT NULL,
    settings VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_clinics_slug UNIQUE (slug)
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(40) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_users_email_tenant UNIQUE (tenant_id, email),
    CONSTRAINT ck_users_role CHECK (role IN ('admin', 'receptionist', 'professional', 'patient'))
);

-- Seed: clinica demo y un usuario por rol por defecto.
INSERT INTO clinics (tenant_id, name, slug, timezone, settings, created_at, updated_at)
SELECT 1, 'Clinica Demo', 'demo', 'America/Argentina/Buenos_Aires',
       '{"cancellationHours":24}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM clinics WHERE slug = 'demo');

UPDATE clinics SET tenant_id = id, updated_at = CURRENT_TIMESTAMP
WHERE slug = 'demo' AND tenant_id <> id;

INSERT INTO users (tenant_id, email, password_hash, role, created_at, updated_at)
SELECT 1, 'admin@demo.test', 'hash-ficticio-no-real', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@demo.test');

INSERT INTO users (tenant_id, email, password_hash, role, created_at, updated_at)
SELECT 1, 'recepcion@demo.test', 'hash-ficticio-no-real', 'receptionist', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'recepcion@demo.test');

INSERT INTO users (tenant_id, email, password_hash, role, created_at, updated_at)
SELECT 1, 'profesional@demo.test', 'hash-ficticio-no-real', 'professional', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'profesional@demo.test');
