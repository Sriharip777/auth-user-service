package com.tcon.auth_user_service.user.service;


import com.tcon.auth_user_service.user.dto.AdminDto;
import com.tcon.auth_user_service.user.dto.UserProfileDto;
import com.tcon.auth_user_service.user.entity.AdminProfile;
import com.tcon.auth_user_service.user.entity.User;
import com.tcon.auth_user_service.user.entity.UserStatus;
import com.tcon.auth_user_service.user.repository.AdminRepository;
import com.tcon.auth_user_service.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;

    @Transactional
    public AdminDto createProfile(String userId, AdminDto dto) {
        if (adminRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("Admin profile already exists for user: " + userId);
        }

        AdminProfile profile = AdminProfile.builder()
                .userId(userId)
                .roleDescription(dto.getRoleDescription())
                .superAdmin(dto.getSuperAdmin() != null ? dto.getSuperAdmin() : false)
                .permissions(dto.getPermissions())
                .department(dto.getDepartment())
                .build();

        AdminProfile saved = adminRepository.save(profile);
        log.info("Admin profile created for userId: {}", userId);
        return toDto(saved);
    }

    public AdminDto getProfile(String userId) {
        AdminProfile profile = adminRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Admin profile not found for user: " + userId));
        return toDto(profile);
    }

    public List<UserProfileDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void suspendUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setStatus(UserStatus.SUSPENDED);
        userRepository.save(user);
        log.info("User suspended: {}", userId);
    }

    @Transactional
    public void activateUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        log.info("User activated: {}", userId);
    }

    @Transactional
    public void deleteUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
        log.info("User deleted: {}", userId);
    }

    private AdminDto toDto(AdminProfile profile) {
        return AdminDto.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .roleDescription(profile.getRoleDescription())
                .superAdmin(profile.getSuperAdmin())
                .permissions(profile.getPermissions())
                .department(profile.getDepartment())
                .build();
    }

    private UserProfileDto toUserDto(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .status(user.getStatus())
                .profilePictureUrl(user.getProfilePictureUrl())
                .twoFactorEnabled(user.getTwoFactorEnabled())
                .emailVerified(user.getEmailVerified())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

