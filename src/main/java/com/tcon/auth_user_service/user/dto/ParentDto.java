package com.tcon.auth_user_service.user.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentDto {

    private String id;
    private String userId;
    private List<String> childUserIds;
    private String preferredContactTime;
    private String emergencyContact;
    private String relationship;
}

