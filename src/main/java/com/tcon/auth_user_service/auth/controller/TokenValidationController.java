package com.tcon.auth_user_service.auth.controller;

import com.tcon.auth_user_service.auth.dto.TokenValidationResponse;
import com.tcon.auth_user_service.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class TokenValidationController {

    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/validate-token")
    public ResponseEntity<TokenValidationResponse> validateToken(
            @RequestHeader("Authorization") String authHeader) {

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(
                    TokenValidationResponse.builder().valid(false).build());
        }

        String token = authHeader.substring(7).trim().replaceAll("\\s+", "");

        try {
            if (jwtTokenProvider.isTokenExpired(token)) {
                log.warn("Token is expired");
                return ResponseEntity.status(401).body(
                        TokenValidationResponse.builder().valid(false).build());
            }

            String userId = jwtTokenProvider.getUserId(token);
            String email  = jwtTokenProvider.getEmail(token);
            String role   = jwtTokenProvider.getRole(token).name(); // "ADMIN","TEACHER" etc.

            log.info("Token valid for userId={} role={}", userId, role);

            return ResponseEntity.ok(TokenValidationResponse.builder()
                    .valid(true)
                    .userId(userId)
                    .email(email)
                    .role(role)
                    .build());

        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return ResponseEntity.status(401).body(
                    TokenValidationResponse.builder().valid(false).build());
        }
    }
}
