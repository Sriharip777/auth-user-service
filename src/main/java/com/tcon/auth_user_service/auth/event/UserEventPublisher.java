package com.tcon.auth_user_service.auth.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcon.auth_user_service.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    @Autowired(required = false)  // Make Kafka optional
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    private static final String USER_CREATED_TOPIC = "user-created";

    public void publishUserCreatedEvent(User user) {
        // Check if Kafka is available
        if (kafkaTemplate == null) {
            log.warn("Kafka is not available. Event not published for user: {}", user.getId());
            return;
        }

        try {
            UserCreatedEvent event = UserCreatedEvent.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .role(user.getRole().name())
                    .createdAt(user.getCreatedAt())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(USER_CREATED_TOPIC, user.getId(), eventJson);

            log.info("Published UserCreatedEvent for user: {}", user.getId());
        } catch (Exception e) {
            log.error("Error publishing UserCreatedEvent: {}", e.getMessage());
            // Don't throw exception - allow registration to complete even if event fails
        }
    }
}
