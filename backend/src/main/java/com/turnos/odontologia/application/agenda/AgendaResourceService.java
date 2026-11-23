package com.turnos.odontologia.application.agenda;

import com.turnos.odontologia.api.dto.agenda.BlockRequest;
import com.turnos.odontologia.api.dto.agenda.BlockResponse;
import com.turnos.odontologia.api.dto.agenda.ChairRequest;
import com.turnos.odontologia.api.dto.agenda.ChairResponse;
import com.turnos.odontologia.api.dto.agenda.ProfessionalRequest;
import com.turnos.odontologia.api.dto.agenda.ProfessionalResponse;
import com.turnos.odontologia.api.dto.agenda.ServiceRequest;
import com.turnos.odontologia.api.dto.agenda.ServiceResponse;
import com.turnos.odontologia.application.exception.BusinessValidationException;
import com.turnos.odontologia.application.exception.ResourceNotFoundException;
import com.turnos.odontologia.domain.Block;
import com.turnos.odontologia.domain.Chair;
import com.turnos.odontologia.domain.Professional;
import com.turnos.odontologia.infrastructure.persistence.BlockRepository;
import com.turnos.odontologia.infrastructure.persistence.AppointmentRepository;
import com.turnos.odontologia.infrastructure.persistence.ChairRepository;
import com.turnos.odontologia.infrastructure.persistence.ProfessionalRepository;
import com.turnos.odontologia.infrastructure.persistence.ServiceRepository;
import com.turnos.odontologia.infrastructure.persistence.UserRepository;
import com.turnos.odontologia.infrastructure.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Service
@Transactional
public class AgendaResourceService {

    private final ChairRepository chairRepository;
    private final AppointmentRepository appointmentRepository;
    private final ServiceRepository serviceRepository;
    private final ProfessionalRepository professionalRepository;
    private final BlockRepository blockRepository;
    private final UserRepository userRepository;
    private final ProfessionalScheduleCodec scheduleCodec;

    public AgendaResourceService(
            ChairRepository chairRepository,
            AppointmentRepository appointmentRepository,
            ServiceRepository serviceRepository,
            ProfessionalRepository professionalRepository,
            BlockRepository blockRepository,
            UserRepository userRepository,
            ProfessionalScheduleCodec scheduleCodec) {
        this.chairRepository = chairRepository;
        this.appointmentRepository = appointmentRepository;
        this.serviceRepository = serviceRepository;
        this.professionalRepository = professionalRepository;
        this.blockRepository = blockRepository;
        this.userRepository = userRepository;
        this.scheduleCodec = scheduleCodec;
    }

    public ChairResponse createChair(ChairRequest request) {
        Chair chair = new Chair();
        chair.setTenantId(tenantId());
        apply(chair, request);
        return toResponse(chairRepository.save(chair));
    }

