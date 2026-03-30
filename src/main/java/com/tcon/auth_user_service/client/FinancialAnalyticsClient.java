package com.tcon.auth_user_service.client;


import com.tcon.auth_user_service.client.dto.MonthlyRevenueStatClientDto;
import com.tcon.auth_user_service.client.dto.TeacherEarningsAnalyticsClientDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(
        name = "financial-service",
        contextId = "financialAnalyticsClient"
)
public interface FinancialAnalyticsClient {

    @GetMapping("/internal/analytics/teachers")
    List<TeacherEarningsAnalyticsClientDto> getTeacherEarnings();

    @GetMapping("/internal/analytics/overview")
    List<MonthlyRevenueStatClientDto> getOverviewRevenue();
}