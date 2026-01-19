package com.tcon.auth_user_service.service;

import com.tcon.auth_user_service.dto.ParentDto;
import com.tcon.auth_user_service.entity.ParentProfile;
import com.tcon.auth_user_service.repository.ParentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParentService {

    private final ParentRepository parentRepository;

    @Transactional(readOnly = true)
    public ParentDto getParentByUserId(String userId) {
        ParentProfile profile = parentRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Parent profile not found"));

        return mapToDto(profile);
    }

    private ParentDto mapToDto(ParentProfile profile) {
        return ParentDto.builder()
                .id(profile.getId())
                .occupation(profile.getOccupation())
                .relationshipToStudent(profile.getRelationshipToStudent())
                .linkedStudentIds(profile.getLinkedStudentIds())
                .preferredContactMethod(profile.getPreferredContactMethod())
                .receiveProgressReports(profile.getReceiveProgressReports())
                .receiveClassReminders(profile.getReceiveClassReminders())
                .build();
    }
}
