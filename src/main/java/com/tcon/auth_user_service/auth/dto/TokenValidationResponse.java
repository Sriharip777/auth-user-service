package com.tcon.auth_user_service.auth.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationResponse {
    private String userId;
    private String email;
    private String role;      // "ADMIN", "TEACHER", "STUDENT", "PARENT"
    private boolean valid;
}
