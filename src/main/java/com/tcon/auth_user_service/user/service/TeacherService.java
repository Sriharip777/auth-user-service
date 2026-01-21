package com.tcon.auth_user_service.user.service;

import com.tcon.auth_user_service.user.dto.TeacherDto;
import com.tcon.auth_user_service.user.dto.TeacherSearchDto;
import com.tcon.auth_user_service.user.entity.TeacherProfile;
import com.tcon.auth_user_service.user.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;

    @Transactional
    public TeacherDto createProfile(String userId, TeacherDto dto) {
        if (teacherRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("Teacher profile already exists for user: " + userId);
        }

        TeacherProfile profile = TeacherProfile.builder()
                .userId(userId)
                .bio(dto.getBio())
                .subjects(dto.getSubjects())
                .languages(dto.getLanguages())
                .yearsOfExperience(dto.getYearsOfExperience())
                .qualifications(dto.getQualifications())
                .hourlyRate(dto.getHourlyRate())
                .averageRating(0.0)
                .totalReviews(0)
                .verificationStatus("PENDING")
                .isAvailable(true)
                .timezone(dto.getTimezone())
                .build();

        TeacherProfile saved = teacherRepository.save(profile);
        log.info("Teacher profile created for userId: {}", userId);
        return toDto(saved);
    }

    public TeacherDto getProfile(String userId) {
        TeacherProfile profile = teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher profile not found for user: " + userId));
        return toDto(profile);
    }

    @Transactional
    public TeacherDto updateProfile(String userId, TeacherDto dto) {
        TeacherProfile profile = teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher profile not found for user: " + userId));

        profile.setBio(dto.getBio());
        profile.setSubjects(dto.getSubjects());
        profile.setLanguages(dto.getLanguages());
        profile.setYearsOfExperience(dto.getYearsOfExperience());
        profile.setQualifications(dto.getQualifications());
        profile.setHourlyRate(dto.getHourlyRate());
        profile.setIsAvailable(dto.getIsAvailable());
        profile.setTimezone(dto.getTimezone());

        TeacherProfile updated = teacherRepository.save(profile);
        log.info("Teacher profile updated for userId: {}", userId);
        return toDto(updated);
    }

    public List<TeacherDto> searchTeachers(TeacherSearchDto searchDto) {
        List<TeacherProfile> profiles = teacherRepository.findAll();

        return profiles.stream()
                .filter(p -> searchDto.getSubject() == null ||
                        p.getSubjects().stream().anyMatch(s -> s.equalsIgnoreCase(searchDto.getSubject())))
                .filter(p -> searchDto.getMinRating() == null ||
                        p.getAverageRating() >= searchDto.getMinRating())
                .filter(p -> searchDto.getMaxHourlyRate() == null ||
                        p.getHourlyRate() <= searchDto.getMaxHourlyRate())
                .filter(p -> searchDto.getMinYearsExperience() == null ||
                        p.getYearsOfExperience() >= searchDto.getMinYearsExperience())
                .filter(p -> !Boolean.TRUE.equals(searchDto.getAvailableOnly()) ||
                        Boolean.TRUE.equals(p.getIsAvailable()))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<TeacherDto> searchBySubject(String subject) {
        return teacherRepository.findBySubjectsContainingIgnoreCase(subject).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<TeacherDto> getTopRatedTeachers() {
        return teacherRepository.findByAverageRatingGreaterThanEqual(4.5).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateRating(String userId, Double newRating) {
        TeacherProfile profile = teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher profile not found for user: " + userId));

        int totalReviews = profile.getTotalReviews();
        double currentAverage = profile.getAverageRating();

        double newAverage = ((currentAverage * totalReviews) + newRating) / (totalReviews + 1);

        profile.setAverageRating(newAverage);
        profile.setTotalReviews(totalReviews + 1);

        teacherRepository.save(profile);
        log.info("Teacher rating updated for userId: {}. New average: {}", userId, newAverage);
    }

    private TeacherDto toDto(TeacherProfile profile) {
        return TeacherDto.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .bio(profile.getBio())
                .subjects(profile.getSubjects())
                .languages(profile.getLanguages())
                .yearsOfExperience(profile.getYearsOfExperience())
                .qualifications(profile.getQualifications())
                .hourlyRate(profile.getHourlyRate())
                .averageRating(profile.getAverageRating())
                .totalReviews(profile.getTotalReviews())
                .verificationStatus(profile.getVerificationStatus())
                .isAvailable(profile.getIsAvailable())
                .timezone(profile.getTimezone())
                .build();
    }
}
