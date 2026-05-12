package com.tcon.auth_user_service.user.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(
        name = "learning-management-service",
        url = "${services.learning-management.url:http://localhost:8084}"
)
public interface LearningManagementClient {

    @GetMapping("/api/courses/teacher/{teacherId}/students")
    List<String> getStudentsForTeacher(@PathVariable("teacherId") String teacherId);

    @GetMapping("/api/bookings/teacher/{teacherId}/student-ids")
    List<String> getBookingStudentIdsForTeacher(@PathVariable("teacherId") String teacherId);
}