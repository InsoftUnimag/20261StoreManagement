package com.distribuidoras.inventario.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UUID usuarioId, String nombre, String rol) {
        return Jwts.builder()
                .subject(usuarioId.toString())
                .claim("nombre", nombre)
                .claim("rol", rol)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public UUID extractUsuarioId(String token) {
        return UUID.fromString(extractSubject(token));
    }

    public String extractNombre(String token) {
        return extractClaim(token, "nombre", String.class);
    }

    public String extractRol(String token) {
        return extractClaim(token, "rol", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token JWT inválido: {}", e.getMessage());
            return false;
        }
    }

    private String extractSubject(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    private <T> T extractClaim(String token, String claim, Class<T> type) {
        return Jwts.parser().verifyWith(getSigningKey()).build()
                .parseSignedClaims(token).getPayload().get(claim, type);
    }
}