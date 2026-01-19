package com.tcon.auth_user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentDto {

    private String id;
    private UserProfileDto user;
    private String occupation;
    private String relationshipToStudent;
    private String linkedStudentIds;
    private String preferredContactMethod;
    private Boolean receiveProgressReports;
    private Boolean receiveClassReminders;
}
