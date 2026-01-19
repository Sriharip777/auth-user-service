package com.tcon.auth_user_service.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "parent_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentProfile {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private String occupation;

    private String relationshipToStudent;

    private String linkedStudentIds;

    private String preferredContactMethod;

    @Builder.Default
    private Boolean receiveProgressReports = true;

    @Builder.Default
    private Boolean receiveClassReminders = true;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
