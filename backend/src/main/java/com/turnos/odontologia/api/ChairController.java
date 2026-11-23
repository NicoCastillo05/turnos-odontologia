package com.turnos.odontologia.api;

import com.turnos.odontologia.api.dto.agenda.ChairRequest;
import com.turnos.odontologia.api.dto.agenda.ChairResponse;
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
@RequestMapping("/api/chairs")
@PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','PROFESSIONAL')")
public class ChairController {

    private final AgendaResourceService service;

    public ChairController(AgendaResourceService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ChairResponse create(@Valid @RequestBody ChairRequest request) {
        return service.createChair(request);
    }

    @GetMapping
    public List<ChairResponse> list() {
        return service.listChairs();
    }

    @GetMapping("/{id}")
    public ChairResponse get(@PathVariable Long id) {
        return service.getChair(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ChairResponse update(@PathVariable Long id, @Valid @RequestBody ChairRequest request) {
        return service.updateChair(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        service.deleteChair(id);
    }
}
