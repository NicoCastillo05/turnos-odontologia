package com.turnos.odontologia.application.booking;

import com.turnos.odontologia.api.dto.publicbooking.ClinicInfoResponse;
import com.turnos.odontologia.api.dto.publicbooking.ClinicInfoResponse.ProfessionalInfo;
import com.turnos.odontologia.api.dto.publicbooking.ClinicInfoResponse.ServiceInfo;
import com.turnos.odontologia.application.agenda.ProfessionalScheduleCodec;
import com.turnos.odontologia.application.agenda.TimeWindow;
import com.turnos.odontologia.application.agenda.WeeklySchedule;
import com.turnos.odontologia.domain.Clinic;
import com.turnos.odontologia.domain.Professional;
import com.turnos.odontologia.domain.Service;
import com.turnos.odontologia.infrastructure.persistence.AppointmentRepository;
import com.turnos.odontologia.infrastructure.persistence.BlockRepository;
import com.turnos.odontologia.infrastructure.persistence.ClinicRepository;
import com.turnos.odontologia.infrastructure.persistence.ProfessionalRepository;
import com.turnos.odontologia.infrastructure.persistence.ServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);
    private static final int SLOT_STEP_MINUTES = 15;

    private final ClinicRepository clinicRepository;
    private final ProfessionalRepository professionalRepository;
    private final ServiceRepository serviceRepository;
    private final AppointmentRepository appointmentRepository;
    private final BlockRepository blockRepository;
    private final ProfessionalScheduleCodec scheduleCodec;

    public BookingService(ClinicRepository clinicRepository,
                          ProfessionalRepository professionalRepository,
                          ServiceRepository serviceRepository,
                          AppointmentRepository appointmentRepository,
                          BlockRepository blockRepository,
                          ProfessionalScheduleCodec scheduleCodec) {
        this.clinicRepository = clinicRepository;
        this.professionalRepository = professionalRepository;
        this.serviceRepository = serviceRepository;
        this.appointmentRepository = appointmentRepository;
        this.blockRepository = blockRepository;
        this.scheduleCodec = scheduleCodec;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ClinicInfoResponse getClinicInfo(String slug) {
        Clinic clinic = clinicRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Clinica no encontrada"));
        Long tenantId = clinic.getTenantId();
        List<ProfessionalInfo> professionals = professionalRepository.findAllByTenantId(tenantId).stream()
                .filter(Professional::isActive)
                .map(p -> new ProfessionalInfo(p.getId(), p.getUser() != null ? p.getUser().getEmail() : "Profesional " + p.getId()))
                .toList();
        List<ServiceInfo> services = serviceRepository.findAllByTenantId(tenantId).stream()
                .filter(com.turnos.odontologia.domain.Service::isActive)
                .map(s -> new ServiceInfo(s.getId(), s.getName(), s.getDurationMinutes()))
                .toList();
        return new ClinicInfoResponse(clinic.getSlug(), clinic.getName(), professionals, services);
    }

    public List<String> availableSlots(String slug, Long professionalId, Long serviceId, LocalDate date) {
        Clinic clinic = clinicRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Clinica no encontrada"));
        Long tenantId = clinic.getTenantId();
        Professional professional = professionalRepository.findByIdAndTenantId(professionalId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Profesional no encontrado"));
        Service service = serviceRepository.findByIdAndTenantId(serviceId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));

        int duration = service.getDurationMinutes();
        WeeklySchedule schedule = scheduleCodec.decode(professional.getSchedule());
        DayOfWeek dow = date.getDayOfWeek();
        List<TimeWindow> windows = schedule.windows().getOrDefault(dow, List.of());

        List<String> result = new ArrayList<>();
        for (TimeWindow w : windows) {
            LocalTime cursor = w.start();
            LocalTime end = w.end();
            while (!cursor.plusMinutes(duration).isAfter(end)) {
                LocalDateTime slotStart = date.atTime(cursor);
                LocalDateTime slotEnd = slotStart.plusMinutes(duration);
                if (!appointmentRepository.existsOverlappingForProfessional(tenantId, professionalId, slotStart, slotEnd, null)
                        && !blockRepository.existsOverlappingForProfessional(tenantId, professionalId, slotStart, slotEnd)) {
                    result.add(slotStart.toString());
                }
                cursor = cursor.plusMinutes(SLOT_STEP_MINUTES);
            }
        }
        log.debug("Slots for {} on {} = {}", professionalId, date, result.size());
        return result;
    }
}
