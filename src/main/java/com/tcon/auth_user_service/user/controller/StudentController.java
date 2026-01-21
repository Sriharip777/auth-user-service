package com.tcon.auth_user_service.user.controller;

import com.tcon.auth_user_service.user.dto.StudentDto;
import com.tcon.auth_user_service.user.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping("/profile")
    @PreAuthorize("hasAnyRole('STUDENT', 'PARENT')")
    public ResponseEntity<StudentDto> createProfile(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody StudentDto dto) {
        StudentDto created = studentService.createProfile(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('STUDENT', 'PARENT', 'ADMIN')")
    public ResponseEntity<StudentDto> getProfile(@AuthenticationPrincipal String userId) {
        StudentDto profile = studentService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/profile/{userId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<StudentDto> getProfileByUserId(@PathVariable String userId) {
        StudentDto profile = studentService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('STUDENT', 'PARENT')")
    public ResponseEntity<StudentDto> updateProfile(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody StudentDto dto) {
        StudentDto updated = studentService.updateProfile(userId, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/profile")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> deleteProfile(@AuthenticationPrincipal String userId) {
        studentService.deleteProfile(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-grade/{gradeLevel}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<List<StudentDto>> getStudentsByGrade(@PathVariable String gradeLevel) {
        List<StudentDto> students = studentService.getStudentsByGrade(gradeLevel);
        return ResponseEntity.ok(students);
    }

    // New endpoint: Search by interest
    @GetMapping("/by-interest/{interest}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<List<StudentDto>> getStudentsByInterest(@PathVariable String interest) {
        List<StudentDto> students = studentService.getStudentsByInterest(interest);
        return ResponseEntity.ok(students);
    }

    // New endpoint: Get children by parent ID
    @GetMapping("/by-parent/{parentId}")
    @PreAuthorize("hasAnyRole('PARENT', 'ADMIN')")
    public ResponseEntity<List<StudentDto>> getStudentsByParentId(@PathVariable String parentId) {
        List<StudentDto> students = studentService.getStudentsByParentId(parentId);
        return ResponseEntity.ok(students);
    }
}
