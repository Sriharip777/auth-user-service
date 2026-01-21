package com.tcon.auth_user_service.event;

import com.tcon.auth_user_service.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class UserEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "user-events";

    public void publishUserCreated(User user) {
        UserCreatedEvent event = new UserCreatedEvent(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getRole()
        );

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(TOPIC, user.getId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Published UserCreatedEvent for userId: {} to partition: {}",
                        user.getId(), result.getRecordMetadata().partition());
            } else {
                log.error("Failed to publish UserCreatedEvent for userId: {}", user.getId(), ex);
            }
        });
    }

    public void publishUserUpdated(User user) {
        UserCreatedEvent event = UserCreatedEvent.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .timestamp(java.time.LocalDateTime.now())
                .eventType("USER_UPDATED")
                .build();

        kafkaTemplate.send(TOPIC, user.getId(), event);
        log.info("Published UserUpdatedEvent for userId: {}", user.getId());
    }

    public void publishUserDeleted(String userId) {
        UserCreatedEvent event = UserCreatedEvent.builder()
                .userId(userId)
                .timestamp(java.time.LocalDateTime.now())
                .eventType("USER_DELETED")
                .build();

        kafkaTemplate.send(TOPIC, userId, event);
        log.info("Published UserDeletedEvent for userId: {}", userId);
    }
}
