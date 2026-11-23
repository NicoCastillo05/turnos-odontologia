package com.turnos.odontologia.application.exception;

public class AvailabilityConflictException extends RuntimeException {
    private final String code;

    public AvailabilityConflictException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
