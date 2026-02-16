package com.tcon.auth_user_service.user.service;

import com.tcon.auth_user_service.user.dto.ContactDto;
import com.tcon.auth_user_service.user.entity.ParentProfile;
import com.tcon.auth_user_service.user.entity.User;
import com.tcon.auth_user_service.user.entity.UserRole;
import com.tcon.auth_user_service.user.repository.ParentRepository;
import com.tcon.auth_user_service.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactService {

    private final ParentRepository parentRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Value("${services.learning.url}")
    private String learningServiceUrl;

    public List<ContactDto> getContactsForUser(String userId, UserRole role) {
        log.info("📇 Getting contacts for userId: {}, role: {}", userId, role);

        return switch (role) {
            case STUDENT -> getTeachersForStudent(userId);
            case TEACHER -> getStudentsForTeacher(userId);
            case PARENT -> getTeachersForParent(userId);
            default -> {
                log.warn("⚠️ Unsupported role for contacts: {}", role);
                yield new ArrayList<>();
            }
        };
    }

    private List<ContactDto> getTeachersForStudent(String studentId) {
        log.info("🔍 Finding teachers for student: {}", studentId);

        try {
            // Call Learning Management Service
            String url = learningServiceUrl + "/api/courses/student/" + studentId + "/teachers";
            log.debug("Calling LMS API: {}", url);

            List<String> teacherIds = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<String>>() {}
            ).getBody();

            if (teacherIds == null || teacherIds.isEmpty()) {
                log.warn("⚠️ No teachers found for student: {}", studentId);
                return new ArrayList<>();
            }

            log.info("✅ Found {} teachers for student", teacherIds.size());

            // Fetch teacher users
            return teacherIds.stream()
                    .map(userRepository::findById)
                    .filter(Optional::isPresent)
                    .map(opt -> convertToContactDto(opt.get()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("❌ Failed to fetch teachers for student: {}", studentId, e);
            return new ArrayList<>();
        }
    }

    private List<ContactDto> getStudentsForTeacher(String teacherId) {
        log.info("🔍 Finding students for teacher: {}", teacherId);

        try {
            // Call Learning Management Service
            String url = learningServiceUrl + "/api/courses/teacher/" + teacherId + "/students";
            log.debug("Calling LMS API: {}", url);

            List<String> studentIds = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<String>>() {}
            ).getBody();

            if (studentIds == null || studentIds.isEmpty()) {
                log.warn("⚠️ No students found for teacher: {}", teacherId);
                return new ArrayList<>();
            }

            log.info("✅ Found {} students for teacher", studentIds.size());

            // Fetch student users
            return studentIds.stream()
                    .map(userRepository::findById)
                    .filter(Optional::isPresent)
                    .map(opt -> convertToContactDto(opt.get()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("❌ Failed to fetch students for teacher: {}", teacherId, e);
            return new ArrayList<>();
        }
    }

    private List<ContactDto> getTeachersForParent(String parentId) {
        log.info("🔍 Finding teachers for parent: {}", parentId);

        // Get parent profile
        ParentProfile parent = parentRepository.findByUserId(parentId).orElse(null);

        if (parent == null || parent.getChildUserIds() == null || parent.getChildUserIds().isEmpty()) {
            log.warn("⚠️ Parent not found or no children: {}", parentId);
            return new ArrayList<>();
        }

        // Get teachers for all children
        Set<String> allTeacherIds = new HashSet<>();
        for (String childUserId : parent.getChildUserIds()) {
            try {
                String url = learningServiceUrl + "/api/courses/student/" + childUserId + "/teachers";
                List<String> teacherIds = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<String>>() {}
                ).getBody();

                if (teacherIds != null) {
                    allTeacherIds.addAll(teacherIds);
                }
            } catch (Exception e) {
                log.error("Failed to fetch teachers for child: {}", childUserId, e);
            }
        }

        log.info("✅ Found {} unique teachers for parent", allTeacherIds.size());

        // Fetch teacher users
        return allTeacherIds.stream()
                .map(userRepository::findById)
                .filter(Optional::isPresent)
                .map(opt -> convertToContactDto(opt.get()))
                .collect(Collectors.toList());
    }

    private ContactDto convertToContactDto(User user) {
        return ContactDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .profilePictureUrl(user.getProfilePictureUrl())
                .build();
    }
}