package com.tcon.auth_user_service.user.service;

import com.tcon.auth_user_service.user.dto.TeacherDto;
import com.tcon.auth_user_service.user.dto.TeacherProfileResponseDto;
import com.tcon.auth_user_service.user.dto.TeacherResponseDto;
import com.tcon.auth_user_service.user.dto.TeacherSearchDto;
import com.tcon.auth_user_service.user.dto.TeacherVerificationDto;
import com.tcon.auth_user_service.user.dto.TeachingAreaDto;
import com.tcon.auth_user_service.user.dto.UpdateTeacherRequest;
import com.tcon.auth_user_service.user.dto.UserProfileDto;
import com.tcon.auth_user_service.user.entity.TeacherProfile;
import com.tcon.auth_user_service.user.entity.TeacherVerification;
import com.tcon.auth_user_service.user.entity.TeachingArea;
import com.tcon.auth_user_service.user.entity.User;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    @Transactional
    public TeacherDto createProfile(String userId, TeacherDto dto) {

        if (teacherRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("Teacher profile already exists for user: " + userId);
        }

        boolean alreadyApproved = teacherVerificationRepository.findByTeacherUserId(userId)
                .map(v -> "APPROVED".equalsIgnoreCase(v.getStatus()))
                .orElse(false);

        String verificationStatus = alreadyApproved ? "VERIFIED" : "PENDING";

        TeacherProfile profile = TeacherProfile.builder()
                .userId(userId)
                .bio(dto.getBio())
                .subjects(cleanStringList(dto.getSubjects()))
                .languages(cleanStringList(dto.getLanguages()))
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
                    log.info("User {} activated because verification was already APPROVED", userId);
                }
            });
        }

        return toDto(savedProfile);
    }

    private List<TeachingArea> mapTeachingAreas(List<TeachingAreaDto> dtos) {
        if (dtos == null) {
            return List.of();
        }

        return dtos.stream()
                .filter(Objects::nonNull)
                .map(a -> TeachingArea.builder()
                        .gradeId(trimToNull(a.getGradeId()))
                        .grade(trimToNull(a.getGrade()))
                        .subjectId(trimToNull(a.getSubjectId()))
                        .subject(trimToNull(a.getSubject()))
                        .topicIds(cleanStringList(a.getTopicIds()))
                        .topics(cleanStringList(a.getTopics()))
                        .build())
                .toList();
    }

    public TeacherDto getProfile(String userId) {

        TeacherProfile profile = teacherRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Teacher profile not found for user: " + userId)
                );

        if ("REJECTED".equalsIgnoreCase(profile.getVerificationStatus())) {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String role = authentication.getAuthorities().iterator().next().getAuthority();

            if ("ROLE_TEACHER".equals(role)) {
                userRepository.findById(userId).ifPresent(user -> {
                    if (user.getStatus() != UserStatus.SUSPENDED) {
                        user.setStatus(UserStatus.SUSPENDED);
                        userRepository.save(user);
                        log.warn("Teacher {} auto-suspended due to rejected verification.", userId);
                    }
                });

                throw new AccessDeniedException("Your verification was rejected. Account suspended.");
            }
        }

        return toDto(profile);
    }

    @Transactional
    public TeacherDto updateProfile(String userId, UpdateTeacherRequest dto) {

        TeacherProfile profile = teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher profile not found"));

        if ("REJECTED".equalsIgnoreCase(profile.getVerificationStatus())) {
            throw new AccessDeniedException("Your verification was rejected.");
        }

        if (dto.getBio() != null) profile.setBio(dto.getBio());
        if (dto.getSubjects() != null) profile.setSubjects(cleanStringList(dto.getSubjects()));
        if (dto.getLanguages() != null) profile.setLanguages(cleanStringList(dto.getLanguages()));
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
        log.info("Teacher profile updated for userId: {}", userId);
        return toDto(updated);
    }

    public List<TeacherDto> searchTeachers(TeacherSearchDto searchDto) {

        List<TeacherProfile> profiles = teacherRepository.findAll();

        return profiles.stream()
                .filter(Objects::nonNull)
                .filter(p -> searchDto.getSubject() == null ||
                        defaultList(p.getSubjects()).stream().anyMatch(s ->
                                s != null && s.equalsIgnoreCase(searchDto.getSubject())))
                .filter(p -> searchDto.getMinRating() == null ||
                        (p.getAverageRating() != null && p.getAverageRating() >= searchDto.getMinRating()))
                .filter(p -> searchDto.getMaxHourlyRate() == null ||
                        (p.getHourlyRate() != null && p.getHourlyRate() <= searchDto.getMaxHourlyRate()))
                .filter(p -> searchDto.getMinYearsExperience() == null ||
                        (p.getYearsOfExperience() != null && p.getYearsOfExperience() >= searchDto.getMinYearsExperience()))
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

    @Transactional
    public void updateRating(String userId, Double newRating) {

        TeacherProfile profile = teacherRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Teacher profile not found for user: " + userId)
                );

        int totalReviews = profile.getTotalReviews() != null ? profile.getTotalReviews() : 0;
        double currentAverage = profile.getAverageRating() != null ? profile.getAverageRating() : 0.0;

        double newAverage = ((currentAverage * totalReviews) + newRating) / (totalReviews + 1);

        profile.setAverageRating(newAverage);
        profile.setTotalReviews(totalReviews + 1);

        teacherRepository.save(profile);

        log.info("Teacher rating updated for userId: {}. New average: {}", userId, newAverage);
    }

    private TeacherDto toDto(TeacherProfile profile) {

        UserProfileDto user = null;

        try {
            user = userSearchService.getUserById(profile.getUserId());
        } catch (Exception e) {
            log.warn("Could not fetch user for userId {}: {}", profile.getUserId(), e.getMessage());
        }

        return TeacherDto.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .firstName(user != null ? user.getFirstName() : null)
                .lastName(user != null ? user.getLastName() : null)
                .bio(profile.getBio())
                .subjects(defaultList(profile.getSubjects()))
                .languages(defaultList(profile.getLanguages()))
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
                                        .filter(Objects::nonNull)
                                        .map(a -> TeachingAreaDto.builder()
                                                .gradeId(a.getGradeId())
                                                .grade(a.getGrade())
                                                .subjectId(a.getSubjectId())
                                                .subject(a.getSubject())
                                                .topicIds(defaultList(a.getTopicIds()))
                                                .topics(defaultList(a.getTopics()))
                                                .build())
                                        .toList()
                )
                .build();
    }

    private TeacherVerificationDto toVerificationDto(TeacherVerification verification) {
        return TeacherVerificationDto.builder()
                .id(verification.getId())
                .teacherUserId(verification.getTeacherUserId())
                .documentUrls(defaultList(verification.getDocumentUrls()))
                .status(verification.getStatus())
                .reviewerUserId(verification.getReviewerUserId())
                .reviewedAt(verification.getReviewedAt())
                .createdAt(verification.getCreatedAt())
                .updatedAt(verification.getUpdatedAt())
                .build();
    }

    public TeacherProfileResponseDto getCompleteProfile(String userId) {

        log.info("Fetching complete profile for teacher userId: {}", userId);

        TeacherDto teacherProfile = getProfile(userId);

        UserProfileDto userDetails = null;
        try {
            userDetails = userSearchService.getUserById(userId);
            log.info("User details fetched for userId: {}", userId);
        } catch (Exception e) {
            log.warn("Could not fetch user details for userId: {}. Error: {}", userId, e.getMessage());
        }

        TeacherProfileResponseDto response = TeacherProfileResponseDto.builder()
                .teacherProfile(teacherProfile)
                .userDetails(userDetails)
                .build();

        log.info("Complete profile built. DisplayName: {}", response.getDisplayName());
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

    public List<TeacherResponseDto> findEligibleForCourse(
            String gradeId,
            String subjectId,
            List<String> topicIds
    ) {
        String safeGradeId = trimToNull(gradeId);
        String safeSubjectId = trimToNull(subjectId);
        List<String> safeTopicIds = cleanStringList(topicIds);

        if (safeGradeId == null) {
            throw new IllegalArgumentException("gradeId is required");
        }

        if (safeSubjectId == null) {
            throw new IllegalArgumentException("subjectId is required");
        }

        log.info("Finding eligible teachers for gradeId={}, subjectId={}, topicIds={}",
                safeGradeId, safeSubjectId, safeTopicIds);

        List<TeacherProfile> profiles = teacherProfileRepository.findAll();

        List<TeacherResponseDto> result = profiles.stream()
                .filter(Objects::nonNull)
                .filter(profile -> Boolean.TRUE.equals(profile.getIsAvailable()))
                .filter(profile -> !"REJECTED".equalsIgnoreCase(profile.getVerificationStatus()))
                .filter(profile -> {
                    User user = userRepository.findById(profile.getUserId()).orElse(null);
                    return user != null && user.getStatus() == UserStatus.ACTIVE;
                })
                .filter(profile -> defaultList(profile.getTeachingAreas()).stream()
                        .anyMatch(area -> matchesArea(area, safeGradeId, safeSubjectId, safeTopicIds)))
                .map(this::mapToTeacherResponseDto)
                .filter(Objects::nonNull)
                .toList();

        log.info("Eligible teachers found count={} for gradeId={}, subjectId={}",
                result.size(), safeGradeId, safeSubjectId);

        return result;
    }

    private boolean matchesArea(TeachingArea area, String gradeId, String subjectId, List<String> topicIds) {
        if (area == null) {
            return false;
        }

        String areaGradeId = trimToNull(area.getGradeId());
        String areaSubjectId = trimToNull(area.getSubjectId());

        if (!Objects.equals(gradeId, areaGradeId)) {
            return false;
        }

        if (!Objects.equals(subjectId, areaSubjectId)) {
            return false;
        }

        if (topicIds.isEmpty()) {
            return true;
        }

        List<String> areaTopicIds = cleanStringList(area.getTopicIds());
        if (areaTopicIds.isEmpty()) {
            return true;
        }

        return areaTopicIds.stream().anyMatch(topicIds::contains);
    }

    private TeacherResponseDto mapToTeacherResponseDto(TeacherProfile profile) {
        try {
            UserProfileDto user = null;

            try {
                user = userSearchService.getUserById(profile.getUserId());
            } catch (Exception ex) {
                log.warn("Could not fetch user details for teacher userId {}: {}",
                        profile.getUserId(), ex.getMessage());
            }

            return TeacherResponseDto.builder()
                    .id(profile.getId())
                    .userId(profile.getUserId())
                    .firstName(user != null ? user.getFirstName() : null)
                    .lastName(user != null ? user.getLastName() : null)
                    .bio(profile.getBio())
                    .subjects(defaultList(profile.getSubjects()))
                    .languages(defaultList(profile.getLanguages()))
                    .yearsOfExperience(profile.getYearsOfExperience())
                    .qualifications(profile.getQualifications())
                    .hourlyRate(profile.getHourlyRate())
                    .averageRating(profile.getAverageRating())
                    .totalReviews(profile.getTotalReviews())
                    .verificationStatus(profile.getVerificationStatus())
                    .isAvailable(profile.getIsAvailable())
                    .timezone(profile.getTimezone())
                    .build();
        } catch (Exception ex) {
            log.error("Failed to map teacher profile userId {}: {}", profile.getUserId(), ex.getMessage(), ex);
            return null;
        }
    }

    private <T> List<T> defaultList(List<T> list) {
        return list != null ? list : new ArrayList<>();
    }

    private List<String> cleanStringList(List<String> values) {
        if (values == null) {
            return new ArrayList<>();
        }

        return values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}