-- V003: pacientes, odontograma y adjuntos clínicos

CREATE TABLE patients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    dni VARCHAR(20),
    phone VARCHAR(30),
    email VARCHAR(150),
    birth_date DATE,
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- La FK de turnos a pacientes se agrega acá porque patients se crea en esta migración.
ALTER TABLE appointments ADD CONSTRAINT fk_appointments_patient
    FOREIGN KEY (patient_id) REFERENCES patients (id);

CREATE TABLE odontograms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    tooth_code VARCHAR(10) NOT NULL,
    status VARCHAR(40) NOT NULL,
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_odontograms_patient FOREIGN KEY (patient_id) REFERENCES patients (id)
);

CREATE TABLE attachments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    url VARCHAR(500) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_attachments_patient FOREIGN KEY (patient_id) REFERENCES patients (id)
);
