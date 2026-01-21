package com.tcon.auth_user_service.event;


import com.tcon.auth_user_service.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpUserEventPublisher extends UserEventPublisher {

    public NoOpUserEventPublisher() {
        super(null); // No KafkaTemplate needed
    }

    @Override
    public void publishUserCreated(User user) {
        log.debug("Kafka disabled - Skipping UserCreatedEvent for userId: {}", user.getId());
    }

    @Override
    public void publishUserUpdated(User user) {
        log.debug("Kafka disabled - Skipping UserUpdatedEvent for userId: {}", user.getId());
    }

    @Override
    public void publishUserDeleted(String userId) {
        log.debug("Kafka disabled - Skipping UserDeletedEvent for userId: {}", userId);
    }
}
