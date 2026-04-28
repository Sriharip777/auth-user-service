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

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSearchService {

    private final UserRepository userRepository;

    public List<UserProfileDto> searchByRole(UserRole role) {
        log.debug("Searching users by role: {}", role);

        return userRepository.findByRole(role)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<UserProfileDto> searchByStatus(UserStatus status) {
        log.debug("Searching users by status: {}", status);

        return userRepository.findByStatus(status)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<UserProfileDto> getAllUsers() {
        log.debug("Retrieving all users");

        return userRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<UserProfileDto> getUsersByIds(List<String> userIds) {
        log.info("Fetching {} users by IDs", userIds.size());

        return userRepository.findAllById(userIds)
                .stream()
                .map(user -> {
                    try {
                        return toDto(user);
                    } catch (Exception e) {
                        log.warn("Could not map user {} to DTO: {}", user.getId(), e.getMessage());
                        return null;
                    }
                })
                .filter(dto -> dto != null)
                .toList();
    }

    public UserProfileDto getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found: " + userId));

        return toDto(user);
    }

    public UserProfileDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found with email: " + email));

        return toDto(user);
    }

    public UserProfileDto getUserByPhoneNumber(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found with phone number: " + phoneNumber));

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
                .firstName(user.getFirstName() != null ? user.getFirstName() : "")
                .lastName(user.getLastName() != null ? user.getLastName() : "")
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .status(user.getStatus())
                .profilePictureUrl(user.getProfilePictureUrl())
                .twoFactorEnabled(
                        user.getTwoFactorEnabled() != null
                                ? user.getTwoFactorEnabled()
                                : false
                )
                .emailVerified(
                        user.getEmailVerified() != null
                                ? user.getEmailVerified()
                                : false
                )
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}