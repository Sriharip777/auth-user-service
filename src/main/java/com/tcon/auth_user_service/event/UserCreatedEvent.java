package com.tcon.auth_user_service.event;

import com.tcon.auth_user_service.user.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent {

    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private UserRole role;
    private LocalDateTime timestamp;
    private String eventType;

    public UserCreatedEvent(String userId, String email, String firstName, String lastName,
                            String phoneNumber, UserRole role) {
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.timestamp = LocalDateTime.now();
        this.eventType = "USER_CREATED";
    }
}
