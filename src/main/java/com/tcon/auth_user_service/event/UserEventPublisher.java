package com.tcon.auth_user_service.event;

import com.tcon.auth_user_service.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class UserEventPublisher {

    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "user-events";

    public void publishUserCreated(User user) {
        if (kafkaTemplate == null) {
            log.debug("Kafka disabled - Skipping UserCreatedEvent for: {}", user.getEmail());
            return;
        }

        UserCreatedEvent event = UserCreatedEvent.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .timestamp(LocalDateTime.now())
                .eventType("USER_CREATED")
                .build();

        try {
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(TOPIC, user.getId(), event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("✅ Event published for: {}", user.getEmail());
                } else {
                    log.error("❌ Event publish failed: {}", ex.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("Exception publishing event: {}", e.getMessage());
        }
    }

    public void publishUserUpdated(User user) {
        if (kafkaTemplate == null) {
            log.debug("Kafka disabled - Skipping UserUpdatedEvent");
            return;
        }

        UserCreatedEvent event = UserCreatedEvent.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .timestamp(LocalDateTime.now())
                .eventType("USER_UPDATED")
                .build();

        try {
            kafkaTemplate.send(TOPIC, user.getId(), event);
            log.info("✅ UserUpdatedEvent published");
        } catch (Exception e) {
            log.error("❌ Event publish failed: {}", e.getMessage());
        }
    }

    public void publishUserDeleted(String userId) {
        if (kafkaTemplate == null) {
            log.debug("Kafka disabled - Skipping UserDeletedEvent");
            return;
        }

        UserCreatedEvent event = UserCreatedEvent.builder()
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .eventType("USER_DELETED")
                .build();

        try {
            kafkaTemplate.send(TOPIC, userId, event);
            log.info("✅ UserDeletedEvent published");
        } catch (Exception e) {
            log.error("❌ Event publish failed: {}", e.getMessage());
        }
    }
}
