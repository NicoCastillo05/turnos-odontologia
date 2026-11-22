package com.turnos.odontologia.api;

import com.turnos.odontologia.api.dto.LoginRequest;
import com.turnos.odontologia.api.dto.RefreshRequest;
import com.turnos.odontologia.api.dto.RegisterRequest;
import com.turnos.odontologia.application.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest req) {
        return authService.login(req.email(), req.password());
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterRequest req) {
        return authService.register(req.email(), req.password(), req.role(), req.tenantId());
    }

    @PostMapping("/refresh")
    public Map<String, Object> refresh(@RequestBody RefreshRequest req) {
        return authService.refresh(req.refreshToken());
    }
}