package com.tcon.auth_user_service.auth.service;

import com.tcon.auth_user_service.client.NotificationClient;
import com.tcon.auth_user_service.client.dto.EmailNotificationRequest;
import com.tcon.auth_user_service.user.entity.User;
import com.tcon.auth_user_service.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationClient notificationClient;

    @Value("${app.password-reset.token-expiration}")
    private long tokenExpirationMs;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Transactional
    public void createResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // Generate random token
        String token = RandomStringUtils.randomAlphanumeric(32);
        long expirationHours = tokenExpirationMs / 3600000;

        // Save token to user
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(expirationHours));
        userRepository.save(user);

        log.info("Password reset token created for email: {}", email);

        // Create reset link
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        // Prepare email payload
        Map<String, Object> emailPayload = new HashMap<>();
        emailPayload.put("name", user.getFirstName() + " " + user.getLastName());
        emailPayload.put("resetLink", resetLink);

        // Send email via Notification Service
        EmailNotificationRequest emailRequest = EmailNotificationRequest.builder()
                .to(user.getEmail())
                .templateCode("FORGOT_PASSWORD")
                .payload(emailPayload)
                .build();

        notificationClient.sendEmail(emailRequest);

        log.info("✅ Password reset email sent to: {}", email);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        if (user.getPasswordResetTokenExpiry() == null ||
                user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        user.resetFailedAttempts();
        userRepository.save(user);

        log.info("Password reset successful for user: {}", user.getEmail());
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        if (user.getEmailVerificationTokenExpiry() == null ||
                user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification token has expired");
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        userRepository.save(user);

        log.info("Email verified for user: {}", user.getEmail());
    }
}