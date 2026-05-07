package com.tcon.auth_user_service.user.controller;

import com.tcon.auth_user_service.user.dto.*;
import com.tcon.auth_user_service.user.service.AdminAnalyticsService;
import com.tcon.auth_user_service.user.service.AdminService;
import com.tcon.auth_user_service.user.service.TeacherVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final TeacherVerificationService verificationService;
    private final AdminAnalyticsService adminAnalyticsService;

    @PostMapping("/profile")
    public ResponseEntity<AdminDto> createProfile(
            Authentication authentication,
            @Valid @RequestBody AdminDto dto
    ) {
        String userId = authentication.getName();
        log.info("Creating admin profile for userId: {}", userId);
        return ResponseEntity.ok(adminService.createProfile(userId, dto));
    }

    @PutMapping("/teachers/{userId}/teaching-areas")
    public ResponseEntity<Map<String, String>> updateTeacherTeachingAreas(
            @PathVariable String userId,
            @RequestBody UpdateTeacherAreasRequest request
    ) {
        adminService.updateTeacherTeachingAreas(userId, request);
        return ResponseEntity.ok(Map.of("message", "Teacher teaching areas updated successfully"));
    }
    @GetMapping("/profile")
    public ResponseEntity<AdminDto> getProfile(Authentication authentication) {
        String userId = authentication.getName();
        log.info("Fetching admin profile for userId: {}", userId);
        return ResponseEntity.ok(adminService.getProfile(userId));
    }

    @PutMapping("/profile")
    public ResponseEntity<AdminDto> updateProfile(
            Authentication authentication,
            @Valid @RequestBody AdminDto dto
    ) {
        String userId = authentication.getName();
        log.info("Updating admin profile for userId={}", userId);
        return ResponseEntity.ok(adminService.updateProfile(userId, dto));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserProfileDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
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
        return ResponseEntity.ok(verificationService.getPendingVerifications());
    }

    @GetMapping("/verifications")
    public ResponseEntity<List<TeacherVerificationDto>> getVerificationsByStatus(
            @RequestParam(name = "status") String status
    ) {
        log.info("Admin fetching verifications with status: {}", status);
        return ResponseEntity.ok(verificationService.getVerificationsByStatus(status));
    }

    @PutMapping("/verifications/{verificationId}/approve")
    public ResponseEntity<TeacherVerificationDto> approveVerification(
            @PathVariable String verificationId,
            Authentication authentication
    ) {
        String reviewerUserId = authentication.getName();
        return ResponseEntity.ok(
                verificationService.approveVerification(verificationId, reviewerUserId)
        );
    }

    @PutMapping("/verifications/{verificationId}/reject")
    public ResponseEntity<TeacherVerificationDto> rejectVerification(
            @PathVariable String verificationId,
            Authentication authentication,
            @RequestBody Map<String, String> body
    ) {
        String reviewerUserId = authentication.getName();
        return ResponseEntity.ok(
                verificationService.rejectVerification(
                        verificationId,
                        reviewerUserId,
                        body.get("reason")
                )
        );
    }

    @GetMapping("/analytics/overview")
    public ResponseEntity<AdminOverviewDto> getOverview() {
        return ResponseEntity.ok(adminAnalyticsService.getOverview());
    }

    @GetMapping("/analytics/teachers")
    public ResponseEntity<List<TeacherAnalyticsDto>> getTeachers(
            @RequestParam(name = "search", required = false) String search
    ) {
        return ResponseEntity.ok(adminAnalyticsService.getTeachers(search));
    }

    @GetMapping("/analytics/students")
    public ResponseEntity<List<StudentAnalyticsDto>> getStudents(
            @RequestParam(name = "search", required = false) String search
    ) {
        return ResponseEntity.ok(adminAnalyticsService.getStudents(search));
    }

    @GetMapping("/analytics/issues")
    public ResponseEntity<List<AnalyticsIssueDto>> getIssues(
            @RequestParam(name = "search", required = false) String search
    ) {
        return ResponseEntity.ok(adminAnalyticsService.getIssues(search));
    }
}