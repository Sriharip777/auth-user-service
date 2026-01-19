package com.tcon.auth_user_service.controller;

import com.tcon.auth_user_service.dto.AdminDto;
import com.tcon.auth_user_service.entity.StudentProfile;
import com.tcon.auth_user_service.repository.StudentRepository;
import com.tcon.auth_user_service.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final StudentRepository studentRepository;

    @GetMapping("/{userId}")
    public ResponseEntity<AdminDto> getAdmin(@PathVariable String userId) {
        AdminDto admin = adminService.getAdminByUserId(userId);
        return ResponseEntity.ok(admin);
    }

    // Fix missing student profile
    @PostMapping("/fix-student-profile/{userId}")
    public ResponseEntity<String> fixStudentProfile(@PathVariable String userId) {
        StudentProfile profile = StudentProfile.builder()
                .userId(userId)
                .grade("10")
                .school("Delhi Public School")
                .demoClassesUsed(0)
                .demoClassesAvailable(3)
                .build();
        studentRepository.save(profile);
        return ResponseEntity.ok("Student profile created for user: " + userId);
    }
}

