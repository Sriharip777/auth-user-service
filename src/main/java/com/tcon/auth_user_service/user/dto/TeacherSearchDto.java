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
public class TeacherSearchDto {

    private String subject;
    private List<String> languages;
    private Double minRating;
    private Double maxHourlyRate;
    private Integer minYearsExperience;
    private Boolean availableOnly;
}
