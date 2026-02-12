package com.tcon.auth_user_service.user.controller;
import com.tcon.auth_user_service.user.dto.AdminDto;
import com.tcon.auth_user_service.user.dto.TeacherVerificationDto;
import com.tcon.auth_user_service.user.dto.UserProfileDto;
import com.tcon.auth_user_service.user.service.AdminService;
import com.tcon.auth_user_service.user.service.TeacherVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    /* =====================================================
       ADMIN PROFILE
       ===================================================== */

    @PostMapping("/profile")
    public ResponseEntity<AdminDto> createProfile(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody AdminDto dto
    ) {
        log.info("Creating admin profile for userId: {}", userId);
        return ResponseEntity.ok(adminService.createProfile(userId, dto));
    }

    @GetMapping("/profile")
    public ResponseEntity<AdminDto> getProfile(
            @AuthenticationPrincipal String userId
    ) {
        return ResponseEntity.ok(adminService.getProfile(userId));
    }

    /* =====================================================
       USER MANAGEMENT
       ===================================================== */

    @GetMapping("/users")
    public ResponseEntity<List<UserProfileDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{userId}/suspend")
    public ResponseEntity<Map<String, String>> suspendUser(
            @PathVariable String userId
    ) {
        adminService.suspendUser(userId);
        return ResponseEntity.ok(Map.of("message", "User suspended successfully"));
    }

    @PutMapping("/users/{userId}/activate")
    public ResponseEntity<Map<String, String>> activateUser(
            @PathVariable String userId
    ) {
        adminService.activateUser(userId);
        return ResponseEntity.ok(Map.of("message", "User activated successfully"));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable String userId
    ) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    /* =====================================================
       TEACHER VERIFICATIONS
       ===================================================== */

    /**
     * ✅ Legacy endpoint (still supported)
     * Used if someone directly calls /pending
     */
    @GetMapping("/verifications/pending")
    public ResponseEntity<List<TeacherVerificationDto>> getPendingVerifications() {
        return ResponseEntity.ok(
                verificationService.getPendingVerifications()
        );
    }

    /**
     * ✅ MAIN ENDPOINT (USED BY FRONTEND)
     * /api/admin/verifications?status=PENDING|APPROVED|REJECTED
     */
    @GetMapping("/verifications")
    public ResponseEntity<List<TeacherVerificationDto>> getVerificationsByStatus(
            @RequestParam(name = "status") String status
    ) {
        log.info("Admin fetching verifications with status: {}", status);
        return ResponseEntity.ok(
                verificationService.getVerificationsByStatus(status)
        );
    }

    /**
     * Approve verification
     */
    @PutMapping("/verifications/{verificationId}/approve")
    public ResponseEntity<TeacherVerificationDto> approveVerification(
            @PathVariable String verificationId,
            @AuthenticationPrincipal String reviewerUserId
    ) {
        return ResponseEntity.ok(
                verificationService.approveVerification(
                        verificationId,
                        reviewerUserId
                )
        );
    }

    /**
     * Reject verification
     */
    @PutMapping("/verifications/{verificationId}/reject")
    public ResponseEntity<TeacherVerificationDto> rejectVerification(
            @PathVariable String verificationId,
            @AuthenticationPrincipal String reviewerUserId,
            @RequestBody Map<String, String> body
    ) {
        return ResponseEntity.ok(
                verificationService.rejectVerification(
                        verificationId,
                        reviewerUserId,
                        body.get("reason")
                )
        );
    }
}