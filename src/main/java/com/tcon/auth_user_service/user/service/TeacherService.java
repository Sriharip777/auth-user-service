package com.tcon.auth_user_service.user.service;

import com.tcon.auth_user_service.user.dto.TeacherDto;
import com.tcon.auth_user_service.user.dto.TeacherProfileResponseDto;
import com.tcon.auth_user_service.user.dto.TeacherSearchDto;
import com.tcon.auth_user_service.user.dto.UserProfileDto;
import com.tcon.auth_user_service.user.entity.TeacherProfile;
import com.tcon.auth_user_service.user.entity.TeacherVerification;
import com.tcon.auth_user_service.user.entity.UserStatus; // ✅ ADDED
import com.tcon.auth_user_service.user.repository.TeacherRepository;
import com.tcon.auth_user_service.user.repository.TeacherVerificationRepository;
import com.tcon.auth_user_service.user.repository.UserRepository; // ✅ ADDED
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserSearchService userSearchService;
    private final TeacherVerificationRepository teacherVerificationRepository;
    private final UserRepository userRepository; // ✅ ADDED

    /* =====================================================
       CREATE PROFILE
       ===================================================== */
    @Transactional
    public TeacherDto createProfile(String userId, TeacherDto dto) {

        if (teacherRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException(
                    "Teacher profile already exists for user: " + userId
            );
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
        profile.setProfileCompletion(calculateProfileCompletion(profile));
        TeacherProfile savedProfile = teacherRepository.save(profile);

        TeacherVerification verification = TeacherVerification.builder()
                .teacherUserId(userId)
                .status("PENDING")
                .documentUrls(List.of())
                .build();

        teacherVerificationRepository.save(verification);

        log.info("✅ Teacher profile + verification created for userId: {}", userId);

        return toDto(savedProfile);
    }

    /* =====================================================
       GET PROFILE (AUTO SUSPEND ON REFRESH IF REJECTED)
       ===================================================== */
    public TeacherDto getProfile(String userId) {

        TeacherProfile profile = teacherRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Teacher profile not found for user: " + userId)
                );

        if ("REJECTED".equals(profile.getVerificationStatus())) {

            Authentication authentication = SecurityContextHolder
                    .getContext()
                    .getAuthentication();

            String role = authentication.getAuthorities()
                    .iterator()
                    .next()
                    .getAuthority();

            // 🔴 If TEACHER accessing → suspend + block
            if (role.equals("ROLE_TEACHER")) {

                userRepository.findById(userId).ifPresent(user -> {
                    if (user.getStatus() != UserStatus.SUSPENDED) {
                        user.setStatus(UserStatus.SUSPENDED);
                        userRepository.save(user);
                        log.warn("🚫 Teacher {} auto-suspended due to rejected verification.", userId);
                    }
                });

                throw new AccessDeniedException(
                        "Your verification was rejected. Account suspended."
                );
            }

            // ✅ If ADMIN accessing → allow
        }


        return toDto(profile);
    }

    /* =====================================================
       UPDATE PROFILE (BLOCK REJECTED)
       ===================================================== */
    @Transactional
    public TeacherDto updateProfile(String userId, TeacherDto dto) {

        TeacherProfile profile = teacherRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Teacher profile not found for user: " + userId)
                );

        if ("REJECTED".equals(profile.getVerificationStatus())) {
            log.warn("❌ Update denied. Teacher {} verification rejected.", userId);
            throw new AccessDeniedException("Your verification was rejected.");
        }

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

    /* =====================================================
       SEARCH
       ===================================================== */
    public List<TeacherDto> searchTeachers(TeacherSearchDto searchDto) {

        List<TeacherProfile> profiles = teacherRepository.findAll();

        return profiles.stream()
                .filter(p -> searchDto.getSubject() == null ||
                        p.getSubjects().stream().anyMatch(s ->
                                s.equalsIgnoreCase(searchDto.getSubject())))
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
        return teacherRepository.findBySubjectsContainingIgnoreCase(subject)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<TeacherDto> getTopRatedTeachers() {
        return teacherRepository.findByAverageRatingGreaterThanEqual(4.5)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /* =====================================================
       UPDATE RATING
       ===================================================== */
    @Transactional
    public void updateRating(String userId, Double newRating) {

        TeacherProfile profile = teacherRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Teacher profile not found for user: " + userId)
                );

        int totalReviews = profile.getTotalReviews();
        double currentAverage = profile.getAverageRating();

        double newAverage =
                ((currentAverage * totalReviews) + newRating) / (totalReviews + 1);

        profile.setAverageRating(newAverage);
        profile.setTotalReviews(totalReviews + 1);

        teacherRepository.save(profile);

        log.info("Teacher rating updated for userId: {}. New average: {}", userId, newAverage);
    }

    /* =====================================================
       DTO MAPPER
       ===================================================== */
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
                .profileCompletion(profile.getProfileCompletion())
                .build();
    }

    /* =====================================================
       COMPLETE PROFILE
       ===================================================== */
    public TeacherProfileResponseDto getCompleteProfile(String userId) {

        log.info("📥 Fetching complete profile for teacher userId: {}", userId);

        TeacherDto teacherProfile = getProfile(userId);

        UserProfileDto userDetails = null;
        try {
            userDetails = userSearchService.getUserById(userId);
            log.info("✅ User details fetched for userId: {}", userId);
        } catch (Exception e) {
            log.warn("⚠️ Could not fetch user details for userId: {}. Error: {}", userId, e.getMessage());
        }

        TeacherProfileResponseDto response = TeacherProfileResponseDto.builder()
                .teacherProfile(teacherProfile)
                .userDetails(userDetails)
                .build();

        log.info("✅ Complete profile built. DisplayName: {}", response.getDisplayName());

        return response;
    }

    private int calculateProfileCompletion(TeacherProfile profile) {
        int score = 0;
        int maxScore = 5; // tune as you want

        if (profile.getBio() != null && !profile.getBio().isBlank()) score++;
        if (profile.getQualifications() != null && !profile.getQualifications().isBlank()) score++;
        if (profile.getHourlyRate() != null && profile.getHourlyRate() > 0) score++;
        if (profile.getIsAvailable() != null) score++;
        // if you later add teachingAreas and want to include it:
        // if (profile.getTeachingAreas() != null && !profile.getTeachingAreas().isEmpty()) score++;

        double percentage = ((double) score / maxScore) * 100;
        return (int) Math.round(percentage);
    }
}