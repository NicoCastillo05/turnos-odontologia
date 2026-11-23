package com.turnos.odontologia.application.agenda;

import com.turnos.odontologia.api.dto.agenda.AppointmentRequest;
import com.turnos.odontologia.api.dto.agenda.AppointmentResponse;
import com.turnos.odontologia.application.exception.AvailabilityConflictException;
import com.turnos.odontologia.application.exception.BusinessValidationException;
import com.turnos.odontologia.application.exception.ResourceNotFoundException;
import com.turnos.odontologia.domain.Appointment;
import com.turnos.odontologia.domain.AppointmentStatus;
import com.turnos.odontologia.domain.Chair;
import com.turnos.odontologia.domain.Patient;
import com.turnos.odontologia.domain.Professional;
import com.turnos.odontologia.domain.User;
import com.turnos.odontologia.infrastructure.persistence.AppointmentRepository;
import com.turnos.odontologia.infrastructure.persistence.BlockRepository;
import com.turnos.odontologia.infrastructure.persistence.ChairRepository;
import com.turnos.odontologia.infrastructure.persistence.PatientRepository;
import com.turnos.odontologia.infrastructure.persistence.ProfessionalRepository;
import com.turnos.odontologia.infrastructure.persistence.ServiceRepository;
import com.turnos.odontologia.infrastructure.persistence.UserRepository;
import com.turnos.odontologia.infrastructure.security.TenantContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final BlockRepository blockRepository;
    private final PatientRepository patientRepository;
    private final ProfessionalRepository professionalRepository;
    private final ChairRepository chairRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final ProfessionalScheduleCodec scheduleCodec;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              BlockRepository blockRepository,
                              PatientRepository patientRepository,
                              ProfessionalRepository professionalRepository,
                              ChairRepository chairRepository,
                              ServiceRepository serviceRepository,
                              UserRepository userRepository,
                              ProfessionalScheduleCodec scheduleCodec) {
        this.appointmentRepository = appointmentRepository;
        this.blockRepository = blockRepository;
        this.patientRepository = patientRepository;
        this.professionalRepository = professionalRepository;
        this.chairRepository = chairRepository;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
        this.scheduleCodec = scheduleCodec;
    }

    public AppointmentResponse create(AppointmentRequest request) {
        Appointment appointment = new Appointment();
        appointment.setTenantId(tenantId());
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        applyAndValidate(appointment, request, null);
        return toResponse(appointmentRepository.save(appointment));
    }

    @Transactional(readOnly = true)
    public AppointmentResponse get(Long id) {
        Appointment appointment = appointment(id);
        requireAccess(appointment);
        return toResponse(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> list(LocalDateTime from, LocalDateTime to, Long professionalId,
                                          Long chairId, AppointmentStatus status) {
        return appointmentRepository.findAllByTenantId(tenantId()).stream()
                .filter(this::canAccess)
                .filter(a -> from == null || !a.getEndAt().isBefore(from))
                .filter(a -> to == null || a.getStartAt().isBefore(to))
                .filter(a -> professionalId == null || a.getProfessional().getId().equals(professionalId))
                .filter(a -> chairId == null || a.getChair().getId().equals(chairId))
                .filter(a -> status == null || a.getStatus() == status)
                .map(this::toResponse)
                .toList();
    }

    public AppointmentResponse update(Long id, AppointmentRequest request) {
        Appointment appointment = appointment(id);
        requireAccess(appointment);
        applyAndValidate(appointment, request, id);
        return toResponse(appointmentRepository.save(appointment));
    }

    public AppointmentResponse updateStatus(Long id, AppointmentStatus status) {
        if (status == null) {
            throw new BusinessValidationException("El estado es obligatorio");
        }
        Appointment appointment = appointment(id);
        requireAccess(appointment);
        appointment.setStatus(status);
        return toResponse(appointmentRepository.save(appointment));
    }

    public void delete(Long id) {
        if (!hasRole("ADMIN")) {
            throw new AccessDeniedException("Solo un administrador puede eliminar turnos");
        }
        appointmentRepository.delete(appointment(id));
    }

    public boolean canAccessAppointment(Long id) {
        return appointmentRepository.findByIdAndTenantId(id, tenantId())
                .map(this::canAccess)
                .orElse(false);
    }

    public boolean ownsProfessional(Long professionalId) {
        Long userId = currentUserId();
        return userId != null && professionalRepository.findByIdAndTenantId(professionalId, tenantId())
                .map(professional -> professional.getUser().getId().equals(userId))
                .orElse(false);
    }

    private void applyAndValidate(Appointment appointment, AppointmentRequest request, Long excludedId) {
        long tenant = tenantId();
        Patient patient = patientRepository.findByIdAndTenantId(request.patientId(), tenant)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente"));
        Professional professional = professionalRepository.findByIdAndTenantId(request.professionalId(), tenant)
                .orElseThrow(() -> new ResourceNotFoundException("Profesional"));
        Chair chair = chairRepository.findByIdAndTenantId(request.chairId(), tenant)
                .orElseThrow(() -> new ResourceNotFoundException("Sillón"));
        com.turnos.odontologia.domain.Service dentalService = serviceRepository
                .findByIdAndTenantId(request.serviceId(), tenant)
                .orElseThrow(() -> new ResourceNotFoundException("Prestación"));

        if (!professional.isActive() || !chair.isActive() || !dentalService.isActive()) {
            throw new BusinessValidationException("Los recursos del turno deben estar activos");
        }
        if (hasRole("PROFESSIONAL") && !professional.getUser().getId().equals(currentUserId())) {
            throw new AccessDeniedException("El profesional solo puede operar sobre su propia agenda");
        }

        LocalDateTime startAt = request.startAt();
        LocalDateTime endAt = startAt.plusMinutes(dentalService.getDurationMinutes());
        validateSchedule(professional, startAt, endAt);
        validateBlocks(tenant, professional.getId(), chair.getId(), startAt, endAt);
        boolean professionalConflict = appointmentRepository.existsOverlappingForProfessional(
                tenant, professional.getId(), startAt, endAt, excludedId);
        boolean chairConflict = appointmentRepository.existsOverlappingForChair(
                tenant, chair.getId(), startAt, endAt, excludedId);
        boolean hasConflict = professionalConflict || chairConflict;

        appointment.setOverbooked(false);
        appointment.setOverbookAuthorizedBy(null);
        if (hasConflict) {
            if (!request.allowOverbooking()) {
                throw conflict(professionalConflict);
            }
            if (!canAuthorizeOverbooking(professional)) {
                throw new AccessDeniedException("El usuario no puede autorizar el sobreturno");
            }
            User authorizer = userRepository.findByIdAndTenantId(currentUserId(), tenant)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario autorizante"));
            appointment.setOverbooked(true);
            appointment.setOverbookAuthorizedBy(authorizer);
        }

        appointment.setPatient(patient);
        appointment.setProfessional(professional);
        appointment.setChair(chair);
        appointment.setService(dentalService);
        appointment.setStartAt(startAt);
        appointment.setEndAt(endAt);
        appointment.setNotes(request.notes());
    }

    private void validateSchedule(Professional professional, LocalDateTime startAt, LocalDateTime endAt) {
        if (!startAt.toLocalDate().equals(endAt.toLocalDate())) {
            throw new BusinessValidationException("El turno debe estar contenido en una ventana de atención");
        }
        boolean contained = scheduleCodec.decode(professional.getSchedule()).windows()
                .getOrDefault(startAt.getDayOfWeek(), List.of()).stream()
                .anyMatch(window -> !startAt.toLocalTime().isBefore(window.start())
                        && !endAt.toLocalTime().isAfter(window.end()));
        if (!contained) {
            throw new BusinessValidationException("El turno debe estar contenido en una ventana de atención");
        }
    }

    private void validateBlocks(long tenant, Long professionalId, Long chairId,
                                LocalDateTime startAt, LocalDateTime endAt) {
        if (blockRepository.existsOverlappingForProfessional(tenant, professionalId, startAt, endAt)) {
            throw new AvailabilityConflictException("PROFESSIONAL_BLOCKED", "El profesional no está disponible");
        }
        if (blockRepository.existsOverlappingForChair(tenant, chairId, startAt, endAt)) {
            throw new AvailabilityConflictException("CHAIR_BLOCKED", "El sillón no está disponible");
        }
    }

    private AvailabilityConflictException conflict(boolean professionalConflict) {
        return professionalConflict
                ? new AvailabilityConflictException("PROFESSIONAL_CONFLICT", "El profesional no está disponible")
                : new AvailabilityConflictException("CHAIR_CONFLICT", "El sillón no está disponible");
    }

    private boolean canAuthorizeOverbooking(Professional professional) {
        return hasRole("ADMIN") || hasRole("RECEPTIONIST")
                || (hasRole("PROFESSIONAL") && professional.getUser().getId().equals(currentUserId()));
    }

    private Appointment appointment(Long id) {
        return appointmentRepository.findByIdAndTenantId(id, tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Turno"));
    }

    private void requireAccess(Appointment appointment) {
        if (!canAccess(appointment)) {
            throw new AccessDeniedException("No puede acceder a este turno");
        }
    }

    private boolean canAccess(Appointment appointment) {
        return !hasRole("PROFESSIONAL")
                || appointment.getProfessional().getUser().getId().equals(currentUserId());
    }

    private long tenantId() {
        Long tenant = TenantContext.get();
        if (tenant == null) {
            throw new IllegalStateException("No hay una clínica autenticada");
        }
        return tenant;
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof Long value ? value : null;
    }

    private boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(appointment.getId(), appointment.getTenantId(),
                appointment.getPatient().getId(), appointment.getProfessional().getId(),
                appointment.getChair().getId(), appointment.getService().getId(),
                appointment.getStartAt(), appointment.getEndAt(), appointment.getStatus(),
                appointment.isOverbooked(), appointment.getOverbookAuthorizedBy() == null
                ? null : appointment.getOverbookAuthorizedBy().getId(), appointment.getNotes());
    }
}
