package com.tcon.auth_user_service.user.service;

import com.tcon.auth_user_service.user.dto.StudentDto;
import com.tcon.auth_user_service.user.entity.StudentProfile;
import com.tcon.auth_user_service.user.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    @Transactional
    public StudentDto createProfile(String userId, StudentDto dto) {
        if (studentRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("Student profile already exists for user: " + userId);
        }

        StudentProfile profile = StudentProfile.builder()
                .userId(userId)
                .gradeLevel(dto.getGradeLevel())
                .schoolName(dto.getSchoolName())
                .dateOfBirth(dto.getDateOfBirth())
                .interests(dto.getInterests() != null ? dto.getInterests() : new ArrayList<>())
                .bio(dto.getBio())
                .parentId(dto.getParentId())
                .enrolledCourses(dto.getEnrolledCourses() != null ? dto.getEnrolledCourses() : new ArrayList<>())
                .build();

        StudentProfile saved = studentRepository.save(profile);
        log.info("Student profile created for userId: {}", userId);
        return toDto(saved);
    }

    public StudentDto getProfile(String userId) {
        StudentProfile profile = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Student profile not found for user: " + userId));
        return toDto(profile);
    }

    @Transactional
    public StudentDto updateProfile(String userId, StudentDto dto) {
        StudentProfile profile = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Student profile not found for user: " + userId));

        profile.setGradeLevel(dto.getGradeLevel());
        profile.setSchoolName(dto.getSchoolName());
        profile.setDateOfBirth(dto.getDateOfBirth());
        profile.setInterests(dto.getInterests() != null ? dto.getInterests() : new ArrayList<>());
        profile.setBio(dto.getBio());
        profile.setParentId(dto.getParentId());
        profile.setEnrolledCourses(dto.getEnrolledCourses() != null ? dto.getEnrolledCourses() : new ArrayList<>());

        StudentProfile updated = studentRepository.save(profile);
        log.info("Student profile updated for userId: {}", userId);
        return toDto(updated);
    }

    @Transactional
    public void deleteProfile(String userId) {
        StudentProfile profile = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Student profile not found for user: " + userId));

        studentRepository.delete(profile);
        log.info("Student profile deleted for userId: {}", userId);
    }

    public List<StudentDto> getStudentsByGrade(String gradeLevel) {
        return studentRepository.findByGradeLevel(gradeLevel).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ADD THIS METHOD
    public List<StudentDto> getStudentsByInterest(String interest) {
        return studentRepository.findByInterestsContaining(interest).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ADD THIS METHOD
    public List<StudentDto> getStudentsByParentId(String parentId) {
        return studentRepository.findByParentId(parentId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private StudentDto toDto(StudentProfile profile) {
        return StudentDto.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .gradeLevel(profile.getGradeLevel())
                .schoolName(profile.getSchoolName())
                .dateOfBirth(profile.getDateOfBirth())
                .interests(profile.getInterests() != null ? profile.getInterests() : new ArrayList<>())
                .bio(profile.getBio())
                .parentId(profile.getParentId())
                .enrolledCourses(profile.getEnrolledCourses() != null ? profile.getEnrolledCourses() : new ArrayList<>())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
