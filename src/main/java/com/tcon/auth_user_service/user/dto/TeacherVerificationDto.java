package com.tcon.auth_user_service.user.dto;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherVerificationDto {

    private String id;
    private String teacherUserId;

    @NotNull(message = "Document URLs are required")
    private List<String> documentUrls;

    private String status;
    private String reviewerUserId;
    private String rejectionReason;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}

