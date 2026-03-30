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
public class TeacherAnalyticsDto {
    private String id;
    private String name;
    private String subject;
    private Double rating;
    private Integer students;
    private Integer classes;
    private Double earnings;
    private Integer attendance;
    private List<String> issues;
    private String status;
}