package com.turnos.odontologia.api;

import com.turnos.odontologia.api.dto.agenda.ProfessionalRequest;
import com.turnos.odontologia.api.dto.agenda.ProfessionalResponse;
import com.turnos.odontologia.application.agenda.AgendaResourceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/professionals")
@PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','PROFESSIONAL')")
public class ProfessionalController {

    private final AgendaResourceService service;

    public ProfessionalController(AgendaResourceService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ProfessionalResponse create(@Valid @RequestBody ProfessionalRequest request) {
        return service.createProfessional(request);
    }

    @GetMapping
    public List<ProfessionalResponse> list() {
        return service.listProfessionals();
    }

    @GetMapping("/{id}")
    public ProfessionalResponse get(@PathVariable Long id) {
        return service.getProfessional(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProfessionalResponse update(@PathVariable Long id, @Valid @RequestBody ProfessionalRequest request) {
        return service.updateProfessional(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        service.deleteProfessional(id);
    }
}
