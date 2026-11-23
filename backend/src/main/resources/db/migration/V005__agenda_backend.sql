-- V005: soporte auditable para sobreturnos e índices de disponibilidad.

ALTER TABLE appointments
    ADD COLUMN overbooked BOOLEAN DEFAULT FALSE NOT NULL;

ALTER TABLE appointments
    ADD COLUMN overbook_authorized_by_user_id BIGINT;

ALTER TABLE appointments
    ADD CONSTRAINT fk_appointments_overbook_authorized_by
        FOREIGN KEY (overbook_authorized_by_user_id) REFERENCES users (id);

CREATE INDEX idx_appointments_tenant_professional_time
    ON appointments (tenant_id, professional_id, start_at, end_at);

CREATE INDEX idx_appointments_tenant_chair_time
    ON appointments (tenant_id, chair_id, start_at, end_at);

CREATE INDEX idx_blocks_tenant_professional_time
    ON blocks (tenant_id, professional_id, start_at, end_at);

CREATE INDEX idx_blocks_tenant_chair_time
    ON blocks (tenant_id, chair_id, start_at, end_at);
