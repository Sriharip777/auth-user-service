package com.tcon.auth_user_service.dto;

import com.tcon.auth_user_service.entity.AdminProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDto {

    private String id;
    private UserProfileDto user;
    private AdminProfile.AdminType adminType;
    private String department;
    private String permissions;
}

