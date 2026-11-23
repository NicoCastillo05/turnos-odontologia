package com.turnos.odontologia.api;

import com.turnos.odontologia.api.dto.agenda.ServiceRequest;
import com.turnos.odontologia.api.dto.agenda.ServiceResponse;
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
@RequestMapping("/api/services")
@PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','PROFESSIONAL')")
public class ServiceController {

    private final AgendaResourceService service;

    public ServiceController(AgendaResourceService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ServiceResponse create(@Valid @RequestBody ServiceRequest request) {
        return service.createService(request);
    }

    @GetMapping
    public List<ServiceResponse> list() {
        return service.listServices();
    }

    @GetMapping("/{id}")
    public ServiceResponse get(@PathVariable Long id) {
        return service.getService(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ServiceResponse update(@PathVariable Long id, @Valid @RequestBody ServiceRequest request) {
        return service.updateService(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        service.deleteService(id);
    }
}
