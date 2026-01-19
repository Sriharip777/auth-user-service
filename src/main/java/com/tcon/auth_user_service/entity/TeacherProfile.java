package com.tcon.auth_user_service.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "teacher_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherProfile {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private String bio;

    private String expertise;

    private String qualifications;

    private Integer yearsOfExperience;

    private String languages;

    private String timezone;

    private String videoIntroUrl;

    private String idProofUrl;

    private String qualificationProofUrl;

    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Builder.Default
    private Double averageRating = 0.0;

    @Builder.Default
    private Integer totalReviews = 0;

    @Builder.Default
    private Integer totalClassesTaught = 0;

    @Builder.Default
    private Integer rescheduleCount = 0;

    @Builder.Default
    private Integer noShowCount = 0;

    @Builder.Default
    private Boolean isBlocked = false;

    private String blockReason;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum VerificationStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}
