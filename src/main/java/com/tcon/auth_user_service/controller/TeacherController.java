package com.tcon.auth_user_service.controller;


import com.tcon.auth_user_service.dto.TeacherDto;
import com.tcon.auth_user_service.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping("/{userId}")
    public ResponseEntity<TeacherDto> getTeacher(@PathVariable String userId) {
        TeacherDto teacher = teacherService.getTeacherByUserId(userId);
        return ResponseEntity.ok(teacher);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<TeacherDto>> searchTeachers(
            @RequestParam String keyword,
            Pageable pageable) {
        Page<TeacherDto> teachers = teacherService.searchTeachers(keyword, pageable);
        return ResponseEntity.ok(teachers);
    }

    @PostMapping("/{userId}/approve")
    public ResponseEntity<Void> approveTeacher(@PathVariable String userId) {
        teacherService.approveTeacher(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/rating")
    public ResponseEntity<Void> updateRating(@PathVariable String userId, @RequestParam double rating) {
        teacherService.updateRating(userId, rating);
        return ResponseEntity.ok().build();
    }
}
