package com.tcon.auth_user_service.auth.service;


import com.tcon.auth_user_service.auth.dto.*;
import com.tcon.auth_user_service.auth.security.JwtTokenProvider;
import com.tcon.auth_user_service.event.UserEventPublisher;
import com.tcon.auth_user_service.auth.security.TwoFactorAuthService;
import com.tcon.auth_user_service.user.entity.User;
import com.tcon.auth_user_service.user.entity.UserStatus;
import com.tcon.auth_user_service.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final TwoFactorAuthService twoFactorAuthService;
    private final PasswordResetService passwordResetService;
    private final UserEventPublisher userEventPublisher;

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + request.getEmail());
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number already in use");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .failedLoginAttempts(0)
                .twoFactorEnabled(false)
                .build();

        // Email verification token
        user.setEmailVerificationToken(RandomStringUtils.randomAlphanumeric(32));
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusDays(1));

        User savedUser = userRepository.save(user);

        log.info("User registered successfully: {} with role: {}", savedUser.getEmail(), savedUser.getRole());

        // Publish event for other services
        userEventPublisher.publishUserCreated(savedUser);

        // TODO: Send verification email
        // emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getEmailVerificationToken());
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (user.isAccountLocked()) {
            throw new IllegalStateException("Account locked until " + user.getLockedUntil() +
                    " due to too many failed login attempts");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.incrementFailedAttempts();
            userRepository.save(user);
            log.warn("Failed login attempt for user: {}. Attempts: {}",
                    user.getEmail(), user.getFailedLoginAttempts());
            throw new BadCredentialsException("Invalid email or password");
        }

        user.resetFailedAttempts();
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("User logged in successfully: {}", user.getEmail());

        // Check if 2FA is enabled
        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            String code = twoFactorAuthService.generateAndSendCode(user);
            log.debug("2FA code generated for user {}: {}", user.getEmail(), code);

            return LoginResponse.builder()
                    .twoFactorRequired(true)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .build();
        }

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .twoFactorRequired(false)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Transactional
    public LoginResponse verifyTwoFactor(TwoFactorRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            throw new IllegalStateException("2FA not enabled for this user");
        }

        if (!twoFactorAuthService.verifyCode(user, request.getCode())) {
            throw new BadCredentialsException("Invalid 2FA code");
        }

        log.info("2FA verification successful for user: {}", user.getEmail());

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .twoFactorRequired(false)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Transactional
    public void requestPasswordReset(PasswordResetRequest request) {
        passwordResetService.createResetToken(request.getEmail());
    }

    @Transactional
    public void resetPassword(PasswordChangeRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
    }

    @Transactional
    public TokenResponse refreshToken(String refreshToken) {
        String userId = jwtTokenProvider.getUserId(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        log.info("Tokens refreshed for user: {}", user.getEmail());

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .build();
    }
}
