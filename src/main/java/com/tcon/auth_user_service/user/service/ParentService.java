package com.tcon.auth_user_service.user.service;

import com.tcon.auth_user_service.user.dto.ParentDto;
import com.tcon.auth_user_service.user.entity.ParentProfile;
import com.tcon.auth_user_service.user.entity.User;
import com.tcon.auth_user_service.user.repository.ParentRepository;
import com.tcon.auth_user_service.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParentService {

    private final ParentRepository parentRepository;
    private final UserRepository userRepository;

    private static final Random RANDOM = new Random();

    // ────────────────────────────────────────────────────────────
    // ✅ Auto-generate parentCode: PARE + 4 random digits (unique)
    // ────────────────────────────────────────────────────────────
    private String generateParentCode() {
        String code;
        int attempts = 0;
        do {
            int digits = 1000 + RANDOM.nextInt(9000);
            code = "PARE" + digits;
            attempts++;
            if (attempts > 100) {
                throw new IllegalStateException(
                        "Could not generate unique parentCode after 100 attempts");
            }
        } while (parentRepository.existsByParentCode(code));
        return code;
    }

    // ────────────────────────────────────────────────────────────
    // Create profile
    // ────────────────────────────────────────────────────────────
    @Transactional
    public ParentDto createProfile(String userId, ParentDto dto) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("UserId must not be null or blank");
        }

        return parentRepository.findByUserId(userId)
                .map(existing -> {
                    log.info("ℹ️ Parent profile already exists for userId: {} — returning existing", userId);
                    return toDto(existing);
                })
                .orElseGet(() -> {
                    String parentCode = generateParentCode();

                    ParentProfile profile = ParentProfile.builder()
                            .userId(userId)
                            .parentCode(parentCode)
                            .childUserIds(dto != null && dto.getChildUserIds() != null
                                    ? dto.getChildUserIds() : new ArrayList<>())
                            .preferredContactTime(dto != null ? dto.getPreferredContactTime() : null)
                            .emergencyContact(dto != null ? dto.getEmergencyContact() : null)
                            .relationship(dto != null ? dto.getRelationship() : null)
                            .build();

                    ParentProfile saved = parentRepository.save(profile);
                    log.info("✅ Parent profile created for userId: {} with parentCode: {}", userId, parentCode);
                    return toDto(saved);
                });
    }

    // ────────────────────────────────────────────────────────────
    // ✅ FIXED: Get profile
    // If missing, auto-create profile from user record
    // ────────────────────────────────────────────────────────────
    @Transactional
    public ParentDto getProfile(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UserId must not be null or blank");
        }

        return parentRepository.findByUserId(userId)
                .map(profile -> {
                    log.info("✅ Parent profile found for userId: {}", userId);
                    return toDto(profile);
                })
                .orElseGet(() -> {
                    log.warn("⚠️ Parent profile missing for userId: {} — auto-creating now", userId);

                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "User not found for userId: " + userId));

                    String parentCode = generateParentCode();

                    ParentProfile profile = ParentProfile.builder()
                            .userId(user.getId())
                            .parentCode(parentCode)
                            .childUserIds(new ArrayList<>())
                            .preferredContactTime(null)
                            .emergencyContact(null)
                            .relationship(null)
                            .build();

                    ParentProfile saved = parentRepository.save(profile);
                    log.info("✅ Parent profile auto-created in getProfile for userId: {} with parentCode: {}",
                            userId, parentCode);

                    return toDto(saved);
                });
    }

    // ────────────────────────────────────────────────────────────
    // Update profile
    // ────────────────────────────────────────────────────────────
    @Transactional
    public ParentDto updateProfile(String userId, ParentDto dto) {
        ParentProfile profile = parentRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Parent profile not found for user: " + userId));

        profile.setChildUserIds(dto.getChildUserIds() != null
                ? dto.getChildUserIds() : new ArrayList<>());
        profile.setPreferredContactTime(dto.getPreferredContactTime());
        profile.setEmergencyContact(dto.getEmergencyContact());
        profile.setRelationship(dto.getRelationship());

        ParentProfile updated = parentRepository.save(profile);
        log.info("✅ Parent profile updated for userId: {}", userId);
        return toDto(updated);
    }

    // ────────────────────────────────────────────────────────────
    // Mapping helper
    // ────────────────────────────────────────────────────────────
    private ParentDto toDto(ParentProfile profile) {
        return ParentDto.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .parentCode(profile.getParentCode())
                .childUserIds(profile.getChildUserIds() != null
                        ? profile.getChildUserIds() : new ArrayList<>())
                .preferredContactTime(profile.getPreferredContactTime())
                .emergencyContact(profile.getEmergencyContact())
                .relationship(profile.getRelationship())
                .build();
    }
}