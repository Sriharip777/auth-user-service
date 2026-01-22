package com.tcon.auth_user_service.auth.dto;


import com.tcon.auth_user_service.user.entity.UserRole;
import com.tcon.auth_user_service.user.entity.UserStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponse {

    private String id;
    private String email;
    private String firstName;
    private String lastName;

    private UserRole role;
    private UserStatus status;

    private Boolean emailVerified;
}
