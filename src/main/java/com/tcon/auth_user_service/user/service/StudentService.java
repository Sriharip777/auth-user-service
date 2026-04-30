package com.tcon.auth_user_service.user.service;

import com.tcon.auth_user_service.user.dto.StudentDto;
import com.tcon.auth_user_service.user.entity.StudentProfile;
import com.tcon.auth_user_service.user.entity.User;
import com.tcon.auth_user_service.user.repository.StudentRepository;
import com.tcon.auth_user_service.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    private static final Random RANDOM = new Random();

    // ────────────────────────────────────────────────────────────
    // Build 4-letter prefix from first name
    // Example: Murali -> mura, Ann -> annx
    // ────────────────────────────────────────────────────────────
    private String buildPrefix(String firstName) {
        if (firstName == null || firstName.isBlank()) {
            return "user";
        }

        String cleaned = firstName.replaceAll("[^A-Za-z]", "").toLowerCase();

        if (cleaned.isBlank()) {
            cleaned = "user";
        }

        if (cleaned.length() >= 4) {
            return cleaned.substring(0, 4);
        }

        return String.format("%-4s", cleaned).replace(' ', 'x');
    }

    // ────────────────────────────────────────────────────────────
    // Auto-generate studentId from first name + 4 random digits
    // Example: mura3456
    // ────────────────────────────────────────────────────────────
    private String generateStudentId(String firstName) {
        String prefix = buildPrefix(firstName);
        String studentId;
        int attempts = 0;

        do {
            int digits = 1000 + RANDOM.nextInt(9000);
            studentId = prefix + digits;
            attempts++;

            if (attempts > 100) {
                throw new IllegalStateException(
                        "Could not generate unique studentId after 100 attempts");
            }
        } while (studentRepository.existsByStudentId(studentId));

        return studentId;
    }

    // ────────────────────────────────────────────────────────────
    // Create profile
    // ────────────────────────────────────────────────────────────
    @Transactional
    public StudentDto createProfile(String userId, StudentDto dto) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("UserId must not be null or blank");
        }

        if (studentRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException(
                    "Student profile already exists for user: " + userId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found for userId: " + userId));

        String studentId = generateStudentId(user.getFirstName());

        StudentProfile profile = StudentProfile.builder()
                .userId(userId)
                .studentId(studentId)
                .gradeLevel(dto.getGradeLevel())
                .schoolName(dto.getSchoolName())
                .dateOfBirth(dto.getDateOfBirth())
                .interests(dto.getInterests() != null
                        ? dto.getInterests() : new ArrayList<>())
                .bio(dto.getBio())
                .parentId(dto.getParentId())
                .enrolledCourses(dto.getEnrolledCourses() != null
                        ? dto.getEnrolledCourses() : new ArrayList<>())
                .build();

        StudentProfile saved = studentRepository.save(profile);
        log.info("✅ Student profile created for userId: {} with studentId: {}",
                userId, studentId);

        return toDtoWithUserDetails(saved);
    }

    // ────────────────────────────────────────────────────────────
    // Get profile
    // ────────────────────────────────────────────────────────────
    public StudentDto getProfile(String userId) {
        StudentProfile profile = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Student profile not found for user: " + userId));
        return toDtoWithUserDetails(profile);
    }

    // ────────────────────────────────────────────────────────────
    // Update profile
    // ────────────────────────────────────────────────────────────
    @Transactional
    public StudentDto updateProfile(String userId, StudentDto dto) {
        StudentProfile profile = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Student profile not found for user: " + userId));

        profile.setGradeLevel(dto.getGradeLevel());
        profile.setSchoolName(dto.getSchoolName());
        profile.setDateOfBirth(dto.getDateOfBirth());
        profile.setInterests(dto.getInterests() != null
                ? dto.getInterests() : new ArrayList<>());
        profile.setBio(dto.getBio());
        profile.setParentId(dto.getParentId());
        profile.setEnrolledCourses(dto.getEnrolledCourses() != null
                ? dto.getEnrolledCourses() : new ArrayList<>());
        // studentId never changes after creation

        StudentProfile updated = studentRepository.save(profile);
        log.info("✅ Student profile updated for userId: {}", userId);

        return toDtoWithUserDetails(updated);
    }

    // ────────────────────────────────────────────────────────────
    // Delete profile
    // ────────────────────────────────────────────────────────────
    @Transactional
    public void deleteProfile(String userId) {
        StudentProfile profile = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Student profile not found for user: " + userId));

        studentRepository.delete(profile);
        log.info("✅ Student profile deleted for userId: {}", userId);
    }

    // ────────────────────────────────────────────────────────────
    // By grade — exact match
    // ────────────────────────────────────────────────────────────
    public List<StudentDto> getStudentsByGrade(String gradeLevel) {
        if (gradeLevel == null || gradeLevel.isBlank()) {
            log.warn("⚠️ getStudentsByGrade called with empty gradeLevel");
            return List.of();
        }

        return studentRepository.findByGradeLevel(gradeLevel).stream()
                .map(this::toDtoWithUserDetails)
                .collect(Collectors.toList());
    }

    // ────────────────────────────────────────────────────────────
    // By grade — flexible contains + ignore case
    // ────────────────────────────────────────────────────────────
    public List<StudentDto> getStudentsByGradeFlexible(String gradeLevel) {
        return studentRepository.findByGradeLevelContainingIgnoreCase(gradeLevel)
                .stream()
                .map(this::toDtoWithUserDetails)
                .collect(Collectors.toList());
    }

    // ────────────────────────────────────────────────────────────
    // By interest
    // ────────────────────────────────────────────────────────────
    public List<StudentDto> getStudentsByInterest(String interest) {
        return studentRepository.findByInterestsContaining(interest).stream()
                .map(this::toDtoSafe)
                .collect(Collectors.toList());
    }

    // ────────────────────────────────────────────────────────────
    // By parentId — basic
    // ────────────────────────────────────────────────────────────
    public List<StudentDto> getStudentsByParentId(String parentId) {
        log.info("🔍 getStudentsByParentId: {}", parentId);
        return studentRepository.findByParentId(parentId).stream()
                .map(this::toDtoSafe)
                .collect(Collectors.toList());
    }

    // ────────────────────────────────────────────────────────────
    // By parentId — enriched
    // ────────────────────────────────────────────────────────────
    public List<StudentDto> getStudentsByParentIdWithDetails(String parentId) {
        log.info("🔍 getStudentsByParentIdWithDetails: {}", parentId);
        return studentRepository.findByParentId(parentId).stream()
                .map(this::toDtoWithUserDetails)
                .collect(Collectors.toList());
    }

    // ────────────────────────────────────────────────────────────
    // Mapping helpers
    // ────────────────────────────────────────────────────────────
    private StudentDto toDtoWithUserDetails(StudentProfile profile) {
        StudentDto dto = toDto(profile);

        Optional<User> userOpt = userRepository.findById(profile.getUserId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setEmail(user.getEmail());
            dto.setPhone(user.getPhoneNumber());
        } else {
            log.warn("⚠️ User record not found for userId: {} — returning partial profile",
                    profile.getUserId());
            dto.setFirstName("Unknown");
            dto.setLastName("Student");
        }

        return dto;
    }

    private StudentDto toDtoSafe(StudentProfile profile) {
        return toDto(profile);
    }

    private StudentDto toDto(StudentProfile profile) {
        return StudentDto.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .studentId(profile.getStudentId())
                .gradeLevel(profile.getGradeLevel())
                .schoolName(profile.getSchoolName())
                .dateOfBirth(profile.getDateOfBirth())
                .interests(profile.getInterests() != null
                        ? profile.getInterests() : new ArrayList<>())
                .bio(profile.getBio())
                .parentId(profile.getParentId())
                .enrolledCourses(profile.getEnrolledCourses() != null
                        ? profile.getEnrolledCourses() : new ArrayList<>())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}