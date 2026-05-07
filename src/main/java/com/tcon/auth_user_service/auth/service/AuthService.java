package com.tcon.auth_user_service.auth.service;

import com.tcon.auth_user_service.auth.dto.LoginRequest;
import com.tcon.auth_user_service.auth.dto.PasswordChangeRequest;
import com.tcon.auth_user_service.auth.dto.PasswordResetRequest;
import com.tcon.auth_user_service.auth.dto.RegisterRequest;
import com.tcon.auth_user_service.auth.dto.TokenResponse;
import com.tcon.auth_user_service.auth.dto.TwoFactorRequest;
import com.tcon.auth_user_service.auth.dto.UserProfileResponse;
import com.tcon.auth_user_service.auth.security.JwtTokenProvider;
import com.tcon.auth_user_service.auth.security.TwoFactorAuthService;
import com.tcon.auth_user_service.event.UserEventPublisher;
import com.tcon.auth_user_service.user.entity.AdminProfile;
import com.tcon.auth_user_service.user.entity.TeacherProfile;
import com.tcon.auth_user_service.user.entity.TeacherVerification;
import com.tcon.auth_user_service.user.entity.User;
import com.tcon.auth_user_service.user.entity.UserRole;
import com.tcon.auth_user_service.user.entity.UserStatus;
import com.tcon.auth_user_service.user.repository.AdminRepository;
import com.tcon.auth_user_service.user.repository.AdminRoleRepository;
import com.tcon.auth_user_service.user.repository.TeacherRepository;
import com.tcon.auth_user_service.user.repository.TeacherVerificationRepository;
import com.tcon.auth_user_service.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.access.AccessDeniedException;
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

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final TwoFactorAuthService twoFactorAuthService;
    private final PasswordResetService passwordResetService;
    private final UserEventPublisher userEventPublisher;
    private final AdminRoleRepository adminRoleRepository;
    private final TeacherRepository teacherRepository;
    private final TeacherVerificationRepository teacherVerificationRepository;

    private static final List<String> ADMIN_ROLES = Arrays.asList(
            "ADMIN",
            "MODERATOR",
            "MODERATOR_L1",
            "MODERATOR_L2",
            "MODERATOR_L3",
            "FINANCE",
            "FIN_ADMIN_L1",
            "FIN_ADMIN_L2",
            "FIN_ADMIN_L3",
            "FINANCIAL_ADMIN",
            "FINANCIAL_SUPPORT_ADMIN"
    );

    @Transactional
    public TokenResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        if (request.getPhoneNumber() != null &&
                userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number already in use");
        }

        UserStatus initialStatus = request.getRole() == UserRole.TEACHER
                ? UserStatus.PENDING_VERIFICATION
                : UserStatus.ACTIVE;

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .status(initialStatus)
                .emailVerified(false)
                .twoFactorEnabled(false)
                .failedLoginAttempts(0)
                .build();

        user.setEmailVerificationToken(RandomStringUtils.randomAlphanumeric(32));
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusDays(1));

        User savedUser = userRepository.save(user);
        userEventPublisher.publishUserCreated(savedUser);

        if (request.getRole() == UserRole.TEACHER) {
            teacherVerificationRepository.findByTeacherUserId(savedUser.getId())
                    .orElseGet(() -> {
                        TeacherVerification verification = TeacherVerification.builder()
                                .teacherUserId(savedUser.getId())
                                .status("PENDING")
                                .documentUrls(List.of())
                                .build();

                        TeacherVerification savedVerification = teacherVerificationRepository.save(verification);
                        log.info("TeacherVerification auto-created for: {}", savedUser.getId());
                        return savedVerification;
                    });
        }

        log.info("User registered: {} ({})", savedUser.getEmail(), savedUser.getRole());
        return buildTokenResponse(savedUser);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (user.getStatus() == UserStatus.SUSPENDED ||
                user.getStatus() == UserStatus.BANNED ||
                user.getStatus() == UserStatus.DELETED) {
            throw new IllegalStateException("Account is suspended or inactive");
        }

        if (user.isAccountLocked()) {
            throw new IllegalStateException("Account locked until " + user.getLockedUntil());
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.incrementFailedAttempts();
            userRepository.save(user);
            throw new BadCredentialsException("Invalid email or password");
        }

        if (user.getRole() == UserRole.TEACHER) {
            teacherRepository.findByUserId(user.getId())
                    .ifPresent(profile -> {
                        if ("REJECTED".equalsIgnoreCase(profile.getVerificationStatus())) {
                            throw new AccessDeniedException("Your teacher verification was rejected.");
                        }
                    });
        }

        user.resetFailedAttempts();
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("User logged in: {}", user.getEmail());

        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            twoFactorAuthService.generateAndSendCode(user);
            throw new IllegalStateException("TWO_FACTOR_REQUIRED");
        }

        return buildTokenResponse(user);
    }

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

        return buildTokenResponse(user);
    }

    @Transactional
    public TokenResponse registerAdmin(RegisterRequest request) {

        if (request.getRole() == null) {
            throw new IllegalArgumentException("Role is required for admin registration");
        }

        String requestedRole = request.getRole().name();

        boolean isStaticAdminRole = ADMIN_ROLES.contains(requestedRole);

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
                .emailVerified(true)
                .twoFactorEnabled(false)
                .failedLoginAttempts(0)
                .build();

        User savedUser = userRepository.save(user);

        adminRepository.findByUserId(savedUser.getId()).orElseGet(() -> {
            AdminProfile profile = AdminProfile.builder()
                    .userId(savedUser.getId())
                    .roleDescription(savedUser.getRole().name())
                    .superAdmin(savedUser.getRole() == UserRole.ADMIN)
                    .permissions(List.of())
                    .department("ADMIN")
                    .build();
            return adminRepository.save(profile);
        });

        log.info("Admin user created: {} ({})", savedUser.getEmail(), savedUser.getRole());
        return buildTokenResponse(savedUser);
    }

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

    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean phoneExists(String phoneNumber) {
        return phoneNumber != null && userRepository.existsByPhoneNumber(phoneNumber);
    }
}