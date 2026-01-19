package com.tcon.auth_user_service.dto;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private String userId;
    private String email;
    private String role;
    private Boolean isApproved;
    private Boolean isTwoFactorEnabled;
    private Boolean requiresTwoFactor; // If true, need to provide 2FA code
}
