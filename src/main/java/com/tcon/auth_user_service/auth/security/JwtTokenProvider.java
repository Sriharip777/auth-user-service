package com.tcon.auth_user_service.auth.security;

import com.tcon.auth_user_service.user.entity.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long accessTokenValidityInMs;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenValidityInMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        log.info("✅ JwtTokenProvider initialized with secret (length: {})", secret.length());
    }

    public String generateAccessToken(String userId, String email, UserRole role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidityInMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("role", role.name());
        claims.put("type", "access");

        return Jwts.builder()
                .subject(userId)
                .claims(claims)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(String userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenValidityInMs);

        return Jwts.builder()
                .subject(userId)
                .issuedAt(now)
                .expiration(expiry)
                .claim("type", "refresh")
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public Jws<Claims> validateToken(String token) {
        // CRITICAL FIX: Clean token before validation
        String cleanToken = cleanToken(token);

        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(cleanToken);
        } catch (ExpiredJwtException ex) {
            log.error("JWT token expired: {}", ex.getMessage());
            throw new IllegalArgumentException("Token expired", ex);
        } catch (JwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
            throw new IllegalArgumentException("Invalid JWT token", ex);
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            String cleanToken = cleanToken(token);
            Date expiration = validateToken(cleanToken).getPayload().getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    public String getUserId(String token) {
        String cleanToken = cleanToken(token);
        return validateToken(cleanToken).getPayload().getSubject();
    }

    public String getEmail(String token) {
        String cleanToken = cleanToken(token);
        return validateToken(cleanToken).getPayload().get("email", String.class);
    }

    public UserRole getRole(String token) {
        String cleanToken = cleanToken(token);
        String role = validateToken(cleanToken).getPayload().get("role", String.class);
        return UserRole.valueOf(role);
    }

    public long getAccessTokenExpiry() {
        return accessTokenValidityInMs / 1000;
    }

    public long getRefreshTokenExpiry() {
        return refreshTokenValidityInMs / 1000;
    }

    /**
     * CRITICAL: Remove all whitespace from token
     */
    private String cleanToken(String token) {
        if (token == null) {
            return null;
        }
        // Remove all whitespace characters (spaces, tabs, newlines, etc.)
        String cleaned = token.trim().replaceAll("\\s+", "");

        if (!cleaned.equals(token)) {
            log.warn("⚠️ Token contained whitespace! Cleaned from {} to {} chars",
                    token.length(), cleaned.length());
        }

        return cleaned;
    }
}
