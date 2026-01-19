package com.tcon.auth_user_service.service;

import com.tcon.auth_user_service.dto.StudentDto;
import com.tcon.auth_user_service.entity.StudentProfile;
import com.tcon.auth_user_service.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    @Transactional(readOnly = true)
    public StudentDto getStudentByUserId(String userId) {
        log.info("Fetching student profile for userId: {}", userId);

        StudentProfile profile = studentRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("Student profile not found for userId: {}", userId);
                    return new RuntimeException("Student profile not found");
                });

        log.info("Student profile found: {}", profile.getId());
        return mapToDto(profile);
    }

    @Transactional
    public void incrementDemoClassUsed(String userId) {
        log.info("Incrementing demo class used for userId: {}", userId);

        StudentProfile profile = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        profile.setDemoClassesUsed(profile.getDemoClassesUsed() + 1);
        studentRepository.save(profile);

        log.info("Demo class used incremented. New count: {}", profile.getDemoClassesUsed());
    }

    @Transactional
    public void addDemoClass(String userId, int count) {
        log.info("Adding {} demo classes for userId: {}", count, userId);

        StudentProfile profile = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        profile.setDemoClassesAvailable(profile.getDemoClassesAvailable() + count);
        studentRepository.save(profile);

        log.info("Demo classes added. New available count: {}", profile.getDemoClassesAvailable());
    }

    private StudentDto mapToDto(StudentProfile profile) {
        log.debug("Mapping StudentProfile to StudentDto");

        StudentDto dto = StudentDto.builder()
                .id(profile.getId())
                .dateOfBirth(profile.getDateOfBirth())
                .grade(profile.getGrade())
                .school(profile.getSchool())
                .interests(profile.getInterests())
                .learningGoals(profile.getLearningGoals())
                .preferredLanguage(profile.getPreferredLanguage())
                .timezone(profile.getTimezone())
                .demoClassesUsed(profile.getDemoClassesUsed())
                .demoClassesAvailable(profile.getDemoClassesAvailable())
                .build();

        log.debug("Mapped StudentDto: {}", dto);
        return dto;
    }
}
