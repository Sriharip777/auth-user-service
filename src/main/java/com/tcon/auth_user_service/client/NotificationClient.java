package com.tcon.auth_user_service.client;


import com.tcon.auth_user_service.client.dto.EmailNotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationClient {

    private final RestTemplate restTemplate;

    @Value("${services.notification.url}")
    private String notificationServiceUrl;

    /**
     * Send email notification via Notification Service
     */
    public void sendEmail(EmailNotificationRequest request) {
        try {
            String url = notificationServiceUrl + "/api/notifications/email";

            log.info("📧 Sending email notification to: {}", request.getTo());
            log.info("📧 Template: {}", request.getTemplateCode());

            String response = restTemplate.postForObject(
                    url,
                    request,
                    String.class
            );

            log.info("✅ Email notification sent successfully: {}", response);

        } catch (Exception ex) {
            log.error("❌ Failed to send email notification", ex);
            // Don't throw - we don't want password reset to fail if email fails
            // The token is still created and valid
        }
    }
}