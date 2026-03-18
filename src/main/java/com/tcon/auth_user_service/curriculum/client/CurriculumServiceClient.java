package com.tcon.auth_user_service.curriculum.client;

import com.tcon.auth_user_service.curriculum.dto.GradeDto;
import com.tcon.auth_user_service.curriculum.dto.SubjectDto;
import com.tcon.auth_user_service.curriculum.dto.TopicDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Profile("teaching-area-migration")
@FeignClient(
        name = "curriculum-service",
        url = "${services.learning.url}"   // points to http://localhost:8084
)
public interface CurriculumServiceClient {
    @GetMapping("/api/grades")
    List<GradeDto> getGrades();

    @GetMapping("/api/subjects")
    List<SubjectDto> getAllSubjects();

    @GetMapping("/api/topics")
    List<TopicDto> getAllTopics();
}