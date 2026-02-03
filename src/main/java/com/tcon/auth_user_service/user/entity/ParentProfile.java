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
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "parent_profiles")
public class ParentProfile {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    @Builder.Default  // ← ADD THIS
    private List<String> childUserIds = new ArrayList<>();  // ← ADD = new ArrayList<>()

    private String preferredContactTime;
    private String emergencyContact;
    private String relationship;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
