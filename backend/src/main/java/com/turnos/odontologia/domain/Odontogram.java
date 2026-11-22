package com.turnos.odontologia.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "odontograms")
public class Odontogram extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    /** Código FDI del diente (ej. 11, 26). */
    @Column(name = "tooth_code", nullable = false, length = 10)
    private String toothCode;

    @Column(name = "status", nullable = false, length = 40)
    private String status;

    @Column(name = "notes", length = 1000)
    private String notes;

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public String getToothCode() {
        return toothCode;
    }

    public void setToothCode(String toothCode) {
        this.toothCode = toothCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
