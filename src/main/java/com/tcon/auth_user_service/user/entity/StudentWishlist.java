package com.tcon.auth_user_service.user.entity;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "student_wishlists")
public class StudentWishlist {

    @Id
    private String id;

    // same as StudentProfile.userId
    private String userId;

    private String courseId;

    private LocalDateTime createdAt;
}