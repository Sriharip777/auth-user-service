package com.tcon.auth_user_service.client;

import com.tcon.auth_user_service.client.dto.MonthlyClassStatClientDto;
import com.tcon.auth_user_service.client.dto.StudentBookingAnalyticsClientDto;
import com.tcon.auth_user_service.client.dto.TeacherBookingAnalyticsClientDto;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LearningAnalyticsService {

    private final LearningAnalyticsClient learningAnalyticsClient;

    public List<TeacherBookingAnalyticsClientDto> getTeacherAnalytics() {
        try {
            List<TeacherBookingAnalyticsClientDto> result = learningAnalyticsClient.getTeacherAnalytics();
            return result != null ? result : new ArrayList<>();
        } catch (FeignException e) {
            log.error("Failed to fetch teacher analytics from LMS. status={}, message={}",
                    e.status(), e.getMessage(), e);
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Unexpected error while fetching teacher analytics: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<StudentBookingAnalyticsClientDto> getStudentAnalytics() {
        try {
            List<StudentBookingAnalyticsClientDto> result = learningAnalyticsClient.getStudentAnalytics();
            return result != null ? result : new ArrayList<>();
        } catch (FeignException e) {
            log.error("Failed to fetch student analytics from LMS. status={}, message={}",
                    e.status(), e.getMessage(), e);
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Unexpected error while fetching student analytics: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<MonthlyClassStatClientDto> getOverviewClassStats() {
        try {
            List<MonthlyClassStatClientDto> result = learningAnalyticsClient.getOverviewClassStats();
            return result != null ? result : new ArrayList<>();
        } catch (FeignException e) {
            log.error("Failed to fetch overview class stats from LMS. status={}, message={}",
                    e.status(), e.getMessage(), e);
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Unexpected error while fetching overview class stats: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}