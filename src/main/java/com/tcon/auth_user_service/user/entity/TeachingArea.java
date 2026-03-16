package com.tcon.auth_user_service.user.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeachingArea {
    private String grade;
    private String subject;
    private List<String> topics;
}