package com.tcon.auth_user_service.auth.dto;



import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenResponse {

    private String accessToken;
    private String refreshToken;

    /**
     * Expiry time in SECONDS
     */
    private Long expiresIn;

    /**
     * Logged-in user profile
     */
    private UserProfileResponse user;
}
