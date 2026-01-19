package com.tcon.auth_user_service.auth.security;

import com.tcon.auth_user_service.auth.event.UserEventPublisher;
import com.tcon.auth_user_service.dto.LoginRequest;
import com.tcon.auth_user_service.dto.RegisterRequest;
import com.tcon.auth_user_service.dto.TokenResponse;
import com.tcon.auth_user_service.entity.*;
import com.tcon.auth_user_service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ParentRepository parentRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TwoFactorAuthService twoFactorAuthService;
    private final AuthenticationManager authenticationManager;
    private final UserEventPublisher userEventPublisher;

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        log.info("Starting registration for email: {}", request.getEmail());

        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            log.error("Email already registered: {}", request.getEmail());
            throw new RuntimeException("Email already registered");
        }

        // Check phone number uniqueness
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                log.error("Phone number already registered: {}", request.getPhoneNumber());
                throw new RuntimeException("Phone number already registered");
            }
        }

        // Create user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .isActive(true)
                .isEmailVerified(false)
                .isTwoFactorEnabled(false)
                .isApproved(request.getRole() != User.UserRole.TEACHER) // Teachers need approval
                .build();

        user = userRepository.save(user);
        log.info("User created successfully with ID: {} and role: {}", user.getId(), user.getRole());

        // Create role-specific profile
        createRoleProfile(user, request);

        // Publish user created event (with error handling for Kafka)
        try {
            userEventPublisher.publishUserCreatedEvent(user);
            log.info("User created event published successfully");
        } catch (Exception e) {
            log.warn("Failed to publish user created event (Kafka might be unavailable): {}", e.getMessage());
            // Don't fail registration if event publishing fails
        }

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        log.info("Registration completed successfully for user: {}", user.getEmail());

        return TokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .isApproved(user.getIsApproved())
                .isTwoFactorEnabled(false)
                .requiresTwoFactor(false)
                .build();
    }

    @Transactional
    public TokenResponse login(LoginRequest
                                           request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // Authenticate user
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            log.debug("Authentication successful for: {}", request.getEmail());
        } catch (Exception e) {
            log.error("Authentication failed for: {}", request.getEmail());
            throw new RuntimeException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if account is active
        if (!user.getIsActive()) {
            log.error("Attempt to login to deactivated account: {}", user.getEmail());
            throw new RuntimeException("Account is deactivated");
        }

        // Check if teacher is approved
        if (user.getRole() == User.UserRole.TEACHER && !user.getIsApproved()) {
            log.error("Unapproved teacher login attempt: {}", user.getEmail());
            throw new RuntimeException("Teacher account pending approval");
        }

        // Handle 2FA
        if (user.getIsTwoFactorEnabled()) {
            if (request.getTwoFactorCode() == null || request.getTwoFactorCode().isEmpty()) {
                log.info("2FA required for user: {}", user.getEmail());
                return TokenResponse.builder()
                        .requiresTwoFactor(true)
                        .userId(user.getId())
                        .email(user.getEmail())
                        .build();
            }

            // Validate 2FA code
            boolean isValidCode = twoFactorAuthService.validateCode(
                    user.getTwoFactorSecret(),
                    request.getTwoFactorCode()
            );

            if (!isValidCode) {
                log.error("Invalid 2FA code for user: {}", user.getEmail());
                throw new RuntimeException("Invalid 2FA code");
            }
            log.info("2FA validation successful for user: {}", user.getEmail());
        }

        // Update last login timestamp
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        log.info("Login successful for user: {} ({})", user.getEmail(), user.getRole());

        return TokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .isApproved(user.getIsApproved())
                .isTwoFactorEnabled(user.getIsTwoFactorEnabled())
                .requiresTwoFactor(false)
                .build();
    }

    private void createRoleProfile(User user, RegisterRequest request) {
        log.info("Creating {} profile for user: {}", user.getRole(), user.getId());

        try {
            switch (user.getRole()) {
                case STUDENT -> {
                    StudentProfile studentProfile = StudentProfile.builder()
                            .userId(user.getId())
                            .grade(request.getGrade())
                            .school(request.getSchool())
                            .demoClassesUsed(0)
                            .demoClassesAvailable(3)
                            .build();
                    StudentProfile saved = studentRepository.save(studentProfile);
                    log.info("Student profile created successfully with ID: {}", saved.getId());
                }
                case TEACHER -> {
                    TeacherProfile teacherProfile = TeacherProfile.builder()
                            .userId(user.getId())
                            .bio(request.getBio())
                            .expertise(request.getExpertise())
                            .qualifications(request.getQualifications())
                            .yearsOfExperience(request.getYearsOfExperience())
                            .verificationStatus(TeacherProfile.VerificationStatus.PENDING)
                            .averageRating(0.0)
                            .totalReviews(0)
                            .totalClassesTaught(0)
                            .rescheduleCount(0)
                            .noShowCount(0)
                            .isBlocked(false)
                            .build();
                    TeacherProfile saved = teacherRepository.save(teacherProfile);
                    log.info("Teacher profile created successfully with ID: {}", saved.getId());
                }
                case PARENT -> {
                    ParentProfile parentProfile = ParentProfile.builder()
                            .userId(user.getId())
                            .receiveProgressReports(true)
                            .receiveClassReminders(true)
                            .build();
                    ParentProfile saved = parentRepository.save(parentProfile);
                    log.info("Parent profile created successfully with ID: {}", saved.getId());
                }
                case ADMIN, SUPPORT_STAFF, FINANCE_ADMIN -> {
                    AdminProfile adminProfile = AdminProfile.builder()
                            .userId(user.getId())
                            .adminType(AdminProfile.AdminType.SUPER_ADMIN)
                            .build();
                    AdminProfile saved = adminRepository.save(adminProfile);
                    log.info("Admin profile created successfully with ID: {}", saved.getId());
                }
                default -> {
                    log.error("Unknown role: {}", user.getRole());
                    throw new RuntimeException("Invalid user role");
                }
            }
        } catch (Exception e) {
            log.error("Error creating role profile for user {}: {}", user.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to create user profile: " + e.getMessage());
        }
    }
}
