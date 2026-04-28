package com.tcon.auth_user_service.user.service;

import com.tcon.auth_user_service.user.dto.*;
import com.tcon.auth_user_service.user.entity.TeacherProfile;
import com.tcon.auth_user_service.user.entity.TeacherVerification;
import com.tcon.auth_user_service.user.entity.TeachingArea;
import com.tcon.auth_user_service.user.entity.UserStatus;
import com.tcon.auth_user_service.user.repository.TeacherProfileRepository;
import com.tcon.auth_user_service.user.repository.TeacherRepository;
import com.tcon.auth_user_service.user.repository.TeacherVerificationRepository;
import com.tcon.auth_user_service.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final UserSearchService userSearchService;
    private final TeacherVerificationRepository teacherVerificationRepository;
    private final UserRepository userRepository;

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

        // 🔹 If admin approved earlier (before profile was created),
        //     set profile verificationStatus = VERIFIED and activate user.
        boolean alreadyApproved = teacherVerificationRepository.findByTeacherUserId(userId)
                .map(v -> "APPROVED".equalsIgnoreCase(v.getStatus()))
                .orElse(false);

        String verificationStatus = alreadyApproved ? "VERIFIED" : "PENDING";

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
                .verificationStatus(verificationStatus)
                .isAvailable(true)
                .timezone(dto.getTimezone())
                .teachingAreas(mapTeachingAreas(dto.getTeachingAreas()))
                .build();

        profile.setProfileCompletion(calculateProfileCompletion(profile));
        TeacherProfile savedProfile = teacherRepository.save(profile);

        if (alreadyApproved) {
            userRepository.findById(userId).ifPresent(user -> {
                if (user.getStatus() != UserStatus.ACTIVE) {
                    user.setStatus(UserStatus.ACTIVE);
                    userRepository.save(user);
                    log.info("✅ User {} activated because verification was already APPROVED", userId);
                }
            });
        }

        return toDto(savedProfile);
    }

    private List<TeachingArea> mapTeachingAreas(List<TeachingAreaDto> dtos) {
        if (dtos == null) return List.of();
        return dtos.stream()
                .map(a -> TeachingArea.builder()
                        .gradeId(a.getGradeId())
                        .grade(a.getGrade())
                        .subjectId(a.getSubjectId())
                        .subject(a.getSubject())
                        .topicIds(a.getTopicIds())
                        .topics(a.getTopics())
                        .build())
                .toList();
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

            if ("ROLE_TEACHER".equals(role)) {

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
        }

        return toDto(profile);
    }

    /* =====================================================
       UPDATE PROFILE (BLOCK REJECTED)
       ===================================================== */
    @Transactional
    public TeacherDto updateProfile(String userId, UpdateTeacherRequest dto) {

        TeacherProfile profile = teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher profile not found"));

        if ("REJECTED".equals(profile.getVerificationStatus())) {
            throw new AccessDeniedException("Your verification was rejected.");
        }

        if (dto.getBio() != null) profile.setBio(dto.getBio());
        if (dto.getSubjects() != null) profile.setSubjects(dto.getSubjects());
        if (dto.getLanguages() != null) profile.setLanguages(dto.getLanguages());
        if (dto.getYearsOfExperience() != null) profile.setYearsOfExperience(dto.getYearsOfExperience());
        if (dto.getQualifications() != null) profile.setQualifications(dto.getQualifications());
        if (dto.getHourlyRate() != null) profile.setHourlyRate(dto.getHourlyRate());
        if (dto.getIsAvailable() != null) profile.setIsAvailable(dto.getIsAvailable());
        if (dto.getTimezone() != null) profile.setTimezone(dto.getTimezone());
        if (dto.getTeachingAreas() != null) {
            profile.setTeachingAreas(mapTeachingAreas(dto.getTeachingAreas()));
        }

        profile.setProfileCompletion(calculateProfileCompletion(profile));
        TeacherProfile updated = teacherRepository.save(profile);
        log.info("✅ Teacher profile updated for userId: {}", userId);
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

        UserProfileDto user = null;

        try {
            user = userSearchService.getUserById(profile.getUserId());
        } catch (Exception e) {
            log.warn("⚠️ Could not fetch user for userId {}: {}", profile.getUserId(), e.getMessage());
        }

        return TeacherDto.builder()
                .id(profile.getId())
                .userId(profile.getUserId())

                // ✅ FIXED (user now defined)
                .firstName(user != null ? user.getFirstName() : null)
                .lastName(user != null ? user.getLastName() : null)

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
                .teachingAreas(
                        profile.getTeachingAreas() == null ? List.of() :
                                profile.getTeachingAreas().stream()
                                        .map(a -> TeachingAreaDto.builder()
                                                .gradeId(a.getGradeId())
                                                .grade(a.getGrade())
                                                .subjectId(a.getSubjectId())
                                                .subject(a.getSubject())
                                                .topicIds(a.getTopicIds())
                                                .topics(a.getTopics())
                                                .build())
                                        .toList()
                )
                .build();
    }

    private TeacherVerificationDto toVerificationDto(TeacherVerification verification) {
        return TeacherVerificationDto.builder()
                .id(verification.getId())
                .teacherUserId(verification.getTeacherUserId())
                .documentUrls(verification.getDocumentUrls())
                .status(verification.getStatus())
                .reviewerUserId(verification.getReviewerUserId())
                .reviewedAt(verification.getReviewedAt())
                .createdAt(verification.getCreatedAt())
                .updatedAt(verification.getUpdatedAt())
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
        int maxScore = 5;

        if (profile.getBio() != null && !profile.getBio().isBlank()) score++;
        if (profile.getQualifications() != null && !profile.getQualifications().isBlank()) score++;
        if (profile.getHourlyRate() != null && profile.getHourlyRate() > 0) score++;
        if (profile.getIsAvailable() != null) score++;
        if (profile.getTeachingAreas() != null && !profile.getTeachingAreas().isEmpty()) score++;
        double percentage = ((double) score / maxScore) * 100;
        return (int) Math.round(percentage);
    }

    /* =====================================================
       ELIGIBLE TEACHERS FOR COURSE
       ===================================================== */
    public List<TeacherResponseDto> findEligibleForCourse(
            String gradeId,
            String subjectId,
            List<String> topicIds
    ) {
        List<TeacherProfile> profiles = teacherRepository.findAll();
        List<String> safeTopicIds = topicIds != null ? topicIds : List.of();

        return profiles.stream()
                .filter(p -> "VERIFIED".equalsIgnoreCase(p.getVerificationStatus()))
                .filter(p -> Boolean.TRUE.equals(p.getIsAvailable()))
                .filter(p -> p.getTeachingAreas() != null && !p.getTeachingAreas().isEmpty())
                .filter(p -> p.getTeachingAreas().stream().anyMatch(area -> {
                    if (area == null) return false;
                    if (area.getGradeId() == null || area.getSubjectId() == null) return false;
                    if (!area.getGradeId().equals(gradeId)) return false;
                    if (!area.getSubjectId().equals(subjectId)) return false;

                    // If course has no topics → grade+subject is enough
                    if (safeTopicIds.isEmpty()) return true;

                    // If teacher has no topicIds for that area → still allow (grade+subject match)
                    if (area.getTopicIds() == null || area.getTopicIds().isEmpty()) return true;

                    // Otherwise require at least one common topic
                    return area.getTopicIds().stream().anyMatch(safeTopicIds::contains);
                }))
                .map(p -> {
                    UserProfileDto userDetails = null;
                    try {
                        userDetails = userSearchService.getUserById(p.getUserId());
                    } catch (Exception e) {
                        log.warn("Could not fetch user details for teacher userId {}: {}", p.getUserId(), e.getMessage());
                    }

                    String firstName = userDetails != null ? userDetails.getFirstName() : null;
                    String lastName  = userDetails != null ? userDetails.getLastName() : null;

                    List<String> subjects = p.getSubjects();
                    if (subjects == null || subjects.isEmpty()) {
                        subjects = p.getTeachingAreas() == null ? List.of()
                                : p.getTeachingAreas().stream()
                                .map(TeachingArea::getSubject)
                                .filter(s -> s != null && !s.isBlank())
                                .distinct()
                                .toList();
                    }

                    return TeacherResponseDto.builder()
                            .id(p.getId())
                            .userId(p.getUserId())
                            .firstName(firstName)
                            .lastName(lastName)
                            .bio(p.getBio())
                            .subjects(subjects)
                            .languages(p.getLanguages())
                            .yearsOfExperience(p.getYearsOfExperience())
                            .qualifications(p.getQualifications())
                            .hourlyRate(p.getHourlyRate())
                            .averageRating(p.getAverageRating())
                            .totalReviews(p.getTotalReviews())
                            .verificationStatus(p.getVerificationStatus())
                            .isAvailable(p.getIsAvailable())
                            .timezone(p.getTimezone())
                            .build();
                })
                .toList();
    }

}