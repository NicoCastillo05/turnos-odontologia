package com.turnos.odontologia.application.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource) {
        super(resource + " no encontrado");
    }
}