    @Transactional(readOnly = true)
    public List<ChairResponse> listChairs() {
        return chairRepository.findAllByTenantId(tenantId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ChairResponse getChair(Long id) {
        return toResponse(chair(id));
    }

    public ChairResponse updateChair(Long id, ChairRequest request) {
        Chair chair = chair(id);
        apply(chair, request);
        return toResponse(chairRepository.save(chair));
    }

    public void deleteChair(Long id) {
        Chair chair = chair(id);
        long tenant = tenantId();
        if (appointmentRepository.existsByTenantIdAndChairId(tenant, id)
                || blockRepository.existsByTenantIdAndChairId(tenant, id)) {
            chair.setActive(false);
            chairRepository.save(chair);
            return;
        }
        chairRepository.delete(chair);
    }

    public ServiceResponse createService(ServiceRequest request) {
        com.turnos.odontologia.domain.Service service = new com.turnos.odontologia.domain.Service();
        service.setTenantId(tenantId());
        apply(service, request);
        return toResponse(serviceRepository.save(service));
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> listServices() {
        return serviceRepository.findAllByTenantId(tenantId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ServiceResponse getService(Long id) {
        return toResponse(service(id));
    }

    public ServiceResponse updateService(Long id, ServiceRequest request) {
        com.turnos.odontologia.domain.Service service = service(id);
        apply(service, request);
        return toResponse(serviceRepository.save(service));
    }

    public void deleteService(Long id) {
        com.turnos.odontologia.domain.Service service = service(id);
        if (appointmentRepository.existsByTenantIdAndServiceId(tenantId(), id)) {
            service.setActive(false);
            serviceRepository.save(service);
            return;
        }
        serviceRepository.delete(service);
    }

    public ProfessionalResponse createProfessional(ProfessionalRequest request) {
        Professional professional = new Professional();
        professional.setTenantId(tenantId());
        apply(professional, request);
        return toResponse(professionalRepository.save(professional));
    }

    @Transactional(readOnly = true)
    public List<ProfessionalResponse> listProfessionals() {
        return professionalRepository.findAllByTenantId(tenantId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProfessionalResponse getProfessional(Long id) {
        return toResponse(professional(id));
    }

    public ProfessionalResponse updateProfessional(Long id, ProfessionalRequest request) {
        Professional professional = professional(id);
        apply(professional, request);
        return toResponse(professionalRepository.save(professional));
    }

    public void deleteProfessional(Long id) {
        Professional professional = professional(id);
        long tenant = tenantId();
        if (appointmentRepository.existsByTenantIdAndProfessionalId(tenant, id)
                || blockRepository.existsByTenantIdAndProfessionalId(tenant, id)) {
            professional.setActive(false);
            professionalRepository.save(professional);
            return;
        }
        professionalRepository.delete(professional);
    }

    public BlockResponse createBlock(BlockRequest request) {
        validateBlock(request);
        Block block = new Block();
        block.setTenantId(tenantId());
        apply(block, request);
        return toResponse(blockRepository.save(block));
    }

    @Transactional(readOnly = true)
    public List<BlockResponse> listBlocks() {
        return blockRepository.findAllByTenantId(tenantId()).stream()
                .filter(block -> !hasRole("PROFESSIONAL") || ownsBlock(block))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BlockResponse getBlock(Long id) {
        Block block = block(id);
        if (hasRole("PROFESSIONAL") && !ownsBlock(block)) {
            throw new AccessDeniedException("No puede acceder a este bloqueo");
        }
        return toResponse(block);
    }

    public BlockResponse updateBlock(Long id, BlockRequest request) {
        validateBlock(request);
        Block block = block(id);
        apply(block, request);
        return toResponse(blockRepository.save(block));
    }

    public void deleteBlock(Long id) {
        blockRepository.delete(block(id));
    }

    private void apply(Chair chair, ChairRequest request) {
        chair.setName(request.name());
        chair.setActive(request.active());
    }

    private void apply(com.turnos.odontologia.domain.Service service, ServiceRequest request) {
        if (request.durationMinutes() < 1) {
            throw new BusinessValidationException("La duración debe ser mayor que cero");
        }
        service.setName(request.name());
        service.setDurationMinutes(request.durationMinutes());
        service.setColor(request.color());
        service.setActive(request.active());
    }

    private void apply(Professional professional, ProfessionalRequest request) {
        long tenant = tenantId();
        professional.setUser(userRepository.findByIdAndTenantId(request.userId(), tenant)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario")));
        professional.setSpecialty(request.specialty());
        try {
            professional.setSchedule(scheduleCodec.encode(request.schedule()));
        } catch (IllegalArgumentException exception) {
            throw new BusinessValidationException(exception.getMessage());
        }
        professional.setActive(request.active());
    }

    private void apply(Block block, BlockRequest request) {
        block.setProfessional(request.professionalId() == null ? null : professional(request.professionalId()));
        block.setChair(request.chairId() == null ? null : chair(request.chairId()));
        block.setStartAt(request.startAt());
        block.setEndAt(request.endAt());
        block.setReason(request.reason());
    }

    private void validateBlock(BlockRequest request) {
        if (request.professionalId() == null && request.chairId() == null) {
            throw new BusinessValidationException("El bloqueo requiere un profesional o sillón");
        }
        if (request.startAt() == null || request.endAt() == null || !request.startAt().isBefore(request.endAt())) {
            throw new BusinessValidationException("El inicio del bloqueo debe ser anterior al fin");
        }
    }

    private Chair chair(Long id) {
        return chairRepository.findByIdAndTenantId(id, tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Sillón"));
    }

    private com.turnos.odontologia.domain.Service service(Long id) {
        return serviceRepository.findByIdAndTenantId(id, tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Prestación"));
    }

    private Professional professional(Long id) {
        return professionalRepository.findByIdAndTenantId(id, tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Profesional"));
    }

    private Block block(Long id) {
        return blockRepository.findByIdAndTenantId(id, tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Bloqueo"));
    }

    private long tenantId() {
        Long tenant = TenantContext.get();
        if (tenant == null) {
            throw new IllegalStateException("No hay una clínica autenticada");
        }
        return tenant;
    }

    private boolean ownsBlock(Block block) {
        Long userId = currentUserId();
        return userId != null && block.getProfessional() != null
                && block.getProfessional().getUser().getId().equals(userId);
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

    private ChairResponse toResponse(Chair chair) {
        return new ChairResponse(chair.getId(), chair.getTenantId(), chair.getName(), chair.isActive());
    }

    private ServiceResponse toResponse(com.turnos.odontologia.domain.Service service) {
        return new ServiceResponse(service.getId(), service.getTenantId(), service.getName(),
                service.getDurationMinutes(), service.getColor(), service.isActive());
    }

    private ProfessionalResponse toResponse(Professional professional) {
        return new ProfessionalResponse(professional.getId(), professional.getTenantId(), professional.getUser().getId(),
                professional.getSpecialty(), scheduleCodec.decode(professional.getSchedule()), professional.isActive());
    }

    private BlockResponse toResponse(Block block) {
        return new BlockResponse(block.getId(), block.getTenantId(),
                block.getProfessional() == null ? null : block.getProfessional().getId(),
                block.getChair() == null ? null : block.getChair().getId(),
                block.getStartAt(), block.getEndAt(), block.getReason());
    }
}
