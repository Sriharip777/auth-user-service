package com.tcon.auth_user_service.user.service;

import com.tcon.auth_user_service.user.dto.UserProfileDto;
import com.tcon.auth_user_service.user.entity.User;
import com.tcon.auth_user_service.user.entity.UserRole;
import com.tcon.auth_user_service.user.entity.UserStatus;
import com.tcon.auth_user_service.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSearchService {

    private final UserRepository userRepository;

    public List<UserProfileDto> searchByRole(UserRole role) {
        log.debug("Searching users by role: {}", role);
        return userRepository.findByRole(role).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<UserProfileDto> searchByStatus(UserStatus status) {
        log.debug("Searching users by status: {}", status);
        return userRepository.findByStatus(status).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<UserProfileDto> getAllUsers() {
        log.debug("Retrieving all users");
        return userRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public UserProfileDto getUserById(String userId) {
        log.debug("Retrieving user by ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new IllegalArgumentException("User not found: " + userId);
                });
        return toDto(user);
    }

    public UserProfileDto getUserByEmail(String email) {
        log.debug("Retrieving user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new IllegalArgumentException("User not found with email: " + email);
                });
        return toDto(user);
    }

    public UserProfileDto getUserByPhoneNumber(String phoneNumber) {
        log.debug("Retrieving user by phone number: {}", phoneNumber);
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> {
                    log.error("User not found with phone number: {}", phoneNumber);
                    return new IllegalArgumentException("User not found with phone number: " + phoneNumber);
                });
        return toDto(user);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByPhoneNumber(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    private UserProfileDto toDto(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                // Remove .fullName(user.getFullName()) - it's computed in DTO
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .status(user.getStatus())
                .profilePictureUrl(user.getProfilePictureUrl())
                .twoFactorEnabled(user.getTwoFactorEnabled())
                .emailVerified(user.getEmailVerified())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

    }

}
