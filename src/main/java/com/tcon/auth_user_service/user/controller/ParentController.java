package com.tcon.auth_user_service.user.controller;

import com.tcon.auth_user_service.user.dto.ParentDto;
import com.tcon.auth_user_service.user.service.ParentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parent")
@RequiredArgsConstructor
public class ParentController {

    private final ParentService parentService;

    @PostMapping("/profile")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ParentDto> createProfile(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody ParentDto dto) {
        ParentDto created = parentService.createProfile(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ParentDto> getProfile(@AuthenticationPrincipal String userId) {
        ParentDto profile = parentService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ParentDto> updateProfile(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody ParentDto dto) {
        ParentDto updated = parentService.updateProfile(userId, dto);
        return ResponseEntity.ok(updated);
    }
}

