package com.tcon.auth_user_service.user.controller;

import com.tcon.auth_user_service.user.dto.ContactDto;
import com.tcon.auth_user_service.user.dto.UserProfileDto;
import com.tcon.auth_user_service.user.entity.UserRole;
import com.tcon.auth_user_service.user.service.ContactService;
import com.tcon.auth_user_service.user.service.UserSearchService;
import lombok.Data;
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

    /**
     * =========================================
     * GET ALL STUDENTS (FOR TEACHER USE ONLY)
     * =========================================
     */
    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/students")
    public List<UserProfileDto> getAllStudents() {

        log.info("Teacher requested list of all students");

        return userSearchService.searchByRole(UserRole.STUDENT);
    }

    /**
     * Batch fetch users
     */
    @PostMapping("/batch")
    public ResponseEntity<List<UserProfileDto>> getUsersByIds(
            @RequestBody BatchUserRequest request) {

        log.info("Batch request for {} users", request.getUserIds().size());

        List<UserProfileDto> users =
                userSearchService.getUsersByIds(request.getUserIds());

        return ResponseEntity.ok(users);
    }

    /**
     * Contacts API
     */
    @GetMapping("/contacts")
    public ResponseEntity<List<ContactDto>> getContacts(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRoleStr) {

        log.info("Getting contacts for userId: {}, role: {}", userId, userRoleStr);

        try {

            UserRole role = UserRole.valueOf(userRoleStr.toUpperCase());

            List<ContactDto> contacts =
                    contactService.getContactsForUser(userId, role);

            return ResponseEntity.ok(contacts);

        } catch (IllegalArgumentException e) {

            log.error("Invalid role: {}", userRoleStr);

            return ResponseEntity.badRequest().build();

        } catch (Exception e) {

            log.error("Error getting contacts: {}", e.getMessage(), e);

            return ResponseEntity.status(500).build();
        }
    }

    @Data
    public static class BatchUserRequest {

        private List<String> userIds;

    }
}