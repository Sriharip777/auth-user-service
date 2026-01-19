package com.tcon.auth_user_service.auth.event;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "review-created", groupId = "auth-user-service")
    public void handleReviewCreated(String message) {
        log.info("Received review-created event: {}", message);
    }

    @KafkaListener(topics = "referral-completed", groupId = "auth-user-service")
    public void handleReferralCompleted(String message) {
        log.info("Received referral-completed event: {}", message);
    }
}
