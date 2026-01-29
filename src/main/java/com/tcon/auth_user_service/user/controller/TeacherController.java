package com.tcon.auth_user_service.user.controller;

import com.tcon.auth_user_service.user.dto.TeacherDto;
import com.tcon.auth_user_service.user.dto.TeacherSearchDto;
import com.tcon.auth_user_service.user.dto.TeacherVerificationDto;
import com.tcon.auth_user_service.user.repository.TeacherVerificationRepository;
import com.tcon.auth_user_service.user.service.TeacherService;
import com.tcon.auth_user_service.user.service.TeacherVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;
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

}
