package com.tcon.auth_user_service.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentBookingAnalyticsClientDto {
    private String studentId;
    private Integer totalClasses;
    private Integer completedClasses;
    private Integer cancelledClasses;
    private Integer totalMinutesLearned;
}
