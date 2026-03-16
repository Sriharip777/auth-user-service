package com.tcon.auth_user_service.user.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTeacherRequest {
    private String bio;
    private List<String> subjects;
    private List<String> languages;
    private Integer yearsOfExperience;
    private String qualifications;
    private Double hourlyRate;
    private Boolean isAvailable;
    private String timezone;
    private List<TeachingAreaDto> teachingAreas; // ✅ all optional, no @NotNull
}