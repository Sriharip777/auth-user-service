package com.tcon.auth_user_service.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactDto {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String profilePictureUrl;
}