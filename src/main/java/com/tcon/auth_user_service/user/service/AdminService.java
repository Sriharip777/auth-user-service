package com.tcon.auth_user_service.user.service;

import com.tcon.auth_user_service.user.dto.AdminDto;
import com.tcon.auth_user_service.user.dto.UserProfileDto;
import com.tcon.auth_user_service.user.entity.AdminProfile;
import com.tcon.auth_user_service.user.entity.User;
import com.tcon.auth_user_service.user.entity.UserRole;
import com.tcon.auth_user_service.user.entity.UserStatus;
import com.tcon.auth_user_service.user.repository.AdminRepository;
import com.tcon.auth_user_service.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Admin profile already exists for user: " + userId
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found: " + userId
                ));

        AdminProfile profile = AdminProfile.builder()
                .userId(userId)
                .roleDescription(dto.getRoleDescription() != null ? dto.getRoleDescription() : user.getRole().name())
                .superAdmin(dto.getSuperAdmin() != null ? dto.getSuperAdmin() : user.getRole() == UserRole.ADMIN)
                .permissions(dto.getPermissions() != null ? dto.getPermissions() : List.of())
                .department(dto.getDepartment() != null ? dto.getDepartment() : "ADMIN")
                .build();

        AdminProfile saved = adminRepository.save(profile);
        log.info("Admin profile created for userId: {}", userId);
        return toDto(saved);
    }

    public AdminDto getProfile(String userId) {
        AdminProfile profile = adminRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "User not found: " + userId
                            ));

                    AdminProfile newProfile = AdminProfile.builder()
                            .userId(userId)
                            .roleDescription(user.getRole().name())
                            .superAdmin(user.getRole() == UserRole.ADMIN)
                            .permissions(List.of())
                            .department("ADMIN")
                            .build();

                    AdminProfile saved = adminRepository.save(newProfile);
                    log.info("Auto-created admin profile for userId: {}", userId);
                    return saved;
                });

        return toDto(profile);
    }

    public List<UserProfileDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AdminDto updateProfile(String userId, AdminDto dto) {
        AdminProfile profile = adminRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "User not found: " + userId
                            ));

                    AdminProfile newProfile = AdminProfile.builder()
                            .userId(userId)
                            .roleDescription(user.getRole().name())
                            .superAdmin(user.getRole() == UserRole.ADMIN)
                            .permissions(List.of())
                            .department("ADMIN")
                            .build();

                    return adminRepository.save(newProfile);
                });

        if (dto.getRoleDescription() != null) {
            profile.setRoleDescription(dto.getRoleDescription());
        }
        if (dto.getSuperAdmin() != null) {
            profile.setSuperAdmin(dto.getSuperAdmin());
        }
        if (dto.getPermissions() != null) {
            profile.setPermissions(dto.getPermissions());
        }
        if (dto.getDepartment() != null) {
            profile.setDepartment(dto.getDepartment());
        }

        AdminProfile saved = adminRepository.save(profile);
        log.info("Admin profile updated for userId: {}", userId);
        return toDto(saved);
    }

    @Transactional
    public void suspendUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found: " + userId
                ));

        user.setStatus(UserStatus.SUSPENDED);
        userRepository.save(user);
        log.info("User suspended: {}", userId);
    }

    @Transactional
    public void activateUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found: " + userId
                ));

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        log.info("User activated: {}", userId);
    }

    @Transactional
    public void deleteUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found: " + userId
                ));

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
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}