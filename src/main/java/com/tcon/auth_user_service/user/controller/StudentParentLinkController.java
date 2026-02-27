package com.tcon.auth_user_service.user.controller;

import com.tcon.auth_user_service.user.service.ParentStudentLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StudentParentLinkController {

    private final ParentStudentLinkService linkService;

    @GetMapping("/api/students/{studentId}/parents")
    public List<String> getParentIds(@PathVariable String studentId) {
        return linkService.getParentIdsForStudent(studentId);
    }
}
