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
@Document(collection = "teacher_profiles")
public class TeacherProfile {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private String bio;
    private List<String> subjects;
    private List<String> languages;
    private Integer yearsOfExperience;
    private String qualifications;
    private Double hourlyRate;

    @Builder.Default
    private Double averageRating = 0.0;

    @Builder.Default
    private Integer totalReviews = 0;

    private String verificationStatus; // PENDING, VERIFIED, REJECTED
    private Boolean isAvailable;
    private String timezone;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
