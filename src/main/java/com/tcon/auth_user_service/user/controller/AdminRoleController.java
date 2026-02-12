package com.tcon.auth_user_service.user.controller;
import com.tcon.auth_user_service.user.dto.AdminRoleDto;
import com.tcon.auth_user_service.user.service.AdminRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/roles")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminRoleController {

    private final AdminRoleService adminRoleService;

    @PostMapping
    public ResponseEntity<AdminRoleDto> createRole(
            @Valid @RequestBody AdminRoleDto dto,
            @AuthenticationPrincipal String userId
    ) {
        log.info("Creating new admin role: {} by userId: {}", dto.getRoleName(), userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminRoleService.createRole(dto, userId));
    }

    @GetMapping
    public ResponseEntity<List<AdminRoleDto>> getAllRoles() {
        return ResponseEntity.ok(adminRoleService.getAllRoles());
    }

    @GetMapping("/active")
    public ResponseEntity<List<AdminRoleDto>> getActiveRoles() {
        return ResponseEntity.ok(adminRoleService.getActiveRoles());
    }

    @PutMapping("/{roleId}")
    public ResponseEntity<AdminRoleDto> updateRole(
            @PathVariable String roleId,
            @Valid @RequestBody AdminRoleDto dto
    ) {
        return ResponseEntity.ok(adminRoleService.updateRole(roleId, dto));
    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<Void> deleteRole(@PathVariable String roleId) {
        adminRoleService.deleteRole(roleId);
        return ResponseEntity.noContent().build();
    }
}
