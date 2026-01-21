package com.tcon.auth_user_service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class UserEventListener {

    @KafkaListener(topics = "user-events", groupId = "auth-user-service-group")
    public void handleUserEvent(UserCreatedEvent event) {
        log.info("Received user event: {} for userId: {}", event.getEventType(), event.getUserId());

        // Process event (e.g., send welcome email, update cache, etc.)
        switch (event.getEventType()) {
            case "USER_CREATED":
                log.info("Processing user creation for: {}", event.getEmail());
                break;
            case "USER_UPDATED":
                log.info("Processing user update for: {}", event.getEmail());
                break;
            case "USER_DELETED":
                log.info("Processing user deletion for userId: {}", event.getUserId());
                break;
            default:
                log.warn("Unknown event type: {}", event.getEventType());
        }
    }
}
