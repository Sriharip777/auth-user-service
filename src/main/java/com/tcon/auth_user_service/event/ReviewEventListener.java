package com.tcon.auth_user_service.event;


import com.tcon.auth_user_service.user.service.TeacherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewEventListener {

    private final TeacherService teacherService;

    @KafkaListener(topics = "review-events", groupId = "auth-user-service-group")
    public void handleReviewEvent(Map<String, Object> event) {
        log.info("Received review event: {}", event);

        String teacherUserId = (String) event.get("teacherUserId");
        Number ratingNumber = (Number) event.get("rating");

        if (teacherUserId != null && ratingNumber != null) {
            Double rating = ratingNumber.doubleValue();
            teacherService.updateRating(teacherUserId, rating);
            log.info("Updated rating for teacher: {} with new rating: {}", teacherUserId, rating);
        }
    }
}
