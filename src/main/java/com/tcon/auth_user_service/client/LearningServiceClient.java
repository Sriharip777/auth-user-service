package com.tcon.auth_user_service.client;

import com.tcon.auth_user_service.client.dto.UpdateDemoSettingsRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "learning-management-service")
public interface LearningServiceClient {

    @PutMapping("/api/courses/teacher/{teacherId}/demo-settings")
    void updateTeacherDemoSettings(
            @PathVariable("teacherId") String teacherId,
            @RequestBody UpdateDemoSettingsRequest request
    );
}
