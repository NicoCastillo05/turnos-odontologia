package com.turnos.odontologia.api.dto;

public record RegisterRequest(String email, String password, String role, Long tenantId) {}