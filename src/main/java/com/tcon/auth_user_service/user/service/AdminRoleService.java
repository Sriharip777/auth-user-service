package com.tcon.auth_user_service.user.service;
import com.tcon.auth_user_service.user.dto.AdminRoleDto;
import com.tcon.auth_user_service.user.entity.AdminRole;
import com.tcon.auth_user_service.user.repository.AdminRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminRoleService {

    private final AdminRoleRepository adminRoleRepository;

    @Transactional
    public AdminRoleDto createRole(AdminRoleDto dto, String createdBy) {
        if (adminRoleRepository.existsByRoleName(dto.getRoleName())) {
            throw new IllegalArgumentException("Role already exists: " + dto.getRoleName());
        }

        AdminRole role = AdminRole.builder()
                .roleName(dto.getRoleName().toUpperCase())
                .description(dto.getDescription())
                .isActive(true)
                .allowedPermissions(dto.getAllowedPermissions())
                .createdBy(createdBy)
                .build();

        AdminRole saved = adminRoleRepository.save(role);
        log.info("✅ Admin role created: {} by {}", saved.getRoleName(), createdBy);
        return toDto(saved);
    }

    public List<AdminRoleDto> getAllRoles() {
        return adminRoleRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<AdminRoleDto> getActiveRoles() {
        return adminRoleRepository.findByIsActive(true).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AdminRoleDto updateRole(String roleId, AdminRoleDto dto) {
        AdminRole role = adminRoleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));

        role.setDescription(dto.getDescription());
        role.setAllowedPermissions(dto.getAllowedPermissions());
        role.setIsActive(dto.getIsActive());

        AdminRole updated = adminRoleRepository.save(role);
        log.info("✅ Admin role updated: {}", updated.getRoleName());
        return toDto(updated);
    }

    @Transactional
    public void deleteRole(String roleId) {
        AdminRole role = adminRoleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));

        role.setIsActive(false);
        adminRoleRepository.save(role);
        log.info("✅ Admin role deactivated: {}", role.getRoleName());
    }

    private AdminRoleDto toDto(AdminRole role) {
        return AdminRoleDto.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .isActive(role.getIsActive())
                .allowedPermissions(role.getAllowedPermissions())
                .createdAt(role.getCreatedAt())
                .createdBy(role.getCreatedBy())
                .build();
    }
}
