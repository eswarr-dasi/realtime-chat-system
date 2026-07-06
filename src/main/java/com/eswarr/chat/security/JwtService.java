package com.eswarr.chat.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Issues and validates JWTs used for both the REST login endpoint
 * and the WebSocket handshake authentication.
 */
@Service
public class JwtService {

    @Value("${chat.jwt.secret:this-is-a-demo-secret-key-change-in-production-min-32-chars}")
    private String secret;

    @Value("${chat.jwt.expiration-ms:86400000}") // 24 hours
    private long expirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** Generate a demo JWT for a given display name — no password required for this demo. */
    public String generateToken(String userName) {
        String userId = UUID.randomUUID().toString();
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
            .subject(userId)
            .claim("userName", userName)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(getSigningKey())
            .compact();
    }

    public String extractUserId(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractUserName(String token) {
        return parseClaims(token).get("userName", String.class);
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
