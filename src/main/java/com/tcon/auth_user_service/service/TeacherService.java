package com.tcon.auth_user_service.service;

import com.tcon.auth_user_service.dto.TeacherDto;
import com.tcon.auth_user_service.entity.TeacherProfile;
import com.tcon.auth_user_service.entity.User;
import com.tcon.auth_user_service.repository.TeacherRepository;
import com.tcon.auth_user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public TeacherDto getTeacherByUserId(String userId) {
        TeacherProfile profile = teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Teacher profile not found"));

        return mapToDto(profile);
    }

    @Transactional(readOnly = true)
    public Page<TeacherDto> searchTeachers(String keyword, Pageable pageable) {
        List<TeacherProfile> teachers = teacherRepository.searchTeachers(keyword);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), teachers.size());

        List<TeacherDto> dtoList = teachers.subList(start, end)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, teachers.size());
    }

    @Transactional
    public void approveTeacher(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setIsApproved(true);
        userRepository.save(user);

        TeacherProfile profile = teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Teacher profile not found"));

        profile.setVerificationStatus(TeacherProfile.VerificationStatus.APPROVED);
        teacherRepository.save(profile);
    }

    @Transactional
    public void updateRating(String userId, double newRating) {
        TeacherProfile profile = teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Teacher profile not found"));

        int totalReviews = profile.getTotalReviews();
        double currentAverage = profile.getAverageRating();

        double newAverage = ((currentAverage * totalReviews) + newRating) / (totalReviews + 1);

        profile.setAverageRating(newAverage);
        profile.setTotalReviews(totalReviews + 1);
        teacherRepository.save(profile);
    }

    @Transactional
    public void incrementClassesTaught(String userId) {
        TeacherProfile profile = teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Teacher profile not found"));

        profile.setTotalClassesTaught(profile.getTotalClassesTaught() + 1);
        teacherRepository.save(profile);
    }

    @Transactional
    public void incrementNoShowCount(String userId) {
        TeacherProfile profile = teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Teacher profile not found"));

        profile.setNoShowCount(profile.getNoShowCount() + 1);

        if (profile.getNoShowCount() >= 3) {
            profile.setIsBlocked(true);
            profile.setBlockReason("Excessive no-shows");
        }

        teacherRepository.save(profile);
    }

    private TeacherDto mapToDto(TeacherProfile profile) {
        return TeacherDto.builder()
                .id(profile.getId())
                .bio(profile.getBio())
                .expertise(profile.getExpertise())
                .qualifications(profile.getQualifications())
                .yearsOfExperience(profile.getYearsOfExperience())
                .languages(profile.getLanguages())
                .timezone(profile.getTimezone())
                .videoIntroUrl(profile.getVideoIntroUrl())
                .verificationStatus(profile.getVerificationStatus())
                .averageRating(profile.getAverageRating())
                .totalReviews(profile.getTotalReviews())
                .totalClassesTaught(profile.getTotalClassesTaught())
                .rescheduleCount(profile.getRescheduleCount())
                .noShowCount(profile.getNoShowCount())
                .isBlocked(profile.getIsBlocked())
                .build();
    }
}
