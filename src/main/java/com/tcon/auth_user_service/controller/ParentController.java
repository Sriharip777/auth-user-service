package com.tcon.auth_user_service.controller;


import com.tcon.auth_user_service.dto.ParentDto;
import com.tcon.auth_user_service.service.ParentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parents")
@RequiredArgsConstructor
public class ParentController {

    private final ParentService parentService;

    @GetMapping("/{userId}")
    public ResponseEntity<ParentDto> getParent(@PathVariable String userId) {
        ParentDto parent = parentService.getParentByUserId(userId);
        return ResponseEntity.ok(parent);
    }
}


