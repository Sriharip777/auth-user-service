package com.tcon.auth_user_service.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherEarningsAnalyticsClientDto {
    private String teacherId;
    private Double earnings;
}