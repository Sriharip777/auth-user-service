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
    private String gradeId;
    private String grade;
    private String subjectId;
    private String subject;
    private List<String> topicIds;
    private List<String> topics;
}