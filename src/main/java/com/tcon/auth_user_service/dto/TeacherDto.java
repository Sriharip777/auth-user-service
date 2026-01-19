package com.tcon.auth_user_service.dto;

import com.tcon.auth_user_service.entity.TeacherProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherDto {

    private String id;
    private UserProfileDto user;
    private String bio;
    private String expertise;
    private String qualifications;
    private Integer yearsOfExperience;
    private String languages;
    private String timezone;
    private String videoIntroUrl;
    private TeacherProfile.VerificationStatus verificationStatus;
    private Double averageRating;
    private Integer totalReviews;
    private Integer totalClassesTaught;
    private Integer rescheduleCount;
    private Integer noShowCount;
    private Boolean isBlocked;
}

