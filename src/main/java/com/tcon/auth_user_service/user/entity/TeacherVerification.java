package com.tcon.auth_user_service.user.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "teacher_verifications")
public class TeacherVerification {

    @Id
    private String id;

    @Indexed
    private String teacherUserId;

    private List<String> documentUrls;

    @Builder.Default
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    private String reviewerUserId;
    private String rejectionReason;
    private LocalDateTime reviewedAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
