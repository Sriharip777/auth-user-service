package com.tcon.auth_user_service.client;


import com.tcon.auth_user_service.client.dto.MonthlyClassStatClientDto;
import com.tcon.auth_user_service.client.dto.StudentBookingAnalyticsClientDto;
import com.tcon.auth_user_service.client.dto.TeacherBookingAnalyticsClientDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(
        name = "learning-management-service",
        contextId = "learningAnalyticsClient"
)
public interface LearningAnalyticsClient {

    @GetMapping("/internal/analytics/teachers")
    List<TeacherBookingAnalyticsClientDto> getTeacherAnalytics();

    @GetMapping("/internal/analytics/students")
    List<StudentBookingAnalyticsClientDto> getStudentAnalytics();

    @GetMapping("/internal/analytics/overview")
    List<MonthlyClassStatClientDto> getOverviewClassStats();
}