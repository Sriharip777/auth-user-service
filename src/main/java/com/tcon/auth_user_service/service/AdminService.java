package com.tcon.auth_user_service.service;

import com.tcon.auth_user_service.dto.AdminDto;
import com.tcon.auth_user_service.entity.AdminProfile;
import com.tcon.auth_user_service.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;

    @Transactional(readOnly = true)
    public AdminDto getAdminByUserId(String userId) {
        AdminProfile profile = adminRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Admin profile not found"));

        return mapToDto(profile);
    }

    private AdminDto mapToDto(AdminProfile profile) {
        return AdminDto.builder()
                .id(profile.getId())
                .adminType(profile.getAdminType())
                .department(profile.getDepartment())
                .permissions(profile.getPermissions())
                .build();
    }
}
