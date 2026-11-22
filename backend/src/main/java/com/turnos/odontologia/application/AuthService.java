package com.turnos.odontologia.application;

import com.turnos.odontologia.domain.User;
import com.turnos.odontologia.infrastructure.persistence.UserRepository;
import com.turnos.odontologia.infrastructure.security.JwtService;
import com.turnos.odontologia.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public Map<String, Object> login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("invalid credentials"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("invalid credentials");
        }
        TenantContext.set(user.getTenantId());
        try {
            String access = jwtService.generateAccessToken(user.getId(), user.getTenantId(), user.getRole());
            String refresh = jwtService.generateRefreshToken(user.getId(), user.getTenantId(), user.getRole());
            return Map.of("accessToken", access, "refreshToken", refresh, "role", user.getRole());
        } finally {
            TenantContext.clear();
        }
    }

    public Map<String, Object> register(String email, String password, String role, Long tenantId) {
        if (userRepository.findByEmailAndTenantId(email, tenantId).isPresent()) {
            throw new IllegalArgumentException("user exists");
        }
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(password));
        u.setRole(role == null ? "receptionist" : role);
        u.setTenantId(tenantId);
        userRepository.save(u);
        log.info("registered user {}", email);
        return Map.of("id", u.getId());
    }

    public Map<String, Object> refresh(String refreshToken) {
        if (!jwtService.isValid(refreshToken, "refresh")) {
            throw new IllegalArgumentException("invalid refresh");
        }
        var c = jwtService.parse(refreshToken);
        Long userId = Long.valueOf(c.getSubject());
        Long tenantId = c.get("tenantId", Long.class);
        String role = c.get("role", String.class);
        String access = jwtService.generateAccessToken(userId, tenantId, role);
        String refresh = jwtService.generateRefreshToken(userId, tenantId, role);
        return Map.of("accessToken", access, "refreshToken", refresh);
    }
}