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
@Document(collection = "parent_profiles")
public class ParentProfile {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    // ✅ NEW: sparse = true prevents E11000 if parentCode is null
    @Indexed(name = "parentCode", unique = true, sparse = true)
    private String parentCode;  // format: PARE + 4 random digits e.g. PARE7291

    private List<String> childUserIds;
    private String preferredContactTime;
    private String emergencyContact;
    private String relationship;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}