package com.tcon.auth_user_service.user.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "admin_roles")
public class AdminRole {

    @Id
    private String id;

    @Indexed(unique = true)
    private String roleName;  // e.g., "FINANCIAL_ADMIN", "SUPPORT_ADMIN"

    private String description;

    @Builder.Default
    private Boolean isActive = true;

    private List<String> allowedPermissions;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private String createdBy;
}
