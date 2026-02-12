package com.tcon.auth_user_service.auth.service;

import com.tcon.auth_user_service.auth.dto.*;
import com.tcon.auth_user_service.auth.security.JwtTokenProvider;
import com.tcon.auth_user_service.auth.security.TwoFactorAuthService;
import com.tcon.auth_user_service.event.UserEventPublisher;
import com.tcon.auth_user_service.user.entity.User;
import com.tcon.auth_user_service.user.entity.UserRole;
import com.tcon.auth_user_service.user.entity.UserStatus;
import com.tcon.auth_user_service.user.repository.AdminRoleRepository;
import com.tcon.auth_user_service.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

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
    private final AdminRoleRepository adminRoleRepository;  // ✅ NEW

    // ✅ ADMIN ROLES - Now includes new financial roles
    private static final List<String> ADMIN_ROLES = Arrays.asList(
            "ADMIN",
            "MODERATOR",
            "FINANCIAL_ADMIN",           // ✅ NEW
            "FINANCIAL_SUPPORT_ADMIN"    // ✅ NEW
    );

    /**
     * Register user and immediately issue tokens
     */
    @Transactional
    public TokenResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        if (request.getPhoneNumber() != null &&
                userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
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
                .twoFactorEnabled(false)
                .failedLoginAttempts(0)
                .build();

        user.setEmailVerificationToken(RandomStringUtils.randomAlphanumeric(32));
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusDays(1));

        User savedUser = userRepository.save(user);
        userEventPublisher.publishUserCreated(savedUser);

        log.info("User registered: {} ({})", savedUser.getEmail(), savedUser.getRole());

        return buildTokenResponse(savedUser);
    }

    /**
     * Login user (2FA-aware)
     */
    @Transactional
    public TokenResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (user.isAccountLocked()) {
            throw new IllegalStateException(
                    "Account locked until " + user.getLockedUntil());
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.incrementFailedAttempts();
            userRepository.save(user);
            throw new BadCredentialsException("Invalid email or password");
        }

        user.resetFailedAttempts();
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("User logged in: {}", user.getEmail());

        // 2FA required
        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            twoFactorAuthService.generateAndSendCode(user);
            throw new IllegalStateException("TWO_FACTOR_REQUIRED");
        }

        return buildTokenResponse(user);
    }

    /**
     * Verify 2FA and issue tokens
     */
    @Transactional
    public TokenResponse verifyTwoFactor(TwoFactorRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            throw new IllegalStateException("2FA not enabled");
        }

        if (!twoFactorAuthService.verifyCode(user, request.getCode())) {
            throw new BadCredentialsException("Invalid 2FA code");
        }

        log.info("2FA verified for {}", user.getEmail());
        return buildTokenResponse(user);
    }

    /**
     * Request password reset
     */
    @Transactional
    public void requestPasswordReset(PasswordResetRequest request) {
        passwordResetService.createResetToken(request.getEmail());
    }

    /**
     * Reset password
     */
    @Transactional
    public void resetPassword(PasswordChangeRequest request) {
        passwordResetService.resetPassword(
                request.getToken(), request.getNewPassword());
    }

    /**
     * Refresh access token
     */
    @Transactional
    public TokenResponse refreshToken(String refreshToken) {

        String userId = jwtTokenProvider.getUserId(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return buildTokenResponse(user);
    }

    /* =========================================================
       🔐 ADMIN REGISTRATION - UPDATED WITH DYNAMIC VALIDATION
       ========================================================= */
    /**
     * Admin-only registration for administrative roles
     * ✅ Now supports: ADMIN, MODERATOR, FINANCIAL_ADMIN, FINANCIAL_SUPPORT_ADMIN
     * ✅ Validates against both hardcoded list AND dynamic admin_roles collection
     */
    @Transactional
    public TokenResponse registerAdmin(RegisterRequest request) {

        if (request.getRole() == null) {
            throw new IllegalArgumentException("Role is required for admin registration");
        }

        String requestedRole = request.getRole().name();

        // ✅ Validate against STATIC admin roles list
        boolean isStaticAdminRole = ADMIN_ROLES.contains(requestedRole);

        // ✅ Validate against DYNAMIC admin_roles collection
        boolean isDynamicAdminRole = adminRoleRepository
                .findByRoleName(requestedRole)
                .map(role -> Boolean.TRUE.equals(role.getIsActive()))
                .orElse(false);

        if (!isStaticAdminRole && !isDynamicAdminRole) {
            throw new IllegalArgumentException(
                    "Invalid role for admin registration: " + requestedRole +
                            ". Allowed roles: " + ADMIN_ROLES
            );
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        if (request.getPhoneNumber() != null &&
                userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
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
                .emailVerified(true)  // ✅ Auto-verify admin emails
                .twoFactorEnabled(false)
                .failedLoginAttempts(0)
                .build();

        User savedUser = userRepository.save(user);

        log.info("✅ Admin user created: {} ({})",
                savedUser.getEmail(), savedUser.getRole());

        return buildTokenResponse(savedUser);
    }

    /**
     * Shared token creation logic (SINGLE SOURCE OF TRUTH)
     */
    private TokenResponse buildTokenResponse(User user) {

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole());

        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenProvider.getAccessTokenExpiry())
                .user(
                        UserProfileResponse.builder()
                                .id(user.getId())
                                .email(user.getEmail())
                                .firstName(user.getFirstName())
                                .lastName(user.getLastName())
                                .role(user.getRole())
                                .status(user.getStatus())
                                .emailVerified(user.getEmailVerified())
                                .build()
                )
                .build();
    }
}
