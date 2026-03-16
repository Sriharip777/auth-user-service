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
public class TeachingAreaDto {
    private String grade;    // e.g. "GRADE_1"
    private String subject;  // e.g. "MATH"
    private List<String> topics;
}