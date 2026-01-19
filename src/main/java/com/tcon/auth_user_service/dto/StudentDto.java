package com.tcon.auth_user_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudentDto {
    private String id;
    private LocalDate dateOfBirth;
    private String grade;
    private String school;
    private String interests;
    private String learningGoals;
    private String preferredLanguage;
    private String timezone;
    private Integer demoClassesUsed;
    private Integer demoClassesAvailable;
}
