package com.tcon.auth_user_service.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String password;

    private String firstName;

    private String lastName;

    @Indexed(unique = true, sparse = true)
    private String phoneNumber;

    private UserRole role;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Boolean isEmailVerified = false;

    @Builder.Default
    private Boolean isTwoFactorEnabled = false;

    private String twoFactorSecret;

    @Builder.Default
    private Boolean isApproved = true;

    private String profileImageUrl;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime lastLoginAt;

    public enum UserRole {
        STUDENT,
        TEACHER,
        PARENT,
        ADMIN,
        SUPPORT_STAFF,
        FINANCE_ADMIN
    }
}
