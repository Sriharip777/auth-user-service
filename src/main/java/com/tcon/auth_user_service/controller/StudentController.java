package com.tcon.auth_user_service.controller;

import com.tcon.auth_user_service.dto.StudentDto;
import com.tcon.auth_user_service.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/{userId}")
    public ResponseEntity<StudentDto> getStudent(@PathVariable String userId) {
        StudentDto student = studentService.getStudentByUserId(userId);
        return ResponseEntity.ok(student);
    }

    @PostMapping("/{userId}/demo-class/use")
    public ResponseEntity<Void> useDemoClass(@PathVariable String userId) {
        studentService.incrementDemoClassUsed(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/demo-class/add")
    public ResponseEntity<Void> addDemoClass(@PathVariable String userId, @RequestParam int count) {
        studentService.addDemoClass(userId, count);
        return ResponseEntity.ok().build();
    }
}
