package com.tcon.auth_user_service.auth.controller;

import com.tcon.auth_user_service.auth.dto.*;
import com.tcon.auth_user_service.auth.security.TwoFactorAuthService;
import com.tcon.auth_user_service.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TwoFactorAuthService twoFactorAuthService;

    /**
     * Register user and return tokens + user profile
     */
    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        TokenResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login user and return tokens + user profile
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody LoginRequest request) {

        TokenResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Verify 2FA code and issue tokens
     */
    @PostMapping("/2fa/verify")
    public ResponseEntity<TokenResponse> verifyTwoFactor(
            @Valid @RequestBody TwoFactorRequest request) {

        TokenResponse response = authService.verifyTwoFactor(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Enable 2FA for logged-in user
     */
    @PostMapping("/2fa/enable")
    public ResponseEntity<TwoFactorResponse> enableTwoFactor(
            @AuthenticationPrincipal String userId) {

        String qrUrl = twoFactorAuthService.enableTwoFactor(userId);

        return ResponseEntity.ok(
                TwoFactorResponse.builder()
                        .qrCodeUrl(qrUrl)
                        .message("2FA enabled successfully. Scan the QR code with your authenticator app.")
                        .build()
        );
    }

    /**
     * Disable 2FA for logged-in user
     */
    @PostMapping("/2fa/disable")
    public ResponseEntity<Map<String, String>> disableTwoFactor(
            @AuthenticationPrincipal String userId) {

        twoFactorAuthService.disableTwoFactor(userId);
        return ResponseEntity.ok(Map.of("message", "2FA disabled successfully"));
    }

    /**
     * Request password reset email
     */
    @PostMapping("/password/reset-request")
    public ResponseEntity<Map<String, String>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request) {

        authService.requestPasswordReset(request);
        return ResponseEntity.ok(Map.of("message", "Password reset email sent"));
    }

    /**
     * Reset password using token
     */
    @PostMapping("/password/reset")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody PasswordChangeRequest request) {

        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    /**
     * Refresh access token using refresh token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponse> refreshToken(
            @RequestBody Map<String, String> request) {

        String refreshToken = request.get("refreshToken");
        TokenResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(
                Map.of(
                        "status", "UP",
                        "service", "auth-user-service"
                )
        );
    }
}
