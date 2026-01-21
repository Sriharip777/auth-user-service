package com.tcon.auth_user_service.user.controller;

import com.tcon.auth_user_service.user.dto.AdminDto;
import com.tcon.auth_user_service.user.dto.TeacherVerificationDto;
import com.tcon.auth_user_service.user.dto.UserProfileDto;
import com.tcon.auth_user_service.user.service.AdminService;
import com.tcon.auth_user_service.user.service.TeacherVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final TeacherVerificationService verificationService;

    @PostMapping("/profile")
    public ResponseEntity<AdminDto> createProfile(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody AdminDto dto) {
        AdminDto created = adminService.createProfile(userId, dto);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/profile")
    public ResponseEntity<AdminDto> getProfile(@AuthenticationPrincipal String userId) {
        AdminDto profile = adminService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserProfileDto>> getAllUsers() {
        List<UserProfileDto> users = adminService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{userId}/suspend")
    public ResponseEntity<Map<String, String>> suspendUser(@PathVariable String userId) {
        adminService.suspendUser(userId);
        return ResponseEntity.ok(Map.of("message", "User suspended successfully"));
    }

    @PutMapping("/users/{userId}/activate")
    public ResponseEntity<Map<String, String>> activateUser(@PathVariable String userId) {
        adminService.activateUser(userId);
        return ResponseEntity.ok(Map.of("message", "User activated successfully"));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    @GetMapping("/verifications/pending")
    public ResponseEntity<List<TeacherVerificationDto>> getPendingVerifications() {
        List<TeacherVerificationDto> verifications = verificationService.getPendingVerifications();
        return ResponseEntity.ok(verifications);
    }

    @PutMapping("/verifications/{verificationId}/approve")
    public ResponseEntity<TeacherVerificationDto> approveVerification(
            @PathVariable String verificationId,
            @AuthenticationPrincipal String reviewerUserId) {
        TeacherVerificationDto result = verificationService.approveVerification(verificationId, reviewerUserId);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/verifications/{verificationId}/reject")
    public ResponseEntity<TeacherVerificationDto> rejectVerification(
            @PathVariable String verificationId,
            @AuthenticationPrincipal String reviewerUserId,
            @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        TeacherVerificationDto result = verificationService.rejectVerification(verificationId, reviewerUserId, reason);
        return ResponseEntity.ok(result);
    }
}
