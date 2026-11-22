package com.turnos.odontologia.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey key;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtService(@Value("${jwt.secret:${JWT_SECRET:dev-secret-change-me-please-32chars}}") String secret,
                      @Value("${jwt.access-expiration-ms:900000}") long accessExpirationMs,
                      @Value("${jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String generateAccessToken(Long userId, Long tenantId, String role) {
        return buildToken(userId, tenantId, role, accessExpirationMs, "access");
    }

    public String generateRefreshToken(Long userId, Long tenantId, String role) {
        return buildToken(userId, tenantId, role, refreshExpirationMs, "refresh");
    }

    private String buildToken(Long userId, Long tenantId, String role, long ttl, String type) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claims(Map.of("tenantId", tenantId, "role", role, "type", type))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttl))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public boolean isValid(String token, String expectedType) {
        try {
            Claims c = parse(token);
            return expectedType.equals(c.get("type", String.class));
        } catch (Exception e) {
            log.debug("invalid token: {}", e.getMessage());
            return false;
        }
    }
}