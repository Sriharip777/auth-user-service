package com.tcon.auth_user_service.auth.security;

import com.tcon.auth_user_service.user.entity.User;
import com.tcon.auth_user_service.user.repository.UserRepository;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwoFactorAuthService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();

    @Transactional
    public String enableTwoFactor(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        String secret = key.getKey();

        user.setTwoFactorSecret(secret);
        user.setTwoFactorEnabled(true);
        userRepository.save(user);

        String qrUrl = GoogleAuthenticatorQRGenerator.getOtpAuthURL(
                "TutoringPlatform",
                user.getEmail(),
                key
        );

        log.info("2FA enabled for user: {}", user.getEmail());
        return qrUrl;
    }

    @Transactional
    public void disableTwoFactor(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        userRepository.save(user);

        log.info("2FA disabled for user: {}", user.getEmail());
    }

    public String generateAndSendCode(User user) {
        if (user.getTwoFactorSecret() == null) {
            throw new IllegalStateException("2FA not configured for user: " + user.getEmail());
        }

        int code = googleAuthenticator.getTotpPassword(user.getTwoFactorSecret());
        String codeStr = String.format("%06d", code);

        // Store in Redis for 5 minutes
        String key = "2fa:" + user.getEmail();
        redisTemplate.opsForValue().set(key, codeStr, 5, TimeUnit.MINUTES);

        log.debug("Generated 2FA code for {}: {}", user.getEmail(), codeStr);

        // TODO: Send via SMS/Email service
        // emailService.send2FACode(user.getEmail(), codeStr);

        return codeStr;
    }

    public boolean verifyCode(User user, String code) {
        if (user.getTwoFactorSecret() == null) {
            log.warn("2FA verification attempted but not configured for user: {}", user.getEmail());
            return false;
        }

        // Check Redis first (for email/sms codes)
        String key = "2fa:" + user.getEmail();
        String storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode != null && storedCode.equals(code)) {
            redisTemplate.delete(key);
            log.info("2FA verification successful via stored code for user: {}", user.getEmail());
            return true;
        }

        // Fallback to TOTP verification
        try {
            int codeInt = Integer.parseInt(code);
            boolean isValid = googleAuthenticator.authorize(user.getTwoFactorSecret(), codeInt);

            if (isValid) {
                log.info("2FA verification successful via TOTP for user: {}", user.getEmail());
            } else {
                log.warn("2FA verification failed for user: {}", user.getEmail());
            }

            return isValid;
        } catch (NumberFormatException e) {
            log.error("Invalid 2FA code format for user: {}", user.getEmail());
            return false;
        }
    }
}
