package com.tcon.auth_user_service.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherDto {

    private String id;
    private String userId;

    @NotBlank(message = "Bio is required")
    private String bio;

    @NotNull(message = "Subjects are required")
    private List<String> subjects;

    private List<String> languages;

    @NotNull(message = "Years of experience is required")
    private Integer yearsOfExperience;

    @NotBlank(message = "Qualifications are required")
    private String qualifications;

    @NotNull(message = "Hourly rate is required")
    private Double hourlyRate;

    private Double averageRating;
    private Integer totalReviews;
    private String verificationStatus;
    private Boolean isAvailable;
    private String timezone;
}
