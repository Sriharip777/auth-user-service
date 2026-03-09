package com.tcon.auth_user_service.user.controller;

import com.tcon.auth_user_service.user.dto.StudentDto;
import com.tcon.auth_user_service.user.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.tcon.auth_user_service.user.service.StudentWishlistService;
import org.springframework.web.bind.annotation.*;


import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final StudentWishlistService studentWishlistService;

    // ──────────────────────────────────────────────────────────────
    // Create student profile
    // ──────────────────────────────────────────────────────────────
    @PostMapping("/profile")
    @PreAuthorize("hasAnyRole('STUDENT', 'PARENT')")
    public ResponseEntity<StudentDto> createProfile(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody StudentDto dto) {
        StudentDto created = studentService.createProfile(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ──────────────────────────────────────────────────────────────
    // Get own profile
    // ──────────────────────────────────────────────────────────────
    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('STUDENT', 'PARENT', 'ADMIN')")
    public ResponseEntity<StudentDto> getProfile(
            @AuthenticationPrincipal String userId) {
        StudentDto profile = studentService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }

    // ──────────────────────────────────────────────────────────────
    // Get profile by userId (for teachers/admins)
    // ──────────────────────────────────────────────────────────────
    @GetMapping("/profile/{userId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<StudentDto> getProfileByUserId(
            @PathVariable String userId) {
        StudentDto profile = studentService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }

    // ──────────────────────────────────────────────────────────────
    // Update profile
    // ──────────────────────────────────────────────────────────────
    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('STUDENT', 'PARENT')")
    public ResponseEntity<StudentDto> updateProfile(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody StudentDto dto) {
        StudentDto updated = studentService.updateProfile(userId, dto);
        return ResponseEntity.ok(updated);
    }

    // ──────────────────────────────────────────────────────────────
    // Delete profile
    // ──────────────────────────────────────────────────────────────
    @DeleteMapping("/profile")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> deleteProfile(
            @AuthenticationPrincipal String userId) {
        studentService.deleteProfile(userId);
        return ResponseEntity.noContent().build();
    }

    // ──────────────────────────────────────────────────────────────
    // By grade
    // ──────────────────────────────────────────────────────────────
    @GetMapping("/by-grade/{gradeLevel}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<List<StudentDto>> getStudentsByGrade(
            @PathVariable String gradeLevel) {
        List<StudentDto> students = studentService.getStudentsByGrade(gradeLevel);
        return ResponseEntity.ok(students);
    }

    // ──────────────────────────────────────────────────────────────
    // By interest
    // ──────────────────────────────────────────────────────────────
    @GetMapping("/by-interest/{interest}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<List<StudentDto>> getStudentsByInterest(
            @PathVariable String interest) {
        List<StudentDto> students = studentService.getStudentsByInterest(interest);
        return ResponseEntity.ok(students);
    }

    // ──────────────────────────────────────────────────────────────
    // ✅ By parent (for parent dashboard + Feign client calls)
    // No @PreAuthorize so internal Feign calls from communication-service work
    // ──────────────────────────────────────────────────────────────
    @GetMapping("/by-parent/{parentId}")
    public ResponseEntity<List<StudentDto>> getStudentsByParentId(
            @PathVariable String parentId) {
        log.info("📋 Fetching students for parent: {}", parentId);
        try {
            List<StudentDto> students = studentService.getStudentsByParentId(parentId);
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            log.error("❌ Error fetching students for parent {}: {}", parentId, e.getMessage());
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    // ──────────────────────────────────────────────────────────────
    // ✅ By parent with enriched user details (for parent dashboard UI)
    // No @PreAuthorize so internal Feign calls work too
    // ──────────────────────────────────────────────────────────────
    @GetMapping("/by-parent/{parentId}/details")
    public ResponseEntity<List<StudentDto>> getStudentsByParentIdWithDetails(
            @PathVariable String parentId) {
        log.info("📋 Fetching student details for parent: {}", parentId);
        try {
            List<StudentDto> students = studentService.getStudentsByParentIdWithDetails(parentId);
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            log.error("❌ Error fetching student details for parent {}: {}", parentId, e.getMessage());
            return ResponseEntity.ok(Collections.emptyList());
        }
    }
    // ──────────────────────────────────────────────────────────────
    // Wishlist: toggle
    // ──────────────────────────────────────────────────────────────
    @PostMapping("/wishlist/{courseId}/toggle")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Boolean> toggleWishlist(
            @AuthenticationPrincipal String userId,
            @PathVariable String courseId
    ) {
        boolean nowWishlisted = studentWishlistService.toggleWishlist(userId, courseId);
        return ResponseEntity.ok(nowWishlisted);
    }

    // ──────────────────────────────────────────────────────────────
    // Wishlist: get my courseIds
    // ──────────────────────────────────────────────────────────────
    @GetMapping("/wishlist")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<String>> getMyWishlist(
            @AuthenticationPrincipal String userId
    ) {
        List<String> courseIds = studentWishlistService.getWishlistCourseIds(userId);
        return ResponseEntity.ok(courseIds);
    }
}