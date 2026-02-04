package com.tcon.auth_user_service.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherProfileResponseDto {

    private TeacherDto teacherProfile;
    private UserProfileDto userDetails;

    // Computed fields for convenience
    public String getDisplayName() {
        if (userDetails == null) {
            return null;
        }

        if (userDetails.getFirstName() != null && userDetails.getLastName() != null) {
            return userDetails.getFirstName() + " " + userDetails.getLastName();
        } else if (userDetails.getFirstName() != null) {
            return userDetails.getFirstName();
        } else if (userDetails.getEmail() != null) {
            return userDetails.getEmail().split("@")[0];
        }

        return null;
    }

    public String getAvatar() {
        if (userDetails != null && userDetails.getProfilePictureUrl() != null) {
            return userDetails.getProfilePictureUrl();
        }

        // Generate avatar URL using DiceBear
        if (teacherProfile != null && teacherProfile.getUserId() != null) {
            return "https://api.dicebear.com/7.x/avataaars/svg?seed=" + teacherProfile.getUserId();
        }

        return null;
    }
}
