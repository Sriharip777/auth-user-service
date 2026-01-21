package com.tcon.auth_user_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherApprovalEvent {

    private String teacherUserId;
    private String status; // APPROVED, REJECTED
    private String reviewerUserId;
    private LocalDateTime timestamp;
    private String eventType;
}
