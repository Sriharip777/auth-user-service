package com.tcon.auth_user_service.user.service;

import com.tcon.auth_user_service.user.dto.ParentDto;
import com.tcon.auth_user_service.user.entity.ParentProfile;
import org.springframework.stereotype.Component;

@Component
public class ParentMapper {

    public ParentDto toDto(ParentProfile entity) {
        if (entity == null) {
            return null;
        }

        return ParentDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .childUserIds(entity.getChildUserIds())
                .preferredContactTime(entity.getPreferredContactTime())
                .emergencyContact(entity.getEmergencyContact())
                .relationship(entity.getRelationship())
                .build();
    }

    public ParentProfile toEntity(ParentDto dto) {
        if (dto == null) {
            return null;
        }

        return ParentProfile.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .childUserIds(dto.getChildUserIds())
                .preferredContactTime(dto.getPreferredContactTime())
                .emergencyContact(dto.getEmergencyContact())
                .relationship(dto.getRelationship())
                .build();
    }
}
