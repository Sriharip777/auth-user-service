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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "student_profiles")
public class StudentProfile {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    // ✅ NEW: sparse = true prevents E11000 when studentId is null
    @Indexed(name = "studentId", unique = true, sparse = true)
    private String studentId;  // format: STUD + 4 random digits e.g. STUD4829

    private String gradeLevel;
    private String schoolName;
    private LocalDate dateOfBirth;

    @Builder.Default
    private List<String> interests = new ArrayList<>();

    private String bio;
    private String parentId;

    @Builder.Default
    private List<String> enrolledCourses = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}