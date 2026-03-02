package com.tcon.auth_user_service.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDemoSettingsRequest {
    private boolean offersDemo;
    private String demoNotes;
}
