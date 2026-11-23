package com.turnos.odontologia.api.dto.publicbooking;

import java.util.List;

public record ClinicInfoResponse(String slug, String name, List<ProfessionalInfo> professionals, List<ServiceInfo> services) {
    public record ProfessionalInfo(Long id, String name) {}
    public record ServiceInfo(Long id, String name, int durationMinutes) {}
}
