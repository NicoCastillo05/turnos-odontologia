package com.turnos.odontologia.api;

import com.turnos.odontologia.api.dto.agenda.BlockRequest;
import com.turnos.odontologia.api.dto.agenda.BlockResponse;
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
@RequestMapping("/api/blocks")
@PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','PROFESSIONAL')")
public class BlockController {

    private final AgendaResourceService service;

    public BlockController(AgendaResourceService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST') or " +
            "(hasRole('PROFESSIONAL') and @agendaAuthorization.canManageBlockRequest(#request))")
    public BlockResponse create(@Valid @RequestBody BlockRequest request) {
        return service.createBlock(request);
    }

    @GetMapping
    public List<BlockResponse> list() {
        return service.listBlocks();
    }

    @GetMapping("/{id}")
    public BlockResponse get(@PathVariable Long id) {
        return service.getBlock(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST') or " +
            "(hasRole('PROFESSIONAL') and @agendaAuthorization.ownsBlock(#id) " +
            "and @agendaAuthorization.canManageBlockRequest(#request))")
    public BlockResponse update(@PathVariable Long id, @Valid @RequestBody BlockRequest request) {
        return service.updateBlock(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST') or " +
            "(hasRole('PROFESSIONAL') and @agendaAuthorization.ownsBlock(#id))")
    public void delete(@PathVariable Long id) {
        service.deleteBlock(id);
    }
}
