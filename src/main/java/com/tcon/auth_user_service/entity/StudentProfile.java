package com.tcon.auth_user_service.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "student_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfile {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private LocalDate dateOfBirth;

    private String grade;

    private String school;

    private String interests;

    private String learningGoals;

    private String preferredLanguage;

    private String timezone;

    @Builder.Default
    private Integer demoClassesUsed = 0;

    @Builder.Default
    private Integer demoClassesAvailable = 3;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
