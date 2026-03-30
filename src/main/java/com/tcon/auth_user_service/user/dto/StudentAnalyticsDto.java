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
public class StudentAnalyticsDto {
    private String id;
    private String name;
    private String grade;
    private List<String> subjects;
    private Integer hoursLearned;
    private Integer attendance;
    private Integer progress;
    private List<String> issues;
    private String status;
}