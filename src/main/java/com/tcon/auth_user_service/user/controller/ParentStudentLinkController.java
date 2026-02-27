package com.tcon.auth_user_service.user.controller;

import com.tcon.auth_user_service.user.service.ParentStudentLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ParentStudentLinkController {

    private final ParentStudentLinkService linkService;

    @GetMapping("/api/parents/{parentId}/students")
    public List<String> getChildStudentIds(@PathVariable String parentId) {
        return linkService.getChildIdsForParent(parentId);
    }
}
