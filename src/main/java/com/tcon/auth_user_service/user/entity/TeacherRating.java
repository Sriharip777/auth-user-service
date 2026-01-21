package com.tcon.auth_user_service.user.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "teacher_ratings")
public class TeacherRating {

    @Id
    private String id;

    @Indexed
    private String teacherUserId;

    @Indexed
    private String studentUserId;

    private Integer rating; // 1-5
    private String review;
    private String sessionId;

    @CreatedDate
    private LocalDateTime createdAt;
}
