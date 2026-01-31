package com.tcon.auth_user_service.user.controller;

import com.tcon.auth_user_service.user.dto.UserProfileDto;
import com.tcon.auth_user_service.user.service.UserSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserSearchService userSearchService;

    /**
     * Get user details by userId (public endpoint for viewing profiles)
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileDto> getUserById(@PathVariable String userId) {
        log.info("Request to get user by ID: {}", userId);
        UserProfileDto user = userSearchService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Get own user details (authenticated endpoint)
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDto> getMyDetails(@AuthenticationPrincipal String userId) {
        log.info("Request to get own user details for userId: {}", userId);
        UserProfileDto user = userSearchService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
}