package com.tcon.auth_user_service.user.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateTeacherAreasRequest {
    private List<TeachingAreaDto> teachingAreas;
    private Boolean isAvailable;
}