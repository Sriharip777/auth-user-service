package com.tcon.auth_user_service.user.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String password;

    private String firstName;

    private String lastName;

    // Fixed: sparse = true allows null values (not all users need phone)
    @Indexed(unique = true, sparse = true)
    private String phoneNumber;

    @Builder.Default
    private UserRole role = UserRole.STUDENT;

    @Builder.Default
    private UserStatus status = UserStatus.PENDING_VERIFICATION;

    private String profilePictureUrl;

    // Two-Factor Authentication
    @Builder.Default
    private Boolean twoFactorEnabled = false;

    private String twoFactorSecret;

    // Security & Login Tracking
    private LocalDateTime lastLoginAt;

    @Builder.Default
    private Integer failedLoginAttempts = 0;

    private LocalDateTime lockedUntil;

    // Password Reset
    private String passwordResetToken;

    private LocalDateTime passwordResetTokenExpiry;

    // Email Verification
    @Builder.Default
    private Boolean emailVerified = false;

    private String emailVerificationToken;

    private LocalDateTime emailVerificationTokenExpiry;

    // Auditing Fields
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String lastModifiedBy;

    // Helper Methods
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return email;
        }
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    public boolean isAccountLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    public void incrementFailedAttempts() {
        this.failedLoginAttempts = (this.failedLoginAttempts == null ? 0 : this.failedLoginAttempts) + 1;

        // Lock account for 30 minutes after 5 failed attempts
        if (this.failedLoginAttempts >= 5) {
            this.lockedUntil = LocalDateTime.now().plusMinutes(30);
            this.status = UserStatus.LOCKED;
        }
    }

    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        if (this.status == UserStatus.LOCKED) {
            this.status = UserStatus.ACTIVE;
        }
    }

    public boolean isEmailVerificationTokenValid() {
        return emailVerificationToken != null &&
                emailVerificationTokenExpiry != null &&
                emailVerificationTokenExpiry.isAfter(LocalDateTime.now());
    }

    public boolean isPasswordResetTokenValid() {
        return passwordResetToken != null &&
                passwordResetTokenExpiry != null &&
                passwordResetTokenExpiry.isAfter(LocalDateTime.now());
    }

    public void markEmailAsVerified() {
        this.emailVerified = true;
        this.emailVerificationToken = null;
        this.emailVerificationTokenExpiry = null;
        if (this.status == UserStatus.PENDING_VERIFICATION) {
            this.status = UserStatus.ACTIVE;
        }
    }
}
