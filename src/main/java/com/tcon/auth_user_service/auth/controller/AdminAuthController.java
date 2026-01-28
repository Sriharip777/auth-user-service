package com.tcon.auth_user_service.auth.controller;

import com.tcon.auth_user_service.auth.dto.RegisterRequest;
import com.tcon.auth_user_service.auth.dto.TokenResponse;
import com.tcon.auth_user_service.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AuthService authService;

    /**
     * Admin-only endpoint to create ADMIN / SUPPORT / FINANCE users
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/register")
    public TokenResponse registerAdmin(@Valid @RequestBody RegisterRequest request) {
        return authService.registerAdmin(request);
    }
}