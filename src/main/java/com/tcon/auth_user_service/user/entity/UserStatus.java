package com.tcon.auth_user_service.user.entity;

public enum UserStatus {
    PENDING_VERIFICATION,  // Email not verified yet
    ACTIVE,                // Account active and verified
    SUSPENDED,             // Temporarily suspended by admin
    LOCKED,                // Locked due to failed login attempts
    BANNED,                // Permanently banned
    DELETED          // User deactivated their own account
}
