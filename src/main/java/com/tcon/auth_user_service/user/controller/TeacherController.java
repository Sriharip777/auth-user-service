package com.tcon.auth_user_service.user.controller;

import com.tcon.auth_user_service.client.LearningServiceClient;
import com.tcon.auth_user_service.client.dto.UpdateDemoSettingsRequest;
import com.tcon.auth_user_service.user.dto.*;
import com.tcon.auth_user_service.user.entity.TeacherProfile;
import com.tcon.auth_user_service.user.repository.TeacherProfileRepository;
import com.tcon.auth_user_service.user.repository.TeacherRepository;
import com.tcon.auth_user_service.user.service.TeacherService;
import com.tcon.auth_user_service.user.service.TeacherVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;
    private final TeacherProfileRepository teacherProfileRepository;
    private final LearningServiceClient learningServiceClient;
private  final TeacherVerificationService submitVerification;
    @PostMapping("/profile")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherDto> createProfile(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody TeacherDto dto) {
        TeacherDto created = teacherService.createProfile(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherDto> getProfile(@AuthenticationPrincipal String userId) {
        TeacherDto profile = teacherService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<TeacherDto> getProfileByUserId(@PathVariable String userId) {
        TeacherDto profile = teacherService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }

    // ✅ NEW ENDPOINT: Get complete teacher profile with user details
    @GetMapping("/profile/{userId}/complete")
    public ResponseEntity<TeacherProfileResponseDto> getCompleteProfile(@PathVariable String userId) {
        TeacherProfileResponseDto completeProfile = teacherService.getCompleteProfile(userId);
        return ResponseEntity.ok(completeProfile);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherDto> updateProfile(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody TeacherDto dto) {
        TeacherDto updated = teacherService.updateProfile(userId, dto);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/search")
    public ResponseEntity<List<TeacherDto>> searchTeachers(@RequestBody TeacherSearchDto searchDto) {
        List<TeacherDto> teachers = teacherService.searchTeachers(searchDto);
        return ResponseEntity.ok(teachers);
    }

    @GetMapping("/search")
    public ResponseEntity<List<TeacherDto>> searchBySubject(@RequestParam String subject) {
        List<TeacherDto> teachers = teacherService.searchBySubject(subject);
        return ResponseEntity.ok(teachers);
    }

    @GetMapping("/top-rated")
    public ResponseEntity<List<TeacherDto>> getTopRatedTeachers() {
        List<TeacherDto> teachers = teacherService.getTopRatedTeachers();
        return ResponseEntity.ok(teachers);
    }

    @PostMapping("/verification/submit")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherVerificationDto> submitVerification(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody TeacherVerificationDto dto) {
        TeacherVerificationDto created = submitVerification.submitVerification(userId, dto);
        return ResponseEntity.ok(created);
    }

    // ✅ NEW: Update demo settings
    @PutMapping("/demo-settings")
    public ResponseEntity<Map<String, Object>> updateDemoSettings(
            @RequestHeader("X-User-Id") String teacherId,
            @RequestBody UpdateDemoSettingsRequest request) {

        log.info("Updating demo settings for teacher: {}", teacherId);

        // ✅ 1. Update TeacherProfile locally
        TeacherProfile profile = teacherProfileRepository.findByUserId(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher profile not found: " + teacherId));

        profile.setOffersDemo(request.isOffersDemo());
        profile.setDemoNotes(request.getDemoNotes());
        teacherProfileRepository.save(profile);

        // ✅ 2. Sync to learning-management-service via Feign
        try {
            learningServiceClient.updateTeacherDemoSettings(teacherId, request);
            log.info("Demo settings synced to learning service for teacher: {}", teacherId);
        } catch (Exception e) {
            log.warn("Could not sync demo settings to learning service: {}", e.getMessage());
            // Non-blocking — profile saved locally even if sync fails
        }

        return ResponseEntity.ok(Map.of(
                "message", "Demo settings updated successfully",
                "offersDemo", request.isOffersDemo()
        ));
    }

    // ✅ NEW: Get demo settings
    @GetMapping("/demo-settings")
    public ResponseEntity<Map<String, Object>> getDemoSettings(
            @RequestHeader("X-User-Id") String teacherId) {

        TeacherProfile profile = teacherProfileRepository.findByUserId(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher profile not found: " + teacherId));

        return ResponseEntity.ok(Map.of(
                "offersDemo", profile.isOffersDemo(),
                "demoNotes", profile.getDemoNotes() != null ? profile.getDemoNotes() : ""
        ));
    }

}
