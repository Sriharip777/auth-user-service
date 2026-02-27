package com.tcon.auth_user_service.user.service;

import com.tcon.auth_user_service.user.entity.StudentProfile;
import com.tcon.auth_user_service.user.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParentStudentLinkService {

    private final StudentRepository studentRepository;

    // Parent -> child student userIds
    public List<String> getChildIdsForParent(String parentId) {
        List<StudentProfile> children = studentRepository.findByParentId(parentId);
        if (children == null || children.isEmpty()) {
            return Collections.emptyList();
        }
        return children.stream()
                .map(StudentProfile::getUserId)  // messaging uses userId
                .distinct()
                .toList();
    }

    // Student userId -> parentIds (currently single parentId field)
    public List<String> getParentIdsForStudent(String studentUserId) {
        return studentRepository.findByUserId(studentUserId)
                .map(profile -> profile.getParentId() == null
                        ? Collections.<String>emptyList()
                        : List.of(profile.getParentId()))
                .orElse(Collections.emptyList());
    }
}
