package com.tcon.auth_user_service.user.controller;

import com.tcon.auth_user_service.user.dto.ContactDto;
import com.tcon.auth_user_service.user.dto.UserProfileDto;
import com.tcon.auth_user_service.user.entity.UserRole;
import com.tcon.auth_user_service.user.service.ContactService;
import com.tcon.auth_user_service.user.service.UserSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserSearchService userSearchService;
    private final ContactService contactService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileDto> getUserById(@PathVariable String userId) {
        log.info("Request to get user by ID: {}", userId);
        UserProfileDto user = userSearchService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDto> getMyDetails(@AuthenticationPrincipal String userId) {
        log.info("Request to get own user details for userId: {}", userId);
        UserProfileDto user = userSearchService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    // ✅✅✅ ADD THIS ENTIRE METHOD ✅✅✅
    @PostMapping("/batch")
    public ResponseEntity<List<UserProfileDto>> getUsersByIds(@RequestBody BatchUserRequest request) {
        log.info("📦 Batch request for {} users", request.getUserIds().size());
        List<UserProfileDto> users = userSearchService.getUsersByIds(request.getUserIds());
        log.info("✅ Returning {} user profiles", users.size());
        return ResponseEntity.ok(users);
    }

    // ✅✅✅ ADD THIS INNER CLASS ✅✅✅
    @lombok.Data
    public static class BatchUserRequest {
        private List<String> userIds;
    }

    // ✅ ADD THIS NEW ENDPOINT
    @GetMapping("/contacts")
    public ResponseEntity<List<ContactDto>> getContacts(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRoleStr) {

        log.info("📇 Getting contacts for userId: {}, role: {}", userId, userRoleStr);

        try {
            UserRole role = UserRole.valueOf(userRoleStr.toUpperCase());
            List<ContactDto> contacts = contactService.getContactsForUser(userId, role);
            log.info("✅ Returning {} contacts for {}", contacts.size(), userId);
            return ResponseEntity.ok(contacts);

        } catch (IllegalArgumentException e) {
            log.error("❌ Invalid role: {}", userRoleStr);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("❌ Error getting contacts for {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}