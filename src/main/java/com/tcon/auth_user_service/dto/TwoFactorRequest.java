package com.tcon.auth_user_service.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Code is required")
    private String code;
}
