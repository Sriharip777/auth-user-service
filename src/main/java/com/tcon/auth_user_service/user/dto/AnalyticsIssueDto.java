package com.tcon.auth_user_service.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsIssueDto {
    private String id;
    private String type;
    private String user;
    private String description;
    private String date;
    private String status;
}