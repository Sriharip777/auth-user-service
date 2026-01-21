package com.tcon.auth_user_service.user.service;

import com.tcon.auth_user_service.user.dto.ParentDto;
import com.tcon.auth_user_service.user.entity.ParentProfile;
import com.tcon.auth_user_service.user.repository.ParentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParentService {

    private final ParentRepository parentRepository;

    @Transactional
    public ParentDto createProfile(String userId, ParentDto dto) {
        if (parentRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("Parent profile already exists for user: " + userId);
        }

        ParentProfile profile = ParentProfile.builder()
                .userId(userId)
                .childUserIds(dto.getChildUserIds())
                .preferredContactTime(dto.getPreferredContactTime())
                .emergencyContact(dto.getEmergencyContact())
                .relationship(dto.getRelationship())
                .build();

        ParentProfile saved = parentRepository.save(profile);
        log.info("Parent profile created for userId: {}", userId);
        return toDto(saved);
    }

    public ParentDto getProfile(String userId) {
        ParentProfile profile = parentRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Parent profile not found for user: " + userId));
        return toDto(profile);
    }

    @Transactional
    public ParentDto updateProfile(String userId, ParentDto dto) {
        ParentProfile profile = parentRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Parent profile not found for user: " + userId));

        profile.setChildUserIds(dto.getChildUserIds());
        profile.setPreferredContactTime(dto.getPreferredContactTime());
        profile.setEmergencyContact(dto.getEmergencyContact());
        profile.setRelationship(dto.getRelationship());

        ParentProfile updated = parentRepository.save(profile);
        log.info("Parent profile updated for userId: {}", userId);
        return toDto(updated);
    }

    private ParentDto toDto(ParentProfile profile) {
        return ParentDto.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .childUserIds(profile.getChildUserIds())
                .preferredContactTime(profile.getPreferredContactTime())
                .emergencyContact(profile.getEmergencyContact())
                .relationship(profile.getRelationship())
                .build();
    }
}
