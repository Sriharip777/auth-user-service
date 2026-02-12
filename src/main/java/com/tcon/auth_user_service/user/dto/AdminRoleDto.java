package com.tcon.auth_user_service.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRoleDto {

    private String id;

    @NotBlank(message = "Role name is required")
    private String roleName;

    private String description;
    private Boolean isActive;
    private List<String> allowedPermissions;
    private LocalDateTime createdAt;
    private String createdBy;
}
